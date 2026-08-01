package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.database.BarraDatabase
import com.example.data.database.entity.AlertEntity
import com.example.data.preferences.BarraSettingsRepository
import com.example.service.HddScannerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UsbReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val db = BarraDatabase.getInstance(context)

        CoroutineScope(Dispatchers.IO).launch {
            val settings = BarraSettingsRepository(context).settingsFlow.first()

            when (action) {
                Intent.ACTION_MEDIA_MOUNTED, "android.hardware.usb.action.USB_DEVICE_ATTACHED" -> {
                    db.alertDao().insertAlert(
                        AlertEntity(
                            type = "HDD_ADDED",
                            title = "HDD Terpasang",
                            message = "HDD USB Baru terdeteksi dan dikoneksikan ke BARRA CLOUD.",
                            severity = "INFO"
                        )
                    )
                    if (settings.autoScanUsbBaru || settings.autoDetectHdd) {
                        val scanner = HddScannerEngine(context)
                        scanner.detectAndScanAllHdds(settings.autoMergeLibrary, settings.autoThumbnail)
                    }
                }
                Intent.ACTION_MEDIA_UNMOUNTED, Intent.ACTION_MEDIA_REMOVED, "android.hardware.usb.action.USB_DEVICE_DETACHED" -> {
                    db.alertDao().insertAlert(
                        AlertEntity(
                            type = "HDD_REMOVED",
                            title = "HDD Dilepas",
                            message = "Satu HDD USB telah dilepas dari perangkat STB.",
                            severity = "WARNING"
                        )
                    )
                }
            }
        }
    }
}
