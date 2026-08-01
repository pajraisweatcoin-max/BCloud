package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "barra_settings")

data class BarraSettings(
    val appMode: String = "SERVER", // "SERVER" or "VIEWER"
    val serverIp: String = "100.100.100.1",
    val serverPort: Int = 8080,
    val autoBackup: Boolean = true,
    val autoScanHdd: Boolean = true,
    val autoThumbnail: Boolean = true,
    val autoStartServer: Boolean = true,
    val autoMountHdd: Boolean = true,
    val autoReconnectTailscale: Boolean = true,
    val autoReboot: Boolean = false,
    val autoResumeUpload: Boolean = true,
    val autoResumeDownload: Boolean = true,
    val autoResumeStreaming: Boolean = true,
    val autoResumeOfflineQueue: Boolean = true,
    val autoHealthCheck: Boolean = true,
    val autoErrorReport: Boolean = true,
    val autoCleanCache: Boolean = true,
    val autoCleanLog: Boolean = true,
    val autoCleanRecycleBin: Boolean = true,
    val autoRebuildDatabase: Boolean = false,
    val autoMaintenance: Boolean = true,
    val autoOptimization: Boolean = true,
    val autoCheckHddHealth: Boolean = true,
    val autoDetectHdd: Boolean = true,
    val autoMergeLibrary: Boolean = true,
    val autoScanUsbBaru: Boolean = true,
    val offlineSyncQueueEnabled: Boolean = true,
    val backupNetworkMode: String = "WIFI_AND_TAILSCALE", // "WIFI_ONLY", "TAILSCALE_ONLY", "WIFI_AND_TAILSCALE", "ALWAYS"
    val autoRebootSchedule: String = "DAILY", // "DAILY", "WEEKLY", "MONTHLY", "CUSTOM"
    val autoRebootTime: String = "03:00"
)

class BarraSettingsRepository(private val context: Context) {

    private object Keys {
        val APP_MODE = stringPreferencesKey("app_mode")
        val SERVER_IP = stringPreferencesKey("server_ip")
        val SERVER_PORT = intPreferencesKey("server_port")

        val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
        val AUTO_SCAN_HDD = booleanPreferencesKey("auto_scan_hdd")
        val AUTO_THUMBNAIL = booleanPreferencesKey("auto_thumbnail")
        val AUTO_START_SERVER = booleanPreferencesKey("auto_start_server")
        val AUTO_MOUNT_HDD = booleanPreferencesKey("auto_mount_hdd")
        val AUTO_RECONNECT_TAILSCALE = booleanPreferencesKey("auto_reconnect_tailscale")
        val AUTO_REBOOT = booleanPreferencesKey("auto_reboot")
        val AUTO_RESUME_UPLOAD = booleanPreferencesKey("auto_resume_upload")
        val AUTO_RESUME_DOWNLOAD = booleanPreferencesKey("auto_resume_download")
        val AUTO_RESUME_STREAMING = booleanPreferencesKey("auto_resume_streaming")
        val AUTO_RESUME_OFFLINE_QUEUE = booleanPreferencesKey("auto_resume_offline_queue")
        val AUTO_HEALTH_CHECK = booleanPreferencesKey("auto_health_check")
        val AUTO_ERROR_REPORT = booleanPreferencesKey("auto_error_report")
        val AUTO_CLEAN_CACHE = booleanPreferencesKey("auto_clean_cache")
        val AUTO_CLEAN_LOG = booleanPreferencesKey("auto_clean_log")
        val AUTO_CLEAN_RECYCLE_BIN = booleanPreferencesKey("auto_clean_recycle_bin")
        val AUTO_REBUILD_DATABASE = booleanPreferencesKey("auto_rebuild_database")
        val AUTO_MAINTENANCE = booleanPreferencesKey("auto_maintenance")
        val AUTO_OPTIMIZATION = booleanPreferencesKey("auto_optimization")
        val AUTO_CHECK_HDD_HEALTH = booleanPreferencesKey("auto_check_hdd_health")
        val AUTO_DETECT_HDD = booleanPreferencesKey("auto_detect_hdd")
        val AUTO_MERGE_LIBRARY = booleanPreferencesKey("auto_merge_library")
        val AUTO_SCAN_USB_BARU = booleanPreferencesKey("auto_scan_usb_baru")
        val OFFLINE_SYNC_QUEUE_ENABLED = booleanPreferencesKey("offline_sync_queue_enabled")

        val BACKUP_NETWORK_MODE = stringPreferencesKey("backup_network_mode")
        val AUTO_REBOOT_SCHEDULE = stringPreferencesKey("auto_reboot_schedule")
        val AUTO_REBOOT_TIME = stringPreferencesKey("auto_reboot_time")
    }

