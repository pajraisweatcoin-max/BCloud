package com.example.service

import android.content.Context
import com.example.data.database.BarraDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.concurrent.Executors

class BarraEmbeddedServer(private val context: Context, private val port: Int = 8080) {

    private var serverSocket: ServerSocket? = null
    @Volatile
    private var isRunning = false
    private val executor = Executors.newCachedThreadPool()
    private val db = BarraDatabase.getInstance(context)

    fun start() {
        if (isRunning) return
        try {
            serverSocket = ServerSocket(port)
            isRunning = true
            executor.execute {
                while (isRunning) {
                    try {
                        val client = serverSocket?.accept() ?: break
                        executor.execute { handleClient(client) }
                    } catch (e: Exception) {
                        if (!isRunning) break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        serverSocket = null
        executor.shutdown()
    }

    private fun handleClient(client: Socket) {
        try {
            client.soTimeout = 10000
            val input = client.getInputStream()
            val reader = BufferedReader(InputStreamReader(input, "UTF-8"))
            val requestLine = reader.readLine() ?: return client.close()

            val parts = requestLine.split(" ")
            if (parts.size < 2) return client.close()

            val method = parts[0].uppercase()
            val rawUrl = parts[1]

            val headers = mutableMapOf<String, String>()
            var line: String?
            while (reader.readLine().also { line = it } != null && line!!.isNotEmpty()) {
                val headerParts = line!!.split(":", limit = 2)
                if (headerParts.size == 2) {
                    headers[headerParts[0].trim().lowercase()] = headerParts[1].trim()
                }
            }

            val path = if (rawUrl.contains("?")) rawUrl.substringBefore("?") else rawUrl
            val query = if (rawUrl.contains("?")) rawUrl.substringAfter("?") else ""

            val output = client.getOutputStream()

            when {
                path == "/api/status" -> handleStatus(output)
                path == "/api/hdds" -> handleHdds(output)
                path == "/api/media" -> handleMedia(output)
                path == "/api/stream" -> handleStream(query, headers, output)
                path == "/api/download" -> handleDownload(query, output)
                path == "/api/upload" -> handleUpload(method, input, headers, output)
                path == "/api/thumbnail" -> handleThumbnail(query, output)
                else -> sendResponse(output, "404 Not Found", "text/plain", "Not Found".toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { client.close() } catch (_: Exception) {}
        }
    }

    private fun handleStatus(output: OutputStream) {
        val json = JSONObject().apply {
            put("serverName", "BARRA CLOUD Server")
            put("status", "ONLINE")
            put("uptime", System.currentTimeMillis())
            put("port", port)
        }
        sendResponse(output, "200 OK", "application/json; charset=UTF-8", json.toString().toByteArray())
    }

    private fun handleHdds(output: OutputStream) {
        val arr = JSONArray()
        val json = JSONObject().apply {
            put("label", "Storage Utama")
            put("mountPath", "/storage/emulated/0")
            put("status", "MOUNTED")
        }
        arr.put(json)
        sendResponse(output, "200 OK", "application/json; charset=UTF-8", arr.toString().toByteArray())
    }

    private fun handleMedia(output: OutputStream) {
        val arr = JSONArray()
        sendResponse(output, "200 OK", "application/json; charset=UTF-8", arr.toString().toByteArray())
    }

    private fun handleStream(query: String, headers: Map<String, String>, output: OutputStream) {
        val pathParam = getQueryParam(query, "path")
        if (pathParam.isNullOrEmpty()) {
            return sendResponse(output, "400 Bad Request", "text/plain", "Missing path".toByteArray())
        }

        val file = File(URLDecoder.decode(pathParam, "UTF-8"))
        if (!file.exists() || !file.canRead()) {
            return sendResponse(output, "404 Not Found", "text/plain", "File not found".toByteArray())
        }

        val fileSize = file.length()
        val rangeHeader = headers["range"]

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            val ranges = rangeHeader.substring(6).split("-")
            val start = ranges[0].toLongOrNull() ?: 0L
            val end = if (ranges.size > 1 && ranges[1].isNotEmpty()) ranges[1].toLong() else fileSize - 1
            val contentLength = end - start + 1

            val headersText = "HTTP/1.1 206 Partial Content\r\n" +
                    "Content-Type: ${getMimeType(file.name)}\r\n" +
                    "Accept-Ranges: bytes\r\n" +
                    "Content-Range: bytes $start-$end/$fileSize\r\n" +
                    "Content-Length: $contentLength\r\n" +
                    "Connection: close\r\n\r\n"

            output.write(headersText.toByteArray())

            FileInputStream(file).use { fis ->
                fis.skip(start)
                val buffer = ByteArray(64 * 1024)
                var bytesToRead = contentLength
                while (bytesToRead > 0) {
                    val read = fis.read(buffer, 0, minOf(buffer.size.toLong(), bytesToRead).toInt())
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    bytesToRead -= read
                }
                output.flush()
            }
        } else {
            val headersText = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: ${getMimeType(file.name)}\r\n" +
                    "Accept-Ranges: bytes\r\n" +
                    "Content-Length: $fileSize\r\n" +
                    "Connection: close\r\n\r\n"

            output.write(headersText.toByteArray())
            FileInputStream(file).use { fis ->
                fis.copyTo(output)
            }
            output.flush()
        }
    }

    private fun handleDownload(query: String, output: OutputStream) {
        val pathParam = getQueryParam(query, "path")
            ?: return sendResponse(output, "400 Bad Request", "text/plain", "Missing path".toByteArray())
        val file = File(URLDecoder.decode(pathParam, "UTF-8"))
        if (!file.exists()) {
            return sendResponse(output, "404 Not Found", "text/plain", "File not found".toByteArray())
        }

        val headersText = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/octet-stream\r\n" +
                "Content-Disposition: attachment; filename=\"${file.name}\"\r\n" +
                "Content-Length: ${file.length()}\r\n" +
                "Connection: close\r\n\r\n"

        output.write(headersText.toByteArray())
        FileInputStream(file).use { fis ->
            fis.copyTo(output)
        }
        output.flush()
    }

    private fun handleUpload(method: String, input: InputStream, headers: Map<String, String>, output: OutputStream) {
        if (method == "POST") {
            sendResponse(output, "200 OK", "text/plain", "SUCCESS".toByteArray())
        } else {
            sendResponse(output, "405 Method Not Allowed", "text/plain", "Method not allowed".toByteArray())
        }
    }

    private fun handleThumbnail(query: String, output: OutputStream) {
        val pathParam = getQueryParam(query, "path")
            ?: return sendResponse(output, "400 Bad Request", "text/plain", "Missing path".toByteArray())
        val file = File(URLDecoder.decode(pathParam, "UTF-8"))
        if (file.exists()) {
            val headersText = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: image/jpeg\r\n" +
                    "Content-Length: ${file.length()}\r\n" +
                    "Connection: close\r\n\r\n"
            output.write(headersText.toByteArray())
            FileInputStream(file).use { it.copyTo(output) }
            output.flush()
        } else {
            sendResponse(output, "404 Not Found", "text/plain", "Not found".toByteArray())
        }
    }

    private fun sendResponse(output: OutputStream, status: String, contentType: String, body: ByteArray) {
        val headers = "HTTP/1.1 $status\r\n" +
                "Content-Type: $contentType\r\n" +
                "Content-Length: ${body.size}\r\n" +
                "Connection: close\r\n\r\n"
        output.write(headers.toByteArray())
        output.write(body)
        output.flush()
    }

    private fun getQueryParam(query: String, param: String): String? {
        query.split("&").forEach {
            val parts = it.split("=")
            if (parts.size == 2 && parts[0] == param) return parts[1]
        }
        return null
    }

    private fun getMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "mp3" -> "audio/mpeg"
            else -> "application/octet-stream"
        }
    }
}
