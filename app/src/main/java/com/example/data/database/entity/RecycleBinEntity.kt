package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recycle_bin")
data class RecycleBinEntity(
    @PrimaryKey val id: String,
    val originalPath: String,
    val trashPath: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val deletedAt: Long = System.currentTimeMillis()
)