    val settingsFlow: Flow<BarraSettings> = context.dataStore.data.map { prefs ->
        BarraSettings(
            appMode = prefs[Keys.APP_MODE] ?: "SERVER",
            serverIp = prefs[Keys.SERVER_IP] ?: "100.100.100.1",
            serverPort = prefs[Keys.SERVER_PORT] ?: 8080,
            autoBackup = prefs[Keys.AUTO_BACKUP] ?: true,
            autoScanHdd = prefs[Keys.AUTO_SCAN_HDD] ?: true,
            autoThumbnail = prefs[Keys.AUTO_THUMBNAIL] ?: true,
            autoStartServer = prefs[Keys.AUTO_START_SERVER] ?: true,
            autoMountHdd = prefs[Keys.AUTO_MOUNT_HDD] ?: true,
            autoReconnectTailscale = prefs[Keys.AUTO_RECONNECT_TAILSCALE] ?: true,
            autoReboot = prefs[Keys.AUTO_REBOOT] ?: false,
            autoResumeUpload = prefs[Keys.AUTO_RESUME_UPLOAD] ?: true,
            autoResumeDownload = prefs[Keys.AUTO_RESUME_DOWNLOAD] ?: true,
            autoResumeStreaming = prefs[Keys.AUTO_RESUME_STREAMING] ?: true,
            autoResumeOfflineQueue = prefs[Keys.AUTO_RESUME_OFFLINE_QUEUE] ?: true,
            autoHealthCheck = prefs[Keys.AUTO_HEALTH_CHECK] ?: true,
            autoErrorReport = prefs[Keys.AUTO_ERROR_REPORT] ?: true,
            autoCleanCache = prefs[Keys.AUTO_CLEAN_CACHE] ?: true,
            autoCleanLog = prefs[Keys.AUTO_CLEAN_LOG] ?: true,
            autoCleanRecycleBin = prefs[Keys.AUTO_CLEAN_RECYCLE_BIN] ?: true,
            autoRebuildDatabase = prefs[Keys.AUTO_REBUILD_DATABASE] ?: false,
            autoMaintenance = prefs[Keys.AUTO_MAINTENANCE] ?: true,
            autoOptimization = prefs[Keys.AUTO_OPTIMIZATION] ?: true,
            autoCheckHddHealth = prefs[Keys.AUTO_CHECK_HDD_HEALTH] ?: true,
            autoDetectHdd = prefs[Keys.AUTO_DETECT_HDD] ?: true,
            autoMergeLibrary = prefs[Keys.AUTO_MERGE_LIBRARY] ?: true,
            autoScanUsbBaru = prefs[Keys.AUTO_SCAN_USB_BARU] ?: true,
            offlineSyncQueueEnabled = prefs[Keys.OFFLINE_SYNC_QUEUE_ENABLED] ?: true,
            backupNetworkMode = prefs[Keys.BACKUP_NETWORK_MODE] ?: "WIFI_AND_TAILSCALE",
            autoRebootSchedule = prefs[Keys.AUTO_REBOOT_SCHEDULE] ?: "DAILY",
            autoRebootTime = prefs[Keys.AUTO_REBOOT_TIME] ?: "03:00"
        )
    }

    suspend fun updateAppMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[Keys.APP_MODE] = mode }
    }

    suspend fun updateServerAddress(ip: String, port: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SERVER_IP] = ip
            prefs[Keys.SERVER_PORT] = port
        }
    }

    suspend fun updateToggle(keyName: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            when (keyName) {
                "autoBackup" -> prefs[Keys.AUTO_BACKUP] = enabled
                "autoScanHdd" -> prefs[Keys.AUTO_SCAN_HDD] = enabled
                "autoThumbnail" -> prefs[Keys.AUTO_THUMBNAIL] = enabled
                "autoStartServer" -> prefs[Keys.AUTO_START_SERVER] = enabled
                "autoMountHdd" -> prefs[Keys.AUTO_MOUNT_HDD] = enabled
                "autoReconnectTailscale" -> prefs[Keys.AUTO_RECONNECT_TAILSCALE] = enabled
                "autoReboot" -> prefs[Keys.AUTO_REBOOT] = enabled
                "autoResumeUpload" -> prefs[Keys.AUTO_RESUME_UPLOAD] = enabled
                "autoResumeDownload" -> prefs[Keys.AUTO_RESUME_DOWNLOAD] = enabled
                "autoResumeStreaming" -> prefs[Keys.AUTO_RESUME_STREAMING] = enabled
                "autoResumeOfflineQueue" -> prefs[Keys.AUTO_RESUME_OFFLINE_QUEUE] = enabled
                "autoHealthCheck" -> prefs[Keys.AUTO_HEALTH_CHECK] = enabled
                "autoErrorReport" -> prefs[Keys.AUTO_ERROR_REPORT] = enabled
                "autoCleanCache" -> prefs[Keys.AUTO_CLEAN_CACHE] = enabled
                "autoCleanLog" -> prefs[Keys.AUTO_CLEAN_LOG] = enabled
                "autoCleanRecycleBin" -> prefs[Keys.AUTO_CLEAN_RECYCLE_BIN] = enabled
                "autoRebuildDatabase" -> prefs[Keys.AUTO_REBUILD_DATABASE] = enabled
                "autoMaintenance" -> prefs[Keys.AUTO_MAINTENANCE] = enabled
                "autoOptimization" -> prefs[Keys.AUTO_OPTIMIZATION] = enabled
                "autoCheckHddHealth" -> prefs[Keys.AUTO_CHECK_HDD_HEALTH] = enabled
                "autoDetectHdd" -> prefs[Keys.AUTO_DETECT_HDD] = enabled
                "autoMergeLibrary" -> prefs[Keys.AUTO_MERGE_LIBRARY] = enabled
                "autoScanUsbBaru" -> prefs[Keys.AUTO_SCAN_USB_BARU] = enabled
                "offlineSyncQueueEnabled" -> prefs[Keys.OFFLINE_SYNC_QUEUE_ENABLED] = enabled
            }
        }
    }

    suspend fun updateBackupMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[Keys.BACKUP_NETWORK_MODE] = mode }
    }

    suspend fun updateRebootConfig(schedule: String, time: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_REBOOT_SCHEDULE] = schedule
            prefs[Keys.AUTO_REBOOT_TIME] = time
        }
    }
}
