package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.database.entity.RecycleBinEntity
import com.example.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    currentPath: String,
    trashItems: List<RecycleBinEntity>,
    onRestoreTrash: (String) -> Unit,
    onEmptyTrash: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Files, 1 = Recycle Bin
    var createFolderDialog by remember { mutableStateOf(false) }
    var folderNameInput by remember { mutableStateOf("") }

    val dir = File(currentPath)
    val filesList = remember(currentPath) {
        if (dir.exists() && dir.isDirectory) dir.listFiles()?.toList() ?: emptyList() else emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("File Manager & Storage Explorer", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateSurface),
                actions = {
                    IconButton(onClick = { createFolderDialog = true }) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "Buat Folder", tint = CyanAccent)
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
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SlateSurface,
                contentColor = CyanAccent
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Storage Explorer", color = if (selectedTab == 0) CyanAccent else TextSecondary) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Recycle Bin (${trashItems.size})", color = if (selectedTab == 1) CyanAccent else TextSecondary) }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Path: $currentPath",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    if (filesList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Folder ini kosong", color = TextSecondary)
                            }
                        }
                    } else {
                        items(filesList) { file ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = SlateSurface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        tint = if (file.isDirectory) AmberWarning else CyanAccent
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = file.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(
                                            text = if (file.isDirectory) "Folder" else "${file.length() / 1024} KB",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    IconButton(onClick = {}) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Recycle Bin View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sampah Terhapus", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        if (trashItems.isNotEmpty()) {
                            TextButton(onClick = onEmptyTrash) {
                                Text("Kosongkan Trash", color = RoseError)
                            }
                        }
                    }

                    if (trashItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Recycle Bin kosong", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(trashItems) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SlateSurface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(item.fileName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text(item.originalPath, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        }
                                        Button(
                                            onClick = { onRestoreTrash(item.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimaryContainer)
                                        ) {
                                            Text("Restore", color = CyanAccent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (createFolderDialog) {
        AlertDialog(
            onDismissRequest = { createFolderDialog = false },
            title = { Text("Buat Folder Baru", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = folderNameInput,
                    onValueChange = { folderNameInput = it },
                    label = { Text("Nama Folder") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (folderNameInput.isNotEmpty()) {
                        val newDir = File(currentPath, folderNameInput)
                        newDir.mkdirs()
                        folderNameInput = ""
                        createFolderDialog = false
                    }
                }) {
                    Text("Buat")
                }
            },
            dismissButton = {
                TextButton(onClick = { createFolderDialog = false }) {
                    Text("Batal")
                }
            },
            containerColor = SlateSurface
        )
    }
}
