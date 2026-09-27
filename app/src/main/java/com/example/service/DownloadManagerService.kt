package com.example.service

import android.content.Context
import android.media.MediaScannerConnection
import com.example.data.db.AppDatabase
import com.example.data.db.DownloadEntity
import com.example.data.model.DownloadStatus
import com.example.data.model.FormatOption
import com.example.data.model.MediaInfo
import com.example.util.FormatUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class LiveDownloadProgress(
    val id: String,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val speedBytesPerSec: Long,
    val etaSeconds: Long,
    val percent: Float,
    val status: DownloadStatus
)

class DownloadManagerService(
    private val context: Context,
    private val database: AppDatabase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = ConcurrentHashMap<String, Job>()
    private val downloadDao = database.downloadDao()

    private val _liveProgress = MutableStateFlow<Map<String, LiveDownloadProgress>>(emptyMap())
    val liveProgress: StateFlow<Map<String, LiveDownloadProgress>> = _liveProgress.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    fun enqueueDownload(
        mediaInfo: MediaInfo,
        format: FormatOption,
        saveDir: String,
        namingTemplate: String
    ): String {
        val downloadId = UUID.randomUUID().toString()
        val videoId = mediaInfo.url.hashCode().toString().takeLast(8)

        val fileName = FormatUtils.generateFileName(
            template = namingTemplate,
            uploader = mediaInfo.uploader,
            id = videoId,
            title = mediaInfo.title,
            ext = format.extension
        )

        // Resolve target directory with safety fallback
        val targetDir = resolveWritableDirectory(saveDir)
        val targetFile = File(targetDir, fileName)

        val entity = DownloadEntity(
            id = downloadId,
            url = mediaInfo.url,
            title = mediaInfo.title,
            uploader = mediaInfo.uploader,
            extractor = mediaInfo.extractor,
            durationSeconds = mediaInfo.durationSeconds,
            thumbnailUrl = mediaInfo.thumbnailUrl,
            formatLabel = format.label,
            extension = format.extension,
            filePath = targetFile.absolutePath,
            fileName = fileName,
            totalBytes = format.estimatedSizeBytes,
            downloadedBytes = 0,
            speedBytesPerSec = 0,
            etaSeconds = 0,
            status = DownloadStatus.QUEUED.name,
            isAudio = format.isAudioOnly
        )

        scope.launch {
            downloadDao.insert(entity)
            startDownloadJob(downloadId, format.downloadUrl, targetFile)
        }

        return downloadId
    }

    private fun resolveWritableDirectory(desiredPath: String): File {
        val dir = File(desiredPath)
        try {
            if (!dir.exists()) {
                dir.mkdirs()
            }
            if (dir.exists() && dir.canWrite()) {
                return dir
            }
        } catch (_: Exception) {
            // fallback
        }

        // Fallback to app's external files directory
        val fallback = context.getExternalFilesDir(null) ?: context.filesDir
        if (!fallback.exists()) fallback.mkdirs()
        return fallback
    }

    private fun startDownloadJob(id: String, downloadUrl: String, targetFile: File) {
        val job = scope.launch {
            try {
                downloadDao.updateStatus(id, DownloadStatus.DOWNLOADING.name)
                updateLiveProgress(id, 0, targetFile.length(), 0, 0, 0f, DownloadStatus.DOWNLOADING)

                val requestBuilder = Request.Builder().url(downloadUrl)
                var existingBytes = 0L
                if (targetFile.exists() && targetFile.length() > 0) {
                    existingBytes = targetFile.length()
                    requestBuilder.addHeader("Range", "bytes=$existingBytes-")
                }

                val response = client.newCall(requestBuilder.build()).execute()
                if (!response.isSuccessful && response.code != 206) {
                    throw IllegalStateException("Server returned HTTP error ${response.code}: ${response.message}")
                }

                val body = response.body ?: throw IllegalStateException("Empty response body")
                val responseLength = body.contentLength()
                val totalBytes = if (responseLength > 0) {
                    if (existingBytes > 0) existingBytes + responseLength else responseLength
                } else {
                    existingBytes + 30_000_000L // Estimate if chunked
                }

                val append = existingBytes > 0 && response.code == 206
                var downloadedBytes = if (append) existingBytes else 0L

                val inputStream: InputStream = body.byteStream()
                val outputStream = FileOutputStream(targetFile, append)

                val buffer = ByteArray(32 * 1024)
                var bytesRead: Int
                var lastDbUpdate = System.currentTimeMillis()
                var speedSampleBytes = 0L
                var speedSampleTime = System.currentTimeMillis()
                var currentSpeed = 0L
                var currentEta = 0L

                outputStream.use { out ->
                    inputStream.use { inStream ->
                        while (inStream.read(buffer).also { bytesRead = it } != -1) {
                            out.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            speedSampleBytes += bytesRead

                            val now = System.currentTimeMillis()
                            val speedDuration = now - speedSampleTime
                            if (speedDuration >= 800) {
                                currentSpeed = (speedSampleBytes * 1000L) / speedDuration.coerceAtLeast(1)
                                val remainingBytes = (totalBytes - downloadedBytes).coerceAtLeast(0)
                                currentEta = if (currentSpeed > 0) remainingBytes / currentSpeed else 0L
                                speedSampleBytes = 0
                                speedSampleTime = now
                            }

                            val percent = if (totalBytes > 0) {
                                (downloadedBytes.toFloat() / totalBytes.toFloat() * 100f).coerceIn(0f, 100f)
                            } else 0f

                            // Update live state for smooth UI
                            updateLiveProgress(
                                id = id,
                                downloaded = downloadedBytes,
                                total = totalBytes,
                                speed = currentSpeed,
                                eta = currentEta,
                                pct = percent,
                                status = DownloadStatus.DOWNLOADING
                            )

                            // Batch DB updates every 500ms to avoid SQLite lock thrashing
                            if (now - lastDbUpdate >= 500) {
                                downloadDao.updateProgress(
                                    id = id,
                                    downloaded = downloadedBytes,
                                    total = totalBytes,
                                    speed = currentSpeed,
                                    eta = currentEta,
                                    status = DownloadStatus.DOWNLOADING.name
                                )
                                lastDbUpdate = now
                            }
                        }
                        out.flush()
                    }
                }

                // Completed successfully!
                downloadDao.updateProgress(
                    id = id,
                    downloaded = downloadedBytes,
                    total = downloadedBytes,
                    speed = 0,
                    eta = 0,
                    status = DownloadStatus.COMPLETED.name
                )
                downloadDao.updateStatus(
                    id = id,
                    status = DownloadStatus.COMPLETED.name,
                    completedAt = System.currentTimeMillis()
                )

                updateLiveProgress(
                    id = id,
                    downloaded = downloadedBytes,
                    total = downloadedBytes,
                    speed = 0,
                    eta = 0,
                    pct = 100f,
                    status = DownloadStatus.COMPLETED
                )

                // Scan media file so it appears in Gallery/Videos
                try {
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(targetFile.absolutePath),
                        null
                    ) { _, _ -> }
                } catch (_: Exception) {}

            } catch (e: CancellationException) {
                downloadDao.updateStatus(id, DownloadStatus.PAUSED.name)
                updateLiveProgress(id, 0, 0, 0, 0, 0f, DownloadStatus.PAUSED)
            } catch (e: Exception) {
                downloadDao.updateStatus(
                    id = id,
                    status = DownloadStatus.FAILED.name,
                    errorMessage = e.localizedMessage ?: "Download failed"
                )
                updateLiveProgress(id, 0, 0, 0, 0, 0f, DownloadStatus.FAILED)
            } finally {
                activeJobs.remove(id)
            }
        }

        activeJobs[id] = job
    }

    private fun updateLiveProgress(
        id: String,
        downloaded: Long,
        total: Long,
        speed: Long,
        eta: Long,
        pct: Float,
        status: DownloadStatus
    ) {
        val current = _liveProgress.value.toMutableMap()
        current[id] = LiveDownloadProgress(
            id = id,
            downloadedBytes = downloaded,
            totalBytes = total,
            speedBytesPerSec = speed,
            etaSeconds = eta,
            percent = pct,
            status = status
        )
        _liveProgress.value = current
    }

    fun pauseDownload(id: String) {
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        scope.launch {
            downloadDao.updateStatus(id, DownloadStatus.PAUSED.name)
        }
    }

    fun resumeDownload(id: String) {
        scope.launch {
            val entity = downloadDao.getById(id) ?: return@launch
            startDownloadJob(id, entity.url, File(entity.filePath))
        }
    }

    fun cancelDownload(id: String) {
        activeJobs[id]?.cancel()
        activeJobs.remove(id)
        scope.launch {
            val entity = downloadDao.getById(id)
            if (entity != null) {
                try {
                    File(entity.filePath).delete()
                } catch (_: Exception) {}
                downloadDao.deleteById(id)
            }
            val current = _liveProgress.value.toMutableMap()
            current.remove(id)
            _liveProgress.value = current
        }
    }

    suspend fun deleteDownloadRecord(id: String, deleteFromDisk: Boolean) = withContext(Dispatchers.IO) {
        val entity = downloadDao.getById(id)
        if (entity != null && deleteFromDisk) {
            try {
                File(entity.filePath).delete()
            } catch (_: Exception) {}
        }
        downloadDao.deleteById(id)
        val current = _liveProgress.value.toMutableMap()
        current.remove(id)
        _liveProgress.value = current
    }

    suspend fun clearCompleted() = withContext(Dispatchers.IO) {
        downloadDao.clearCompleted()
    }
}
