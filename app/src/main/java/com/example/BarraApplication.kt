package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class BarraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serverChannel = NotificationChannel(
                CHANNEL_ID_SERVER,
                "BARRA CLOUD Server",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifikasi status Foreground Service BARRA CLOUD"
            }

            val alertChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "BARRA CLOUD Alert Center",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Peringatan sistem, HDD, dan jaringan"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(serverChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    companion object {
        const val CHANNEL_ID_SERVER = "barra_server_channel"
        const val CHANNEL_ID_ALERTS = "barra_alerts_channel"

        lateinit var instance: BarraApplication
            private set
    }
}
