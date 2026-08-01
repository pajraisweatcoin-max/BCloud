package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.database.entity.QueueItemEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineQueueScreen(
    queueList: List<QueueItemEntity>,
    queueEnabled: Boolean,
    onToggleQueue: (Boolean) -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onUpdatePriority: (Long, Int) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearCompleted: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Sync Queue", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateSurface),
                actions = {
                    IconButton(onClick = onClearCompleted) {
                        Icon(Icons.Default.CleaningServices, contentDescription = "Clear Completed", tint = CyanAccent)
                    }
                }
            )
        },
        containerColor = SlateBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Main Toggle Card
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
                        Text("Offline Sync Queue ON / OFF", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Otomatis antre transfer saat Tailscale / koneksi terputus dan lanjutkan secara mandiri tanpa terulang dari nol.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Switch(
                        checked = queueEnabled,
                        onCheckedChange = onToggleQueue,
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = CyanPrimaryContainer)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Daftar Antrean Transfer (${queueList.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)

            Spacer(modifier = Modifier.height(8.dp))

            if (queueList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Antrean kosong", color = TextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(queueList) { item ->
                        QueueCardItem(
                            item = item,
                            onPause = { onUpdateStatus(item.id, "PAUSED") },
                            onResume = { onUpdateStatus(item.id, "QUEUED") },
                            onCancel = { onDeleteItem(item.id) },
                            onPrioritize = { onUpdatePriority(item.id, item.priority + 1) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueueCardItem(
    item: QueueItemEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onPrioritize: () -> Unit
) {
    val progress = if (item.totalBytes > 0) item.bytesTransferred.toFloat() / item.totalBytes else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (item.type) {
                            "UPLOAD" -> Icons.Default.CloudUpload
                            "DOWNLOAD" -> Icons.Default.CloudDownload
                            else -> Icons.Default.Sync
                        },
                        contentDescription = null,
                        tint = CyanAccent
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(item.fileName, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${item.type} • Priori: ${item.priority}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }

                AssistChip(
                    onClick = {},
                    label = { Text(item.status) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = when (item.status) {
                    "RUNNING" -> CyanAccent
                    "COMPLETED" -> EmeraldSuccess
                    "FAILED" -> RoseError
                    else -> AmberWarning
                },
                trackColor = SlateSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (item.status == "RUNNING") {
                    TextButton(onClick = onPause) { Text("Pause", color = AmberWarning) }
                } else if (item.status == "PAUSED" || item.status == "FAILED") {
                    TextButton(onClick = onResume) { Text("Resume", color = CyanAccent) }
                }
                TextButton(onClick = onPrioritize) { Text("Prioritas", color = EmeraldSuccess) }
                TextButton(onClick = onCancel) { Text("Batal", color = RoseError) }
            }
        }
    }
}
