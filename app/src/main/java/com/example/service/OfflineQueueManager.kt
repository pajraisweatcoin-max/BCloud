package com.example.service

import android.content.Context
import com.example.data.database.BarraDatabase
import com.example.data.database.entity.QueueItemEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

class OfflineQueueManager(private val context: Context) {

    private val db = BarraDatabase.getInstance(context)
    private val client = OkHttpClient.Builder().build()
    private var job: Job? = null

    fun startQueueProcessor(scope: CoroutineScope) {
        job?.cancel()
        job = scope.launch(Dispatchers.IO) {
            db.queueDao().getActiveQueueItems().collect { queueList ->
                val queued = queueList.filter { it.status == "QUEUED" }
                for (item in queued) {
                    processQueueItem(item)
                }
            }
        }
    }

    private suspend fun processQueueItem(item: QueueItemEntity) = withContext(Dispatchers.IO) {
        db.queueDao().updateStatus(item.id, "RUNNING")
        try {
            when (item.type) {
                "DOWNLOAD" -> downloadWithRange(item)
                "UPLOAD" -> uploadWithResume(item)
                else -> {
                    // Sync or Backup
                    delay(1000)
                    db.queueDao().updateProgress(item.id, "COMPLETED", item.totalBytes, null)
                }
            }
        } catch (e: Exception) {
            db.queueDao().updateProgress(item.id, "FAILED", item.bytesTransferred, e.localizedMessage ?: "Network error")
        }
    }

    private suspend fun downloadWithRange(item: QueueItemEntity) {
        val file = File(item.localPath)
        val downloadedBytes = if (file.exists()) file.length() else 0L

        val request = Request.Builder()
            .url(item.remoteUrl)
            .header("Range", "bytes=$downloadedBytes-")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful && response.code != 206) {
            db.queueDao().updateProgress(item.id, "FAILED", downloadedBytes, "HTTP ${response.code}")
            return
        }

        val body = response.body ?: run {
            db.queueDao().updateProgress(item.id, "FAILED", downloadedBytes, "Empty response body")
            return
        }

        val totalLength = (body.contentLength() + downloadedBytes).coerceAtLeast(item.totalBytes)
        val inputStream = body.byteStream()
        val raf = RandomAccessFile(file, "rw")
        raf.seek(downloadedBytes)

        val buffer = ByteArray(8192)
        var bytesRead: Int
        var currentBytes = downloadedBytes

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            // Check if status changed to PAUSED or CANCELLED
            val currentItem = db.queueDao().getQueueItemById(item.id)
            if (currentItem?.status == "PAUSED") {
                raf.close()
                inputStream.close()
                return
            }

            raf.write(buffer, 0, bytesRead)
            currentBytes += bytesRead
            db.queueDao().updateProgress(item.id, "RUNNING", currentBytes, null)
        }

        raf.close()
        inputStream.close()
        db.queueDao().updateProgress(item.id, "COMPLETED", currentBytes, null)
    }

    private suspend fun uploadWithResume(item: QueueItemEntity) {
        val file = File(item.localPath)
        if (!file.exists()) {
            db.queueDao().updateProgress(item.id, "FAILED", 0, "File tidak ditemukan")
            return
        }

        val requestBody = RequestBody.create("application/octet-stream".toMediaType(), file)
        val request = Request.Builder()
            .url(item.remoteUrl)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            db.queueDao().updateProgress(item.id, "COMPLETED", file.length(), null)
        } else {
            db.queueDao().updateProgress(item.id, "FAILED", 0, "HTTP ${response.code}")
        }
    }
}
