package com.example.data.database.dao

import androidx.room.*
import com.example.data.database.entity.MediaFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_files ORDER BY dateAdded DESC")
    fun getAllMedia(): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE mediaType = :type ORDER BY dateAdded DESC")
    fun getMediaByType(type: String): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteMedia(): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE parentPath = :parent ORDER BY fileName ASC")
    fun getMediaInFolder(parent: String): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE fileName LIKE '%' || :query || '%' ORDER BY dateAdded DESC")
    fun searchMedia(query: String): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE path = :path LIMIT 1")
    suspend fun getMediaByPath(path: String): MediaFileEntity?

    @Query("SELECT COUNT(*) FROM media_files")
    fun getMediaCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM media_files WHERE mediaType = 'IMAGE'")
    fun getPhotosCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM media_files WHERE mediaType = 'VIDEO'")
    fun getVideosCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mediaList: List<MediaFileEntity>)

    @Query("UPDATE media_files SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: String, isFav: Boolean)

    @Query("UPDATE media_files SET playPositionMs = :pos WHERE id = :id")
    suspend fun updatePlayPosition(id: String, pos: Long)

    @Query("DELETE FROM media_files WHERE path = :path")
    suspend fun deleteByPath(path: String)

    @Query("DELETE FROM media_files WHERE hddVolumeId = :hddId")
    suspend fun deleteByHdd(hddId: String)

    @Query("DELETE FROM media_files")
    suspend fun clearAll()
}
