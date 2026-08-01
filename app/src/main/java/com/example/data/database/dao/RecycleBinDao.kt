package com.example.data.database.dao

import androidx.room.*
import com.example.data.database.entity.RecycleBinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecycleBinDao {
    @Query("SELECT * FROM recycle_bin ORDER BY deletedAt DESC")
    fun getAllTrash(): Flow<List<RecycleBinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrash(item: RecycleBinEntity)

    @Query("SELECT * FROM recycle_bin WHERE id = :id LIMIT 1")
    suspend fun getTrashById(id: String): RecycleBinEntity?

    @Query("DELETE FROM recycle_bin WHERE id = :id")
    suspend fun deleteTrash(id: String)

    @Query("DELETE FROM recycle_bin")
    suspend fun emptyTrash()
}
