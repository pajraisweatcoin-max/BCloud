package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.entity.HddVolumeEntity
import com.example.data.repository.SystemStats
import com.example.service.ServerConnectionResult
import com.example.service.TailscaleNetworkInfo
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    appMode: String,
    serverIp: String,
    serverPort: Int,
    networkInfo: TailscaleNetworkInfo,
    connectionResult: ServerConnectionResult?,
    isPinging: Boolean,
    syncStatusMessage: String,
    stats: SystemStats,
    hdds: List<HddVolumeEntity>,
    totalMediaCount: Int,
    photosCount: Int,
    videosCount: Int,
    queueCount: Int,
    onTriggerScan: () -> Unit,
    onTestConnection: () -> Unit,
    onSyncRemote: () -> Unit,
    onOpenTailscaleApp: () -> Unit,
    onNavigateToHdd: () -> Unit,
    onNavigateToQueue: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode & Server Active Card
        item {
            ServerStatusHeaderCard(appMode, stats, onTriggerScan)
        }

        // Tailscale & Network Status Card (Functional Mode & Tailscale)
        item {
            TailscaleModeNetworkCard(
                appMode = appMode,
                serverIp = serverIp,
                serverPort = serverPort,
                networkInfo = networkInfo,
                connectionResult = connectionResult,
                isPinging = isPinging,
                syncStatusMessage = syncStatusMessage,
                onTestConnection = onTestConnection,
                onSyncRemote = onSyncRemote,
                onOpenTailscaleApp = onOpenTailscaleApp
            )
        }

        // Gauges Row (CPU, RAM)
        item {
            GaugeGridSection(stats)
        }

        // Quick Media Stats Row
        item {
            MediaStatsRow(totalMediaCount, photosCount, videosCount, queueCount, onNavigateToQueue)
        }

        // Connected HDDs Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "USB HDDs Terhubung (${hdds.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = onNavigateToHdd) {
                    Text("Kelola HDD", color = CyanAccent)
                }
            }
        }

        if (hdds.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Usb,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Mendeteksi HDD USB...",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onTriggerScan,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimaryContainer)
                        ) {
                            Text("Scan USB Sekarang", color = CyanAccent)
                        }
                    }
                }
            }
        } else {
            items(hdds) { hdd ->
                HddCardItem(hdd)
            }
        }
    }
}

@Composable
fun ServerStatusHeaderCard(appMode: String, stats: SystemStats, onTriggerScan: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (appMode == "SERVER") EmeraldSuccess else CyanAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (appMode == "SERVER") "MODE SERVER (STB HOST)" else "MODE VIEWER (CLIENT)",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (appMode == "SERVER") EmeraldSuccess else CyanAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Uptime Sistem: ${stats.uptimeSeconds / 3600}j ${ (stats.uptimeSeconds % 3600) / 60 }m",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "Scanner Engine: ${stats.scannerStatus}",
                    style = MaterialTheme.typography.bodySmall,
                    color = M3PurplePrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onTriggerScan,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(M3PurplePrimaryContainer)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Rescan", tint = M3PurpleOnPrimaryContainer)
            }
        }
    }
}

