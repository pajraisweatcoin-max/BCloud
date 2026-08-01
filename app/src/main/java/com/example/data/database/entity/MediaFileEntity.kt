package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_files")
data class MediaFileEntity(
    @PrimaryKey val id: String, // Hash or canonical path
    val fileName: String,
    val path: String,
    val hddVolumeId: String,
    val hddVolumeLabel: String,
    val hddMountPath: String,
    val fileSizeBytes: Long,
    val mediaType: String, // "IMAGE", "VIDEO", "AUDIO", "DOCUMENT", "OTHER"
    val mimeType: String,
    val durationMs: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val playPositionMs: Long = 0,
    val parentPath: String,
    val thumbnailPath: String? = null
)
