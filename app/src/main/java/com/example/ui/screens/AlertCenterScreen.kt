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
import com.example.data.database.entity.AlertEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertCenterScreen(
    alerts: List<AlertEntity>,
    onClearAlerts: () -> Unit,
    onMarkRead: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alert Center & Peringatan System", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateSurface),
                actions = {
                    if (alerts.isNotEmpty()) {
                        IconButton(onClick = onClearAlerts) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = CyanAccent)
                        }
                    }
                }
            )
        },
        containerColor = SlateBackground
    ) { padding ->
        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Semua Sistem Normal & Aman", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text("Tidak ada peringatan atau kesalahan terdeteksi", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(alerts) { alert ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (alert.severity) {
                                "ERROR" -> RoseError.copy(alpha = 0.15f)
                                "WARNING" -> AmberWarning.copy(alpha = 0.15f)
                                else -> SlateSurface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (alert.severity) {
                                    "ERROR" -> Icons.Default.Error
                                    "WARNING" -> Icons.Default.Warning
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = when (alert.severity) {
                                    "ERROR" -> RoseError
                                    "WARNING" -> AmberWarning
                                    else -> CyanAccent
                                },
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(alert.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(alert.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }

                            if (!alert.isRead) {
                                IconButton(onClick = { onMarkRead(alert.id) }) {
                                    Icon(Icons.Default.Done, contentDescription = "Read", tint = EmeraldSuccess)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