@Composable
fun TailscaleModeNetworkCard(
    appMode: String,
    serverIp: String,
    serverPort: Int,
    networkInfo: TailscaleNetworkInfo,
    connectionResult: ServerConnectionResult?,
    isPinging: Boolean,
    syncStatusMessage: String,
    onTestConnection: () -> Unit,
    onSyncRemote: () -> Unit,
    onOpenTailscaleApp: () -> Unit
) {
    val clipboard = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (networkInfo.isTailscaleVpnActive) SleekSurfaceVariant else M3PurpleSecondaryContainer
        ),
        border = BorderStroke(1.dp, SleekBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (networkInfo.isTailscaleVpnActive) M3PurplePrimary else RoseError.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Tailscale Network & VPN",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (networkInfo.isTailscaleVpnActive) "VPN Terdeteksi / Tun Active" else "Tailscale Belum Terhubung",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (networkInfo.isTailscaleVpnActive) EmeraldSuccess else AmberWarning
                        )
                    }
                }

                TextButton(onClick = onOpenTailscaleApp) {
                    Text("Buka App", color = CyanAccent, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SleekBorder)
            Spacer(modifier = Modifier.height(12.dp))

            if (appMode == "SERVER") {
                // SERVER MODE INFORMATION
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("IP Tailscale STB: ${networkInfo.tailscaleIp ?: "Belum Ada (Buka App Tailscale)"}", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("IP LAN Local: ${networkInfo.localIp ?: "127.0.0.1"}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("Embedded HTTP Server Port: $serverPort (Ready)", style = MaterialTheme.typography.bodySmall, color = EmeraldSuccess)
                    Text("API Endpoint: http://${networkInfo.tailscaleIp ?: networkInfo.localIp ?: "localhost"}:$serverPort/api/status", style = MaterialTheme.typography.bodySmall, color = M3PurplePrimary)

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val ip = networkInfo.tailscaleIp ?: networkInfo.localIp ?: ""
                                if (ip.isNotEmpty()) {
                                    clipboard.setText(AnnotatedString("http://$ip:$serverPort"))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimaryContainer)
                        ) {
                            Text("Salin URL Server", color = M3PurpleOnPrimaryContainer, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // VIEWER MODE INFORMATION
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Target Server IP: http://$serverIp:$serverPort", fontWeight = FontWeight.Bold, color = TextPrimary)

                    if (connectionResult != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (connectionResult.isSuccess) EmeraldSuccess.copy(alpha = 0.2f) else RoseError.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = connectionResult.message,
                                color = if (connectionResult.isSuccess) EmeraldSuccess else RoseError,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onTestConnection,
                            enabled = !isPinging,
                            colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimaryContainer)
                        ) {
                            Text(if (isPinging) "Mengecek..." else "Tes Ping Server", color = M3PurpleOnPrimaryContainer, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onSyncRemote,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimaryContainer)
                        ) {
                            Text("Sync Media Server", color = CyanAccent, fontSize = 12.sp)
                        }
                    }

                    if (syncStatusMessage.isNotEmpty()) {
                        Text(syncStatusMessage, style = MaterialTheme.typography.bodySmall, color = CyanAccent)
                    }
                }
            }
        }
    }
}

@Composable
fun GaugeGridSection(stats: SystemStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GaugeTile(
            title = "CPU",
            value = "${stats.cpuUsagePercent.toInt()}%",
            progress = (stats.cpuUsagePercent / 100f).coerceIn(0f, 1f),
            color = if (stats.cpuUsagePercent > 80) RoseError else M3PurplePrimary,
            modifier = Modifier.weight(1f)
        )
        GaugeTile(
            title = "RAM",
            value = "${stats.ramUsedMb} MB",
            progress = (stats.ramUsedMb.toFloat() / stats.ramTotalMb.coerceAtLeast(1)).coerceIn(0f, 1f),
            color = if (stats.ramUsedMb > 800) AmberWarning else M3PurplePrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun GaugeTile(title: String, value: String, progress: Float, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
        border = BorderStroke(1.dp, SleekBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = M3PurplePrimary)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = color,
                trackColor = SleekBorder
            )
        }
    }
}

@Composable
fun MediaStatsRow(total: Int, photos: Int, videos: Int, queue: Int, onNavigateToQueue: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatBox("Foto", photos.toString(), Icons.Default.Image, M3PurplePrimary, Modifier.weight(1f))
        StatBox("Video", videos.toString(), Icons.Default.Movie, M3PurplePrimary, Modifier.weight(1f))
        StatBox("Queue", queue.toString(), Icons.Default.Sync, M3PurplePrimary, Modifier.weight(1f))
    }
}

@Composable
fun StatBox(title: String, count: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
        border = BorderStroke(1.dp, SleekBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = count, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}

@Composable
fun HddCardItem(hdd: HddVolumeEntity) {
    val usedBytes = hdd.totalSpaceBytes - hdd.freeSpaceBytes
    val usedGb = usedBytes / (1024 * 1024 * 1024)
    val totalGb = hdd.totalSpaceBytes / (1024 * 1024 * 1024)
    val progress = if (hdd.totalSpaceBytes > 0) usedBytes.toFloat() / hdd.totalSpaceBytes else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = M3PurplePrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = hdd.label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = hdd.mountPath, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = M3PurpleSecondaryContainer
                ) {
                    Text(
                        text = if (hdd.isMounted) "MOUNTED" else "UNMOUNTED",
                        style = MaterialTheme.typography.labelSmall,
                        color = M3PurpleOnSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = M3PurplePrimary,
                trackColor = SleekBorder
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "$usedGb GB terpakai dari $totalGb GB", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(text = "${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
