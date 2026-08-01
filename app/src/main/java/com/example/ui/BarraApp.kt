package com.example.ui

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.entity.MediaFileEntity
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarraViewModel
import java.io.File
import java.security.MessageDigest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraApp(
    viewModel: BarraViewModel,
    onRequestPermissions: () -> Unit = {}
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val stats by viewModel.systemStats.collectAsStateWithLifecycle()
    val hdds by viewModel.allHdds.collectAsStateWithLifecycle()
    val mediaList by viewModel.allMedia.collectAsStateWithLifecycle()
    val activeQueue by viewModel.activeQueue.collectAsStateWithLifecycle()
    val alerts by viewModel.allAlerts.collectAsStateWithLifecycle()
    val unreadAlerts by viewModel.unreadAlertsCount.collectAsStateWithLifecycle()
    val trashItems by viewModel.trashItems.collectAsStateWithLifecycle()

    val totalMedia by viewModel.totalMediaCount.collectAsStateWithLifecycle()
    val photosCount by viewModel.photosCount.collectAsStateWithLifecycle()
    val videosCount by viewModel.videosCount.collectAsStateWithLifecycle()

    val networkInfo by viewModel.networkInfo.collectAsStateWithLifecycle()
    val connectionResult by viewModel.connectionResult.collectAsStateWithLifecycle()
    val isPinging by viewModel.isPinging.collectAsStateWithLifecycle()
    val syncStatusMessage by viewModel.syncStatusMessage.collectAsStateWithLifecycle()

    var activeScreen by remember { mutableStateOf("DASHBOARD") }
    var selectedVideoForPlayback by remember { mutableStateOf<MediaFileEntity?>(null) }
    var selectedImageForView by remember { mutableStateOf<MediaFileEntity?>(null) }

    if (selectedVideoForPlayback != null) {
        VideoPlayerScreen(
            videoPathOrUrl = selectedVideoForPlayback!!.path,
            initialPositionMs = selectedVideoForPlayback!!.playPositionMs,
            onBack = { selectedVideoForPlayback = null },
            onSavePosition = { pos ->
                viewModel.addRecentView(
                    selectedVideoForPlayback!!.path,
                    selectedVideoForPlayback!!.fileName,
                    "VIDEO",
                    selectedVideoForPlayback!!.thumbnailPath
                )
            }
        )
        return
    }

    if (selectedImageForView != null) {
        ImageViewerScreen(
            media = selectedImageForView!!,
            onBack = { selectedImageForView = null },
            onToggleFavorite = { id, current -> viewModel.toggleFavorite(id, current) },
            onDelete = { id -> }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ARCHITECT SYSTEM",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = M3PurplePrimary,
                            fontSize = 10.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = M3PurplePrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BARRA CLOUD", fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SleekSurface),
                actions = {
                    // Mode Switcher Pill Button
                    Button(
                        onClick = {
                            val newMode = if (settings.appMode == "SERVER") "VIEWER" else "SERVER"
                            viewModel.switchMode(newMode)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = M3PurplePrimaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(M3PurplePrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (settings.appMode == "SERVER") "Server Mode" else "Viewer Mode",
                            color = M3PurpleOnPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Alert Center Action Icon with Unread Badge
                    BadgedBox(
                        badge = {
                            if (unreadAlerts > 0) {
                                Badge(containerColor = RoseError) {
                                    Text(unreadAlerts.toString(), color = Color.White)
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(onClick = { activeScreen = "ALERTS" }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = TextPrimary)
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SleekSurfaceVariant, tonalElevation = 2.dp) {
                NavigationBarItem(
                    selected = activeScreen == "DASHBOARD",
                    onClick = { activeScreen = "DASHBOARD" },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Status") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = M3PurpleOnPrimaryContainer, selectedTextColor = M3PurpleOnPrimaryContainer, indicatorColor = M3PurplePrimaryContainer)
                )
                NavigationBarItem(
                    selected = activeScreen == "GALLERY",
                    onClick = { activeScreen = "GALLERY" },
                    icon = { Icon(Icons.Default.Collections, contentDescription = "Galeri") },
                    label = { Text("Galeri") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = M3PurpleOnPrimaryContainer, selectedTextColor = M3PurpleOnPrimaryContainer, indicatorColor = M3PurplePrimaryContainer)
                )
                NavigationBarItem(
                    selected = activeScreen == "FILES",
                    onClick = { activeScreen = "FILES" },
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Files") },
                    label = { Text("Files") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = M3PurpleOnPrimaryContainer, selectedTextColor = M3PurpleOnPrimaryContainer, indicatorColor = M3PurplePrimaryContainer)
                )
                NavigationBarItem(
                    selected = activeScreen == "QUEUE",
                    onClick = { activeScreen = "QUEUE" },
                    icon = { Icon(Icons.Default.Sync, contentDescription = "Queue") },
                    label = { Text("Queue") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = M3PurpleOnPrimaryContainer, selectedTextColor = M3PurpleOnPrimaryContainer, indicatorColor = M3PurplePrimaryContainer)
                )
                NavigationBarItem(
                    selected = activeScreen == "SETTINGS",
                    onClick = { activeScreen = "SETTINGS" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = M3PurpleOnPrimaryContainer, selectedTextColor = M3PurpleOnPrimaryContainer, indicatorColor = M3PurplePrimaryContainer)
                )
                NavigationBarItem(
                    selected = activeScreen == "HELP",
                    onClick = { activeScreen = "HELP" },
                    icon = { Icon(Icons.Default.Help, contentDescription = "Help") },
                    label = { Text("Bantuan") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = M3PurpleOnPrimaryContainer, selectedTextColor = M3PurpleOnPrimaryContainer, indicatorColor = M3PurplePrimaryContainer)
                )
            }
        },
        containerColor = SleekBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Permission request banner if no media detected
            if (totalMedia == 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = M3PurpleSecondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Izin Penyimpanan Diperlukan",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = M3PurpleOnSecondaryContainer
                            )
                            Text(
                                "Izinkan akses file & media agar foto, video & file lokal dapat di-scan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onRequestPermissions()
                                viewModel.triggerScan()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimary)
                        ) {
                            Text("Izinkan / Scan", fontSize = 12.sp)
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                when (activeScreen) {
                    "DASHBOARD" -> DashboardScreen(
                        appMode = settings.appMode,
                        serverIp = settings.serverIp,
                        serverPort = settings.serverPort,
                        networkInfo = networkInfo,
                        connectionResult = connectionResult,
                        isPinging = isPinging,
                        syncStatusMessage = syncStatusMessage,
                        stats = stats,
                        hdds = hdds,
                        totalMediaCount = totalMedia,
                        photosCount = photosCount,
                        videosCount = videosCount,
                        queueCount = activeQueue.size,
                        onTriggerScan = { viewModel.triggerScan() },
                        onTestConnection = { viewModel.testServerConnection() },
                        onSyncRemote = { viewModel.syncMediaWithRemoteServer() },
                        onOpenTailscaleApp = { viewModel.openTailscaleApp() },
                        onNavigateToHdd = { activeScreen = "HDD" },
                        onNavigateToQueue = { activeScreen = "QUEUE" }
                    )
                    "HDD" -> HddStorageScreen(
                        hdds = hdds,
                        autoMergeLibrary = settings.autoMergeLibrary,
                        onToggleMergeLibrary = { viewModel.updateToggle("autoMergeLibrary", it) },
                        onTriggerScan = { viewModel.triggerScan() }
                    )
                    "GALLERY" -> GalleryScreen(
                        mediaList = mediaList,
                        onMediaClick = { media ->
                            if (media.mediaType == "VIDEO") {
                                selectedVideoForPlayback = media
                            } else if (media.mediaType == "IMAGE") {
                                selectedImageForView = media
                            }
                        },
                        onToggleFavorite = { id, current -> viewModel.toggleFavorite(id, current) }
                    )
                    "FILES" -> FileManagerScreen(
                        currentPath = Environment.getExternalStorageDirectory().absolutePath,
                        trashItems = trashItems,
                        onOpenFile = { file ->
                            val ext = file.extension.lowercase()
                            val path = file.absolutePath
                            val fileId = hashPath(path)
                            val mediaType = when (ext) {
                                "jpg", "jpeg", "png", "webp", "gif", "heic" -> "IMAGE"
                                "mp4", "mkv", "avi", "mov", "webm" -> "VIDEO"
                                else -> "OTHER"
                            }
                            val mediaEntity = MediaFileEntity(
                                id = fileId,
                                fileName = file.name,
                                path = file.absolutePath,
                                hddVolumeId = "INTERNAL_STORAGE",
                                hddVolumeLabel = "STB Storage Utama",
                                hddMountPath = "/storage/emulated/0",
                                fileSizeBytes = file.length(),
                                mediaType = mediaType,
                                mimeType = "*/*",
                                parentPath = file.parent ?: ""
                            )
                            if (mediaType == "VIDEO") {
                                selectedVideoForPlayback = mediaEntity
                            } else if (mediaType == "IMAGE") {
                                selectedImageForView = mediaEntity
                            }
                        },
                        onRestoreTrash = { id -> viewModel.restoreFromTrash(id) },
                        onEmptyTrash = { viewModel.emptyTrash() }
                    )
                    "QUEUE" -> OfflineQueueScreen(
                        queueList = activeQueue,
                        queueEnabled = settings.offlineSyncQueueEnabled,
                        onToggleQueue = { viewModel.updateToggle("offlineSyncQueueEnabled", it) },
                        onUpdateStatus = { id, status -> viewModel.updateQueueStatus(id, status) },
                        onUpdatePriority = { id, priority -> viewModel.updateQueuePriority(id, priority) },
                        onDeleteItem = { id -> viewModel.deleteQueueItem(id) },
                        onClearCompleted = { viewModel.clearCompletedQueue() }
                    )
                    "BACKUP" -> AutoBackupScreen(
                        settings = settings,
                        onToggleAutoBackup = { viewModel.updateToggle("autoBackup", it) },
                        onUpdateBackupMode = { mode -> }
                    )
                    "ALERTS" -> AlertCenterScreen(
                        alerts = alerts,
                        onClearAlerts = { viewModel.clearAlerts() },
                        onMarkRead = { id -> }
                    )
                    "SETTINGS" -> SettingsScreen(
                        settings = settings,
                        networkInfo = networkInfo,
                        connectionResult = connectionResult,
                        isPinging = isPinging,
                        onToggle = { key, valBool -> viewModel.updateToggle(key, valBool) },
                        onUpdateServerAddress = { ip, port -> viewModel.updateServerAddress(ip, port) },
                        onTestConnection = { viewModel.testServerConnection() },
                        onOpenTailscaleApp = { viewModel.openTailscaleApp() },
                        onOpenTailscaleConsole = { viewModel.openTailscaleConsole() },
                        onSwitchMode = { viewModel.switchMode(it) }
                    )
                    "HELP" -> HelpCenterScreen()
                }
            }
        }
    }
}

private fun hashPath(path: String): String {
    val bytes = MessageDigest.getInstance("MD5").digest(path.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
