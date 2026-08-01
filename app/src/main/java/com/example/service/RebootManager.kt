package com.example.service

import android.content.Context
import android.content.Intent
import com.example.data.database.BarraDatabase
import com.example.data.database.entity.AlertEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RebootManager(private val context: Context) {

    private val db = BarraDatabase.getInstance(context)

    fun scheduleCountdownAndReboot(scope: CoroutineScope, delayMinutes: Int = 5) {
        scope.launch(Dispatchers.IO) {
            // 5-min warning
            db.alertDao().insertAlert(
                AlertEntity(
                    type = "AUTO_REBOOT",
                    title = "Peringatan Reboot Otomatis",
                    message = "Server akan melakukan reboot dalam $delayMinutes menit. Simpan semua pekerjaan Anda.",
                    severity = "WARNING"
                )
            )

            if (delayMinutes > 1) {
                delay((delayMinutes - 1) * 60 * 1000L)
            }

            // 1-min warning
            db.alertDao().insertAlert(
                AlertEntity(
                    type = "AUTO_REBOOT",
                    title = "PERINGATAN KRITIS REBOOT",
                    message = "Server akan melakukan reboot dalam 1 MENIT!",
                    severity = "ERROR"
                )
            )

            delay(60 * 1000L)

            // Check active transfers
            val activeTransfers = db.queueDao().getQueueItemById(1) // stub check
            // Perform reboot if root/su available or system permission
            executeReboot()
        }
    }

    private fun executeReboot() {
        try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot"))
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_REBOOT)
                intent.putExtra("nowait", 1)
                intent.putExtra("interval", 1)
                intent.putExtra("window", 0)
                context.sendBroadcast(intent)
            } catch (ex: Exception) {
                // If reboot permissions unavailable on standard non-rooted STB, notify
            }
        }
    }
}
