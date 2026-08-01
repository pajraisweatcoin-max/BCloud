package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "HDD_REMOVED", "HDD_ADDED", "HDD_FULL", "SCANNER_FAILED", "SERVER_CRASH", "TAILSCALE_DISCONNECT", "UPLOAD_FAILED", "SYSTEM_HIGH_RAM", "SYSTEM_HIGH_CPU", "AUTO_REBOOT"
    val title: String,
    val message: String,
    val severity: String = "INFO", // "INFO", "WARNING", "ERROR"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
