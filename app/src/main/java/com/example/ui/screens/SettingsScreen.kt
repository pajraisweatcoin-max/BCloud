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
import com.example.data.preferences.BarraSettings
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: BarraSettings,
    onToggle: (String, Boolean) -> Unit,
    onUpdateServerAddress: (String, Int) -> Unit
) {
    var ipInput by remember(settings.serverIp) { mutableStateOf(settings.serverIp) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Sempurna BARRA CLOUD", color = TextPrimary) },
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
            // Server Network Config
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Koneksi Tailscale & Server IP", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ipInput,
                            onValueChange = { ipInput = it },
                            label = { Text("IP Tailscale / MagicDNS") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onUpdateServerAddress(ipInput, settings.serverPort) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimaryContainer)
                        ) {
                            Text("Simpan IP Server", color = CyanAccent)
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
