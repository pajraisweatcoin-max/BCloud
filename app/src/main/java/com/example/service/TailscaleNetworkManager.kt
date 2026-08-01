package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.database.BarraDatabase
import com.example.data.database.entity.MediaFileEntity
import com.example.data.database.entity.HddVolumeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.NetworkInterface
import java.net.URL

data class TailscaleNetworkInfo(
    val tailscaleIp: String? = null,
    val localIp: String? = null,
    val isTailscaleVpnActive: Boolean = false,
    val interfaceNames: List<String> = emptyList()
)

data class ServerConnectionResult(
    val isSuccess: Boolean,
    val responseTimeMs: Long = -1,
    val message: String = "",
    val serverMode: String = "",
    val totalMedia: Int = 0
)

class TailscaleNetworkManager(private val context: Context) {

    private val db = BarraDatabase.getInstance(context)

    fun detectNetworkInterfaces(): TailscaleNetworkInfo {
        var tailscaleIp: String? = null
        var localIp: String? = null
        var isVpn = false
        val interfaces = mutableListOf<String>()

        try {
            val netInterfaces = NetworkInterface.getNetworkInterfaces() ?: return TailscaleNetworkInfo()
            for (intf in netInterfaces) {
                if (!intf.isUp || intf.isLoopback) continue
                interfaces.add(intf.name)

                if (intf.name.contains("tun") || intf.name.contains("tailscale") || intf.name.contains("vpn")) {
                    isVpn = true
                }

                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    if (addr.isLoopbackAddress) continue
                    val hostAddr = addr.hostAddress ?: continue
                    if (hostAddr.contains(":")) continue // Skip IPv6 for display simplicity

                    if (hostAddr.startsWith("100.")) {
                        tailscaleIp = hostAddr
                        isVpn = true
                    } else if (!hostAddr.startsWith("127.")) {
                        localIp = hostAddr
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return TailscaleNetworkInfo(
            tailscaleIp = tailscaleIp,
            localIp = localIp ?: "127.0.0.1",
            isTailscaleVpnActive = isVpn || tailscaleIp != null,
            interfaceNames = interfaces
        )
    }

    suspend fun pingServer(serverIp: String, port: Int): ServerConnectionResult = withContext(Dispatchers.IO) {
        if (serverIp.isBlank()) {
            return@withContext ServerConnectionResult(false, message = "IP Server belum diisi")
        }

        val startTime = System.currentTimeMillis()
        val urlString = "http://$serverIp:$port/api/status"

        return@withContext try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            val latency = System.currentTimeMillis() - startTime

            if (responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                val json = JSONObject(sb.toString())
                val mode = json.optString("mode", "SERVER")
                val totalMedia = json.optInt("totalMedia", 0)

                ServerConnectionResult(
                    isSuccess = true,
                    responseTimeMs = latency,
                    message = "Terhubung ($latency ms)",
                    serverMode = mode,
                    totalMedia = totalMedia
                )
            } else {
                ServerConnectionResult(
                    isSuccess = false,
                    message = "Server merespons HTTP $responseCode"
                )
            }
        } catch (e: Exception) {
            ServerConnectionResult(
                isSuccess = false,
                message = "Gagal terhubung: ${e.localizedMessage ?: "Timeout"}"
            )
        }
    }

    suspend fun fetchAndSyncMediaFromRemoteServer(serverIp: String, port: Int): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (serverIp.isBlank()) return@withContext Pair(false, "IP Server kosong")

        try {
            // Fetch Media List
            val mediaUrl = URL("http://$serverIp:$port/api/media")
            val conn = mediaUrl.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                val jsonArray = JSONArray(sb.toString())
                val mediaList = mutableListOf<MediaFileEntity>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    mediaList.add(
                        MediaFileEntity(
                            id = obj.optString("id", "remote_$i"),
                            fileName = obj.optString("fileName", "File $i"),
                            path = "http://$serverIp:$port/api/stream?path=" + Uri.encode(obj.optString("path")),
                            hddVolumeId = "REMOTE_SERVER",
                            hddVolumeLabel = "Remote Server ($serverIp)",
                            hddMountPath = "http://$serverIp:$port",
                            fileSizeBytes = obj.optLong("fileSizeBytes", 0L),
                            mediaType = obj.optString("mediaType", "OTHER"),
                            mimeType = obj.optString("mimeType", "*/*"),
                            isFavorite = obj.optBoolean("isFavorite", false),
                            parentPath = obj.optString("parentPath", "")
                        )
                    )
                }

                if (mediaList.isNotEmpty()) {
                    db.mediaDao().insertAll(mediaList)
                }

                return@withContext Pair(true, "Berhasil sinkron ${mediaList.size} file media dari $serverIp")
            } else {
                return@withContext Pair(false, "Server merespons HTTP ${conn.responseCode}")
            }
        } catch (e: Exception) {
            return@withContext Pair(false, "Gagal sinkron media: ${e.localizedMessage}")
        }
    }

    fun openTailscaleApp() {
        try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage("com.tailscale.ipn")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                // Open Play Store or Tailscale Web
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.tailscale.ipn")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openTailscaleAdminConsole() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://login.tailscale.com/admin/machines")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
