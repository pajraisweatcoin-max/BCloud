package com.example.ui.screens

import androidx.compose.foundation.layout.*
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
fun AutoBackupScreen(
    settings: BarraSettings,
    onToggleAutoBackup: (Boolean) -> Unit,
    onUpdateBackupMode: (String) -> Unit
) {
    var selectedMode by remember(settings.backupNetworkMode) { mutableStateOf(settings.backupNetworkMode) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto Backup HP ke Server STB", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateSurface)
            )
        },
        containerColor = SlateBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Switch
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
                        Text("Auto Backup Foto & Video", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Simpan otomatis foto dan video baru dari HP ke Harddisk Server STB.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(
                        checked = settings.autoBackup,
                        onCheckedChange = onToggleAutoBackup,
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = CyanPrimaryContainer)
                    )
                }
            }

            Text("Pilihan Jaringan Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

            val modes = listOf(
                Pair("WIFI_ONLY", "WiFi Saja"),
                Pair("TAILSCALE_ONLY", "Tailscale Saja"),
                Pair("WIFI_AND_TAILSCALE", "WiFi + Tailscale"),
                Pair("ALWAYS", "Selalu (Sertukan Data Seluler)")
            )

            modes.forEach { mode ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMode == mode.first,
                            onClick = {
                                selectedMode = mode.first
                                onUpdateBackupMode(mode.first)
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = CyanAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(mode.second, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimaryContainer)
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, tint = CyanAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mulai Sinkronisasi Backup Sekarang", color = CyanAccent, fontWeight = FontWeight.Bold)
            }
        }
    }
}
