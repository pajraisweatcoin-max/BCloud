package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queue_items")
data class QueueItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "UPLOAD", "DOWNLOAD", "SYNC", "BACKUP"
    val fileName: String,
    val localPath: String,
    val remoteUrl: String,
    val bytesTransferred: Long = 0,
    val totalBytes: Long = 0,
    val status: String = "QUEUED", // "QUEUED", "RUNNING", "PAUSED", "COMPLETED", "FAILED"
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val priority: Int = 1 // Higher priority executes first
)
