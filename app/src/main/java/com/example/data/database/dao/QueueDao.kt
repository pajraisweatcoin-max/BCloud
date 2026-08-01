package com.example.data.database.dao

import androidx.room.*
import com.example.data.database.entity.QueueItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {
    @Query("SELECT * FROM queue_items ORDER BY priority DESC, timestamp ASC")
    fun getAllQueueItems(): Flow<List<QueueItemEntity>>

    @Query("SELECT * FROM queue_items WHERE status IN ('QUEUED', 'RUNNING', 'PAUSED') ORDER BY priority DESC, timestamp ASC")
    fun getActiveQueueItems(): Flow<List<QueueItemEntity>>

    @Query("SELECT * FROM queue_items WHERE id = :id LIMIT 1")
    suspend fun getQueueItemById(id: Long): QueueItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItem(item: QueueItemEntity): Long

    @Query("UPDATE queue_items SET status = :status, bytesTransferred = :bytesTransferred, errorMessage = :errorMessage WHERE id = :id")
    suspend fun updateProgress(id: Long, status: String, bytesTransferred: Long, errorMessage: String? = null)

    @Query("UPDATE queue_items SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE queue_items SET priority = :priority WHERE id = :id")
    suspend fun updatePriority(id: Long, priority: Int)

    @Query("DELETE FROM queue_items WHERE id = :id")
    suspend fun deleteQueueItem(id: Long)

    @Query("DELETE FROM queue_items WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()
}
