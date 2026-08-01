package com.example.data.database.dao

import androidx.room.*
import com.example.data.database.entity.RecentViewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentDao {
    @Query("SELECT * FROM recent_views ORDER BY viewedAt DESC LIMIT 50")
    fun getRecentlyViewed(): Flow<List<RecentViewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(recent: RecentViewEntity)

    @Query("DELETE FROM recent_views WHERE filePath = :path")
    suspend fun deleteRecent(path: String)

    @Query("DELETE FROM recent_views")
    suspend fun clearAll()
}
