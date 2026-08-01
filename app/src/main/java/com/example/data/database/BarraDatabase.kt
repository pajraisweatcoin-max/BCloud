package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.database.dao.*
import com.example.data.database.entity.*

@Database(
    entities = [
        MediaFileEntity::class,
        HddVolumeEntity::class,
        QueueItemEntity::class,
        AlertEntity::class,
        RecentViewEntity::class,
        RecycleBinEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BarraDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun hddDao(): HddDao
    abstract fun queueDao(): QueueDao
    abstract fun alertDao(): AlertDao
    abstract fun recentDao(): RecentDao
    abstract fun recycleBinDao(): RecycleBinDao

    companion object {
        @Volatile
        private var INSTANCE: BarraDatabase? = null

        fun getInstance(context: Context): BarraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BarraDatabase::class.java,
                    "barra_cloud_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
