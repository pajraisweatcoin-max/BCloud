package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.BarraSettings
import com.example.service.ServerConnectionResult
import com.example.service.TailscaleNetworkInfo
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: BarraSettings,
    networkInfo: TailscaleNetworkInfo,
    connectionResult: ServerConnectionResult?,
    isPinging: Boolean,
    onToggle: (String, Boolean) -> Unit,
    onUpdateServerAddress: (String, Int) -> Unit,
    onTestConnection: () -> Unit,
    onOpenTailscaleApp: () -> Unit,
    onOpenTailscaleConsole: () -> Unit,
    onSwitchMode: (String) -> Unit
) {
    var ipInput by remember(settings.serverIp) { mutableStateOf(settings.serverIp) }
    var portInput by remember(settings.serverPort) { mutableStateOf(settings.serverPort.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan & Network BARRA CLOUD", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateSurface)
            )
        },
        containerColor = SlateBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode Selector Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Mode Operasi Aplikasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Pilih peran perangkat ini dalam jaringan BARRA CLOUD:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = settings.appMode == "SERVER",
                                onClick = { onSwitchMode("SERVER") },
                                label = { Text("Server Mode (STB Host)", fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = settings.appMode == "VIEWER",
                                onClick = { onSwitchMode("VIEWER") },
                                label = { Text("Viewer Mode (HP Client)", fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (settings.appMode == "SERVER")
                                "STB bertindak sebagai Host Utama. Menjalankan REST API server di port ${settings.serverPort} & membagikan storage HDD ke jaringan."
                            else
                                "HP/Tablet bertindak sebagai Klien. Mengakses HDD & file media milik STB Server secara jarak jauh melalui IP Tailscale.",
                            style = MaterialTheme.typography.bodySmall,
                            color = M3PurplePrimary
                        )
                    }
                }
            }

            // Tailscale & Network Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pengaturan Tailscale & Network IP", fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (networkInfo.isTailscaleVpnActive) EmeraldSuccess.copy(alpha = 0.2f) else AmberWarning.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (networkInfo.isTailscaleVpnActive) "Tailscale VPN Active" else "Tailscale VPN Inactive",
                                    color = if (networkInfo.isTailscaleVpnActive) EmeraldSuccess else AmberWarning,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Detected Tailscale IP: ${networkInfo.tailscaleIp ?: "Tidak terdeteksi (Buka Tailscale)"}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text("Detected Local IP: ${networkInfo.localIp ?: "127.0.0.1"}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = ipInput,
                            onValueChange = { ipInput = it },
                            label = { Text("IP Target Server (Tailscale / MagicDNS)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = portInput,
                            onValueChange = { portInput = it },
                            label = { Text("Port Server (Default: 8080)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val port = portInput.toIntOrNull() ?: 8080
                                    onUpdateServerAddress(ipInput, port)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimaryContainer),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Simpan IP", color = CyanAccent)
                            }

                            Button(
                                onClick = onTestConnection,
                                enabled = !isPinging,
                                colors = ButtonDefaults.buttonColors(containerColor = M3PurplePrimaryContainer),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isPinging) "Mengecek..." else "Tes Ping", color = M3PurpleOnPrimaryContainer)
                            }
                        }

                        if (connectionResult != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = connectionResult.message,
                                color = if (connectionResult.isSuccess) EmeraldSuccess else RoseError,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = SleekBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onOpenTailscaleApp,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Buka App Tailscale", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = onOpenTailscaleConsole,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Admin Console", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Category: AUTOMATION TOGGLES
            item {
                Text("Semua Fitur Otomasi (ON / OFF)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyanAccent)
            }

            item { ToggleSettingRow("Auto Backup", "Backup otomatis foto/video dari Viewer ke Server STB", settings.autoBackup) { onToggle("autoBackup", it) } }
            item { ToggleSettingRow("Auto Scan HDD", "Pindai isi file harddisk USB secara latar belakang", settings.autoScanHdd) { onToggle("autoScanHdd", it) } }
            item { ToggleSettingRow("Auto Thumbnail", "Hasilkan pratinjau foto & video secara otomatis", settings.autoThumbnail) { onToggle("autoThumbnail", it) } }
            item { ToggleSettingRow("Auto Start Server", "Jalankan Server otomatis setelah STB dinyalakan", settings.autoStartServer) { onToggle("autoStartServer", it) } }
            item { ToggleSettingRow("Auto Mount HDD", "Mount harddisk USB otomatis saat dicolok", settings.autoMountHdd) { onToggle("autoMountHdd", it) } }
            item { ToggleSettingRow("Auto Reconnect Tailscale", "Hubungkan ulang Tailscale jika VPN terputus", settings.autoReconnectTailscale) { onToggle("autoReconnectTailscale", it) } }
            item { ToggleSettingRow("Auto Reboot", "Jadwal restart otomatis harian/mingguan", settings.autoReboot) { onToggle("autoReboot", it) } }
            item { ToggleSettingRow("Auto Resume Upload", "Lanjutkan upload gagal tanpa mengulang dari nol", settings.autoResumeUpload) { onToggle("autoResumeUpload", it) } }
            item { ToggleSettingRow("Auto Resume Download", "Lanjutkan download yang terputus", settings.autoResumeDownload) { onToggle("autoResumeDownload", it) } }
            item { ToggleSettingRow("Auto Resume Streaming", "Simpan posisi terakhir tontonan video", settings.autoResumeStreaming) { onToggle("autoResumeStreaming", it) } }
            item { ToggleSettingRow("Auto Resume Offline Queue", "Lanjutkan antrean transfer otomatis", settings.autoResumeOfflineQueue) { onToggle("autoResumeOfflineQueue", it) } }
            item { ToggleSettingRow("Auto Health Check", "Monitoring kesehatan hardware, RAM, CPU & Storage", settings.autoHealthCheck) { onToggle("autoHealthCheck", it) } }
            item { ToggleSettingRow("Auto Clean Cache", "Pembersihan berkala thumbnail & file sementara", settings.autoCleanCache) { onToggle("autoCleanCache", it) } }
            item { ToggleSettingRow("Auto Clean Recycle Bin", "Pembersihan berkala file terhapus di Trash", settings.autoCleanRecycleBin) { onToggle("autoCleanRecycleBin", it) } }
            item { ToggleSettingRow("Auto Detect HDD", "Deteksi cepat saat harddisk USB baru dicolok", settings.autoDetectHdd) { onToggle("autoDetectHdd", it) } }
            item { ToggleSettingRow("Auto Merge Library", "Gabungkan semua HDD menjadi satu perpustakaan", settings.autoMergeLibrary) { onToggle("autoMergeLibrary", it) } }
            item { ToggleSettingRow("Auto Scan USB Baru", "Langsung indeks file ketika USB baru terdeteksi", settings.autoScanUsbBaru) { onToggle("autoScanUsbBaru", it) } }
        }
    }
}

@Composable
fun ToggleSettingRow(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = CyanPrimaryContainer)
            )
        }
    }
}
