package com.example.data.repository

import android.content.Context
import com.example.data.database.BarraDatabase
import com.example.data.database.entity.*
import com.example.data.preferences.BarraSettings
import com.example.data.preferences.BarraSettingsRepository
import kotlinx.coroutines.flow.Flow

data class SystemStats(
    val cpuUsagePercent: Float = 12.5f,
    val ramUsedMb: Long = 210,
    val ramTotalMb: Long = 1024,
    val bandwidthKbps: Long = 128,
    val serverStatus: String = "RUNNING 24/7",
    val uptimeSeconds: Long = 86400,
    val tailscaleConnected: Boolean = true,
    val tailscaleIp: String = "100.82.194.55",
    val activeViewersCount: Int = 2,
    val scannerStatus: String = "IDLE (100% Synced)",
    val thumbnailQueueCount: Int = 0
)

class BarraRepository(private val context: Context) {

    private val db = BarraDatabase.getInstance(context)
    val settingsRepo = BarraSettingsRepository(context)

    val allMedia: Flow<List<MediaFileEntity>> = db.mediaDao().getAllMedia()
    val allHdds: Flow<List<HddVolumeEntity>> = db.hddDao().getAllHdds()
    val activeQueue: Flow<List<QueueItemEntity>> = db.queueDao().getAllQueueItems()
    val allAlerts: Flow<List<AlertEntity>> = db.alertDao().getAllAlerts()
    val unreadAlertsCount: Flow<Int> = db.alertDao().getUnreadCount()
    val recentViews: Flow<List<RecentViewEntity>> = db.recentDao().getRecentlyViewed()
    val trashItems: Flow<List<RecycleBinEntity>> = db.recycleBinDao().getAllTrash()
    val settings: Flow<BarraSettings> = settingsRepo.settingsFlow

    val totalMediaCount: Flow<Int> = db.mediaDao().getMediaCount()
    val photosCount: Flow<Int> = db.mediaDao().getPhotosCount()
    val videosCount: Flow<Int> = db.mediaDao().getVideosCount()

    fun getMediaByType(type: String): Flow<List<MediaFileEntity>> = db.mediaDao().getMediaByType(type)
    fun getFavoriteMedia(): Flow<List<MediaFileEntity>> = db.mediaDao().getFavoriteMedia()
    fun searchMedia(query: String): Flow<List<MediaFileEntity>> = db.mediaDao().searchMedia(query)

    suspend fun insertMedia(media: MediaFileEntity) = db.mediaDao().insertMedia(media)
    suspend fun toggleFavorite(mediaId: String, currentFav: Boolean) = db.mediaDao().setFavorite(mediaId, !currentFav)
    suspend fun updatePlayPosition(mediaId: String, posMs: Long) = db.mediaDao().updatePlayPosition(mediaId, posMs)

    suspend fun addRecentView(recent: RecentViewEntity) = db.recentDao().insertRecent(recent)

    suspend fun addAlert(alert: AlertEntity) = db.alertDao().insertAlert(alert)
    suspend fun markAlertRead(id: Long) = db.alertDao().markAsRead(id)
    suspend fun clearAlerts() = db.alertDao().clearAllAlerts()

    suspend fun insertHdd(hdd: HddVolumeEntity) = db.hddDao().insertHdd(hdd)
    suspend fun updateHddMount(id: String, mounted: Boolean) = db.hddDao().updateMountStatus(id, mounted)

    suspend fun addQueueItem(item: QueueItemEntity): Long = db.queueDao().insertQueueItem(item)
    suspend fun updateQueueStatus(id: Long, status: String) = db.queueDao().updateStatus(id, status)
    suspend fun updateQueuePriority(id: Long, priority: Int) = db.queueDao().updatePriority(id, priority)
    suspend fun deleteQueueItem(id: Long) = db.queueDao().deleteQueueItem(id)
    suspend fun clearCompletedQueue() = db.queueDao().clearCompleted()

    suspend fun moveToTrash(id: String, originalPath: String, trashPath: String, fileName: String, sizeBytes: Long) {
        db.recycleBinDao().insertTrash(
            RecycleBinEntity(
                id = id,
                originalPath = originalPath,
                trashPath = trashPath,
                fileName = fileName,
                fileSizeBytes = sizeBytes
            )
        )
        db.mediaDao().deleteByPath(originalPath)
    }

    suspend fun restoreFromTrash(id: String) {
        val trash = db.recycleBinDao().getTrashById(id)
        if (trash != null) {
            db.recycleBinDao().deleteTrash(id)
        }
    }

    suspend fun emptyTrash() = db.recycleBinDao().emptyTrash()
}
