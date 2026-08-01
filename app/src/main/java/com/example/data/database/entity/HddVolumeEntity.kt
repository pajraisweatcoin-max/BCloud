package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hdd_volumes")
data class HddVolumeEntity(
    @PrimaryKey val volumeId: String,
    val label: String,
    val mountPath: String,
    val totalSpaceBytes: Long,
    val freeSpaceBytes: Long,
    val isMounted: Boolean = true,
    val isPrimary: Boolean = false,
    val fileSystem: String = "FAT32/NTFS/ext4",
    val healthStatus: String = "GOOD", // "GOOD", "WARNING", "BAD"
    val lastScanned: Long = System.currentTimeMillis()
)
