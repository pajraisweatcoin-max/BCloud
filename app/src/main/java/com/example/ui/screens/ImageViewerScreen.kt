package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.database.entity.MediaFileEntity
import com.example.ui.theme.AmberWarning
import java.io.File
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerScreen(
    media: MediaFileEntity,
    onBack: () -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current
    var showInfoDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Text(
                    text = media.fileName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Row {
                IconButton(onClick = { onToggleFavorite(media.id, media.isFavorite) }) {
                    Icon(
                        imageVector = if (media.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorit",
                        tint = if (media.isFavorite) AmberWarning else Color.White
                    )
                }
                IconButton(onClick = { showInfoDialog = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                }
                IconButton(onClick = {
                    try {
                        val file = File(media.path)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = media.mimeType.ifEmpty { "image/*" }
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Foto"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Bagikan", tint = Color.White)
                }
                IconButton(onClick = {
                    onDelete(media.id)
                    onBack()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                }
            }
        }

        // Image Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = File(media.path),
                contentDescription = media.fileName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Detail Foto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nama: ${media.fileName}", fontWeight = FontWeight.Bold)
                    Text("Path: ${media.path}")
                    Text("Ukuran: ${media.fileSizeBytes / (1024 * 1024)} MB (${media.fileSizeBytes} bytes)")
                    Text("Tipe: ${media.mimeType}")
                    Text("Storage: ${media.hddVolumeLabel}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Tutup")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
