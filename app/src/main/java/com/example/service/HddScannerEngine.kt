package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.util.Size
import com.example.data.database.BarraDatabase
import com.example.data.database.entity.HddVolumeEntity
import com.example.data.database.entity.MediaFileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class HddScannerEngine(private val context: Context) {

    private val db = BarraDatabase.getInstance(context)

    suspend fun detectAndScanAllHdds(autoMergeLibrary: Boolean, autoThumbnail: Boolean) = withContext(Dispatchers.IO) {
        val detectedHdds = mutableListOf<HddVolumeEntity>()

        // Primary internal/external storage
        val primaryStorage = Environment.getExternalStorageDirectory()
        if (primaryStorage.exists() && primaryStorage.canRead()) {
            val stat = StatFs(primaryStorage.path)
            val totalBytes = stat.totalBytes
            val freeBytes = stat.availableBytes
            val hdd = HddVolumeEntity(
                volumeId = "INTERNAL_STORAGE",
                label = "STB Storage Utama",
                mountPath = primaryStorage.absolutePath,
                totalSpaceBytes = totalBytes,
                freeSpaceBytes = freeBytes,
                isMounted = true,
                isPrimary = true,
                fileSystem = "ext4 / f2fs",
                healthStatus = "GOOD",
                lastScanned = System.currentTimeMillis()
            )
            detectedHdds.add(hdd)
            db.hddDao().insertHdd(hdd)
        }

        // Secondary USB Storage directories (/storage/ or /mnt/media_rw/)
        val storageRoots = listOf(File("/storage"), File("/mnt/media_rw"))
        storageRoots.forEach { root ->
            if (root.exists() && root.isDirectory) {
                root.listFiles()?.forEach { file ->
                    if (file.isDirectory && file.canRead() && !file.name.equals("emulated", ignoreCase = true) && !file.name.equals("self", ignoreCase = true)) {
                        try {
                            val stat = StatFs(file.path)
                            val totalBytes = stat.totalBytes
                            val freeBytes = stat.availableBytes
                            if (totalBytes > 0) {
                                val volumeId = "USB_HDD_" + file.name.uppercase()
                                val label = "USB Drive (" + file.name + ")"
                                val hdd = HddVolumeEntity(
                                    volumeId = volumeId,
                                    label = label,
                                    mountPath = file.absolutePath,
                                    totalSpaceBytes = totalBytes,
                                    freeSpaceBytes = freeBytes,
                                    isMounted = true,
                                    isPrimary = false,
                                    fileSystem = "NTFS / FAT32 / exFAT",
                                    healthStatus = if (freeBytes < totalBytes * 0.05) "WARNING" else "GOOD",
                                    lastScanned = System.currentTimeMillis()
                                )
                                detectedHdds.add(hdd)
                                db.hddDao().insertHdd(hdd)
                            }
                        } catch (e: Exception) {
                            // Ignored if stat fails
                        }
                    }
                }
            }
        }

        // Scan media files on detected HDDs
        scanMediaStore(autoThumbnail)
        detectedHdds.forEach { hdd ->
            scanDirectory(File(hdd.mountPath), hdd, autoThumbnail)
        }
    }

    private suspend fun scanMediaStore(autoThumbnail: Boolean) {
        try {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE
            )
            val cursor = context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                null,
                null,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
            )
            cursor?.use { c ->
                val dataCol = c.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                val nameCol = c.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeCol = c.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val dateCol = c.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeCol = c.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)

                val mediaList = mutableListOf<MediaFileEntity>()
                while (c.moveToNext()) {
                    val path = if (dataCol >= 0) c.getString(dataCol) else null
                    if (path.isNullOrEmpty()) continue
                    val name = if (nameCol >= 0) c.getString(nameCol) ?: File(path).name else File(path).name
                    val size = if (sizeCol >= 0) c.getLong(sizeCol) else 0L
                    val dateMod = if (dateCol >= 0) c.getLong(dateCol) * 1000 else System.currentTimeMillis()
                    val mime = if (mimeCol >= 0) c.getString(mimeCol) ?: "" else ""

                    if (name.startsWith(".")) continue
                    val file = File(path)
                    val fileId = hashPath(path)
                    val mediaType = getMediaType(name)

                    var thumbnailPath: String? = null
                    if (autoThumbnail && (mediaType == "IMAGE" || mediaType == "VIDEO") && file.exists()) {
                        thumbnailPath = generateThumbnail(file, fileId, mediaType)
                    }

                    val media = MediaFileEntity(
                        id = fileId,
                        fileName = name,
                        path = path,
                        hddVolumeId = "INTERNAL_STORAGE",
                        hddVolumeLabel = "STB Storage Utama",
                        hddMountPath = "/storage/emulated/0",
                        fileSizeBytes = if (size > 0) size else if (file.exists()) file.length() else 0L,
                        mediaType = mediaType,
                        mimeType = mime.ifEmpty { getMimeType(name) },
                        durationMs = 0,
                        dateAdded = dateMod,
                        dateModified = dateMod,
                        parentPath = file.parent ?: "",
                        thumbnailPath = thumbnailPath
                    )
                    mediaList.add(media)
                    if (mediaList.size >= 50) {
                        db.mediaDao().insertAll(mediaList)
                        mediaList.clear()
                    }
                }
                if (mediaList.isNotEmpty()) {
                    db.mediaDao().insertAll(mediaList)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun scanDirectory(dir: File, hdd: HddVolumeEntity, autoThumbnail: Boolean) {
        if (!dir.exists() || !dir.isDirectory || !dir.canRead()) return
        val files = dir.listFiles() ?: return

        val mediaList = mutableListOf<MediaFileEntity>()
        for (file in files) {
            if (file.name.startsWith(".")) continue
            if (file.isDirectory) {
                scanDirectory(file, hdd, autoThumbnail)
            } else {
                val mediaType = getMediaType(file.name)
                val mimeType = getMimeType(file.name)
                val fileId = hashPath(file.absolutePath)
                var thumbnailPath: String? = null

                if (autoThumbnail && (mediaType == "IMAGE" || mediaType == "VIDEO")) {
                    thumbnailPath = generateThumbnail(file, fileId, mediaType)
                }

                val media = MediaFileEntity(
                    id = fileId,
                    fileName = file.name,
                    path = file.absolutePath,
                    hddVolumeId = hdd.volumeId,
                    hddVolumeLabel = hdd.label,
                    hddMountPath = hdd.mountPath,
                    fileSizeBytes = file.length(),
                    mediaType = mediaType,
                    mimeType = mimeType,
                    durationMs = 0,
                    dateAdded = file.lastModified(),
                    dateModified = file.lastModified(),
                    parentPath = dir.absolutePath,
                    thumbnailPath = thumbnailPath
                )
                mediaList.add(media)
                if (mediaList.size >= 50) {
                    db.mediaDao().insertAll(mediaList)
                    mediaList.clear()
                }
            }
        }
        if (mediaList.isNotEmpty()) {
            db.mediaDao().insertAll(mediaList)
        }
    }

    private fun generateThumbnail(file: File, fileId: String, mediaType: String): String? {
        return try {
            val thumbDir = File(context.cacheDir, "thumbnails")
            if (!thumbDir.exists()) thumbDir.mkdirs()
            val thumbFile = File(thumbDir, "thumb_$fileId.jpg")
            if (thumbFile.exists()) return thumbFile.absolutePath

            var bitmap: Bitmap? = null
            if (mediaType == "VIDEO") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    bitmap = ThumbnailUtils.createVideoThumbnail(file, Size(320, 240), null)
                }
            } else if (mediaType == "IMAGE") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    bitmap = ThumbnailUtils.createImageThumbnail(file, Size(320, 240), null)
                }
            }
            if (bitmap != null) {
                FileOutputStream(thumbFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)
                }
                bitmap.recycle()
                thumbFile.absolutePath
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun getMediaType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "jpg", "jpeg", "png", "webp", "gif", "heic" -> "IMAGE"
            "mp4", "mkv", "avi", "mov", "webm", "ts", "m2ts", "flv" -> "VIDEO"
            "mp3", "flac", "aac", "wav", "m4a", "ogg" -> "AUDIO"
            "pdf", "txt", "doc", "docx", "zip", "rar" -> "DOCUMENT"
            else -> "OTHER"
        }
    }

    private fun getMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "avi" -> "video/x-msvideo"
            "mp3" -> "audio/mpeg"
            "pdf" -> "application/pdf"
            else -> "*/*"
        }
    }

    private fun hashPath(path: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(path.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
