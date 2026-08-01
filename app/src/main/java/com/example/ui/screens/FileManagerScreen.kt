package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.database.entity.RecycleBinEntity
import com.example.ui.theme.*
import java.io.File

fun File.isImageFile(): Boolean {
    val ext = extension.lowercase()
    return ext in listOf("jpg", "jpeg", "png", "webp", "gif", "heic")
}

fun File.isVideoFile(): Boolean {
    val ext = extension.lowercase()
    return ext in listOf("mp4", "mkv", "avi", "mov", "webm", "3gp")
}

fun File.isAudioFile(): Boolean {
    val ext = extension.lowercase()
    return ext in listOf("mp3", "flac", "aac", "wav", "m4a", "ogg")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    currentPath: String,
    trashItems: List<RecycleBinEntity>,
    onOpenFile: (File) -> Unit,
    onRestoreTrash: (String) -> Unit,
    onEmptyTrash: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Files, 1 = Recycle Bin
    var createFolderDialog by remember { mutableStateOf(false) }
    var folderNameInput by remember { mutableStateOf("") }

    var currentFolder by remember { mutableStateOf(File(currentPath)) }

    val filesList = remember(currentFolder) {
        try {
            if (currentFolder.exists() && currentFolder.isDirectory) {
                currentFolder.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    var selectedFileForMenu by remember { mutableStateOf<File?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Explorer", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateSurface),
                navigationIcon = {
                    if (currentFolder.parentFile != null && currentFolder.absolutePath != "/storage/emulated/0") {
                        IconButton(onClick = {
                            currentFolder.parentFile?.let { currentFolder = it }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali ke Atas", tint = TextPrimary)
                        }
                    }
                },
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
                    text = { Text("File Explorer", color = if (selectedTab == 0) CyanAccent else TextSecondary) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Recycle Bin (${trashItems.size})", color = if (selectedTab == 1) CyanAccent else TextSecondary) }
                )
            }

            if (selectedTab == 0) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Path breadcrumb banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SleekSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentFolder.absolutePath,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (file.isDirectory) {
                                                currentFolder = file
                                            } else {
                                                onOpenFile(file)
                                            }
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = SlateSurface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                file.isDirectory -> Icons.Default.Folder
                                                file.isImageFile() -> Icons.Default.Image
                                                file.isVideoFile() -> Icons.Default.Movie
                                                file.isAudioFile() -> Icons.Default.MusicNote
                                                else -> Icons.Default.InsertDriveFile
                                            },
                                            contentDescription = null,
                                            tint = when {
                                                file.isDirectory -> AmberWarning
                                                file.isImageFile() || file.isVideoFile() -> CyanAccent
                                                else -> M3PurplePrimary
                                            },
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = file.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = if (file.isDirectory) "Folder" else "${file.length() / 1024} KB",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                        IconButton(onClick = { selectedFileForMenu = file }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextSecondary)
                                        }
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
                                        Column(modifier = Modifier.weight(1f)) {
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
                    label = { Text("Nama Folder") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (folderNameInput.isNotEmpty()) {
                        val newDir = File(currentFolder, folderNameInput)
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

    if (selectedFileForMenu != null) {
        val targetFile = selectedFileForMenu!!
        AlertDialog(
            onDismissRequest = { selectedFileForMenu = null },
            title = { Text(targetFile.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Path: ${targetFile.absolutePath}")
                    Text("Ukuran: ${targetFile.length() / 1024} KB")
                }
            },
            confirmButton = {
                Button(onClick = {
                    try {
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", targetFile)
                        val ext = targetFile.extension.lowercase()
                        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, mime)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Buka File Dengan"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    selectedFileForMenu = null
                }) {
                    Text("Buka dengan Aplikasi Lain")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedFileForMenu = null }) {
                    Text("Tutup")
                }
            },
            containerColor = SlateSurface
        )
    }
}
