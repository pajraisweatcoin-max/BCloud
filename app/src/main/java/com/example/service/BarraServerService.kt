package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.BarraApplication
import com.example.MainActivity
import com.example.R
import com.example.data.preferences.BarraSettingsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class BarraServerService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var embeddedServer: BarraEmbeddedServer? = null
        private set
    var scannerEngine: HddScannerEngine? = null
        private set
    var statsMonitor: SystemStatsMonitor? = null
        private set
    var queueManager: OfflineQueueManager? = null
        private set

    inner class LocalBinder : Binder() {
        fun getService(): BarraServerService = this@BarraServerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        promoteToForeground()

        scannerEngine = HddScannerEngine(this)
        statsMonitor = SystemStatsMonitor(this)
        queueManager = OfflineQueueManager(this)

        serviceScope.launch {
            val settings = BarraSettingsRepository(this@BarraServerService).settingsFlow.first()

            // Initialize Ktor / Embedded HTTP Server
            embeddedServer = BarraEmbeddedServer(this@BarraServerService, settings.serverPort).apply {
                start()
            }

            // Start Queue Processor
            queueManager?.startQueueProcessor(serviceScope)

            // Periodic Scan & Stats Monitor
            launch {
                while (isActive) {
                    statsMonitor?.updateStats()
                    if (settings.autoScanHdd) {
                        scannerEngine?.detectAndScanAllHdds(settings.autoMergeLibrary, settings.autoThumbnail)
                    }
                    delay(30000) // 30 sec cycle
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        promoteToForeground()
        return START_STICKY
    }

    private fun promoteToForeground() {
        val notification = buildNotification("BARRA CLOUD Server 24/7 Aktif")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        embeddedServer?.stop()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, BarraApplication.CHANNEL_ID_SERVER)
            .setContentTitle("BARRA CLOUD Media Server")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
    }
}
