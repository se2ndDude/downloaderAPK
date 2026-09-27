package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.size - 1) {
            value /= 1024
            unitIndex++
        }
        return String.format(Locale.US, "%.2f %s", value, units[unitIndex])
    }

    fun formatSpeed(bytesPerSec: Long): String {
        return "${formatBytes(bytesPerSec)}/s"
    }

    fun formatEta(seconds: Long): String {
        if (seconds <= 0) return "--"
        if (seconds < 60) return "${seconds}s"
        val minutes = seconds / 60
        val remainingSecs = seconds % 60
        if (minutes < 60) {
            return "${minutes}m ${remainingSecs}s"
        }
        val hours = minutes / 60
        val remMin = minutes % 60
        return "${hours}h ${remMin}m"
    }

    fun formatDuration(seconds: Long): String {
        if (seconds <= 0) return "00:00"
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, secs)
        }
    }

    fun generateFileName(
        template: String,
        uploader: String,
        id: String,
        title: String,
        ext: String
    ): String {
        val safeUploader = sanitizeFileName(uploader.ifBlank { "unknown" })
        val safeId = sanitizeFileName(id.ifBlank { System.currentTimeMillis().toString() })
        val safeTitle = sanitizeFileName(title.ifBlank { "media" })
        val safeExt = sanitizeFileName(ext.ifBlank { "mp4" }).removePrefix(".")
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        var result = template
            .replace("%(uploader,channel,id)s", safeUploader)
            .replace("%(uploader)s", safeUploader)
            .replace("%(channel)s", safeUploader)
            .replace("%(id)s", safeId)
            .replace("%(title)s", safeTitle)
            .replace("%(timestamp)s", timestamp)
            .replace("%(ext)s", safeExt)

        if (!result.endsWith(".$safeExt", ignoreCase = true)) {
            result = "$result.$safeExt"
        }
        return result
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .trim()
            .take(120)
    }
}
