package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.*
import com.example.data.preferences.BarraSettings
import com.example.data.repository.BarraRepository
import com.example.data.repository.SystemStats
import com.example.service.HddScannerEngine
import com.example.service.SystemStatsMonitor
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BarraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BarraRepository(application)
    private val scannerEngine = HddScannerEngine(application)
    private val statsMonitor = SystemStatsMonitor(application)

    val settings: StateFlow<BarraSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BarraSettings())

    val allMedia: StateFlow<List<MediaFileEntity>> = repository.allMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHdds: StateFlow<List<HddVolumeEntity>> = repository.allHdds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeQueue: StateFlow<List<QueueItemEntity>> = repository.activeQueue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlerts: StateFlow<List<AlertEntity>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadAlertsCount: StateFlow<Int> = repository.unreadAlertsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentViews: StateFlow<List<RecentViewEntity>> = repository.recentViews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashItems: StateFlow<List<RecycleBinEntity>> = repository.trashItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMediaCount: StateFlow<Int> = repository.totalMediaCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val photosCount: StateFlow<Int> = repository.photosCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val videosCount: StateFlow<Int> = repository.videosCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val systemStats: StateFlow<SystemStats> = statsMonitor.stats

    init {
        viewModelScope.launch {
            statsMonitor.updateStats()
        }
    }

    fun switchMode(mode: String) {
        viewModelScope.launch {
            repository.settingsRepo.updateAppMode(mode)
        }
    }

    fun updateToggle(keyName: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.settingsRepo.updateToggle(keyName, enabled)
        }
    }

    fun updateServerAddress(ip: String, port: Int) {
        viewModelScope.launch {
            repository.settingsRepo.updateServerAddress(ip, port)
        }
    }

    fun triggerScan() {
        viewModelScope.launch {
            val currSettings = settings.value
            scannerEngine.detectAndScanAllHdds(currSettings.autoMergeLibrary, currSettings.autoThumbnail)
        }
    }

    fun toggleFavorite(mediaId: String, current: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(mediaId, current)
        }
    }

    fun addQueueItem(type: String, fileName: String, localPath: String, remoteUrl: String, totalBytes: Long) {
        viewModelScope.launch {
            repository.addQueueItem(
                QueueItemEntity(
                    type = type,
                    fileName = fileName,
                    localPath = localPath,
                    remoteUrl = remoteUrl,
                    totalBytes = totalBytes
                )
            )
        }
    }

    fun updateQueueStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateQueueStatus(id, status)
        }
    }

    fun updateQueuePriority(id: Long, priority: Int) {
        viewModelScope.launch {
            repository.updateQueuePriority(id, priority)
        }
    }

    fun deleteQueueItem(id: Long) {
        viewModelScope.launch {
            repository.deleteQueueItem(id)
        }
    }

    fun clearCompletedQueue() {
        viewModelScope.launch {
            repository.clearCompletedQueue()
        }
    }

    fun restoreFromTrash(id: String) {
        viewModelScope.launch {
            repository.restoreFromTrash(id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }

    fun clearAlerts() {
        viewModelScope.launch {
            repository.clearAlerts()
        }
    }

    fun addRecentView(filePath: String, fileName: String, mediaType: String, thumbnailPath: String?) {
        viewModelScope.launch {
            repository.addRecentView(
                RecentViewEntity(
                    filePath = filePath,
                    fileName = fileName,
                    mediaType = mediaType,
                    viewedAt = System.currentTimeMillis(),
                    thumbnailPath = thumbnailPath
                )
            )
        }
    }
}
