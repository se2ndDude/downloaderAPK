package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DownloadStatus

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val uploader: String,
    val extractor: String,
    val durationSeconds: Long = 0,
    val thumbnailUrl: String? = null,
    val formatLabel: String,
    val extension: String,
    val filePath: String,
    val fileName: String,
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val speedBytesPerSec: Long = 0,
    val etaSeconds: Long = 0,
    val status: String = DownloadStatus.QUEUED.name,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isAudio: Boolean = false
) {
    val progressPercent: Float
        get() = if (totalBytes > 0) {
            (downloadedBytes.toFloat() / totalBytes.toFloat() * 100f).coerceIn(0f, 100f)
        } else {
            0f
        }
}
