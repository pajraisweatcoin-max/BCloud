package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.database.entity.HddVolumeEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HddStorageScreen(
    hdds: List<HddVolumeEntity>,
    autoMergeLibrary: Boolean,
    onToggleMergeLibrary: (Boolean) -> Unit,
    onTriggerScan: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengelolaan Storage & USB HDD", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateSurface),
                actions = {
                    IconButton(onClick = onTriggerScan) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rescan HDDs", tint = CyanAccent)
                    }
                }
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
            // Unified Library Switch
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gabungkan Semua HDD ke 1 Library",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Semua file dari banyak HDD USB akan ditampilkan secara bersatu dalam Galeri",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = autoMergeLibrary,
                            onCheckedChange = onToggleMergeLibrary,
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = CyanPrimaryContainer)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Daftar Harddisk USB Terhubung (${hdds.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (hdds.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SlateSurface)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.UsbOff, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Tidak ada HDD USB terdeteksi", color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tancapkan HDD/FD USB ke port STB. BARRA CLOUD akan mendeteksi secara Otomatis Hot-Plug.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(hdds) { hdd ->
                    HddDetailCard(hdd)
                }
            }
        }
    }
}

@Composable
fun HddDetailCard(hdd: HddVolumeEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = hdd.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Path: ${hdd.mountPath}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                AssistChip(
                    onClick = {},
                    label = { Text(hdd.healthStatus, color = if (hdd.healthStatus == "GOOD") EmeraldSuccess else AmberWarning) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Format: ${hdd.fileSystem}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text(text = if (hdd.isPrimary) "Storage Utama STB" else "External USB HDD", style = MaterialTheme.typography.bodySmall, color = CyanAccent)
            }
        }
    }
}
