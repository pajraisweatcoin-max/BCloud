package com.example.data.database.dao

import androidx.room.*
import com.example.data.database.entity.HddVolumeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HddDao {
    @Query("SELECT * FROM hdd_volumes ORDER BY label ASC")
    fun getAllHdds(): Flow<List<HddVolumeEntity>>

    @Query("SELECT * FROM hdd_volumes WHERE isMounted = 1")
    fun getMountedHdds(): Flow<List<HddVolumeEntity>>

    @Query("SELECT * FROM hdd_volumes WHERE volumeId = :volumeId LIMIT 1")
    suspend fun getHddById(volumeId: String): HddVolumeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHdd(hdd: HddVolumeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hdds: List<HddVolumeEntity>)

    @Query("UPDATE hdd_volumes SET isMounted = :mounted WHERE volumeId = :volumeId")
    suspend fun updateMountStatus(volumeId: String, mounted: Boolean)

    @Query("DELETE FROM hdd_volumes WHERE volumeId = :volumeId")
    suspend fun deleteHdd(volumeId: String)
}
