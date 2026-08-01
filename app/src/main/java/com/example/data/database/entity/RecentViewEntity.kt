package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_views")
data class RecentViewEntity(
    @PrimaryKey val filePath: String,
    val fileName: String,
    val mediaType: String,
    val viewedAt: Long = System.currentTimeMillis(),
    val playPositionMs: Long = 0,
    val thumbnailPath: String? = null
)
