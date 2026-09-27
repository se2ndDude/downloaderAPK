package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class SettingsState(
    val downloadPath: String,
    val namingTemplate: String = "%(uploader)s_%(id)s.%(ext)s",
    val defaultQuality: String = "Best Quality",
    val wifiOnly: Boolean = false,
    val maxConcurrent: Int = 3,
    val autoClipboard: Boolean = true,
    val isPathWritable: Boolean = true,
    val freeSpaceBytes: Long = 0L,
    val totalSpaceBytes: Long = 0L
)

class SettingsRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mediadl_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DOWNLOAD_PATH = "download_path"
        private const val KEY_NAMING_TEMPLATE = "naming_template"
        private const val KEY_DEFAULT_QUALITY = "default_quality"
        private const val KEY_WIFI_ONLY = "wifi_only"
        private const val KEY_MAX_CONCURRENT = "max_concurrent"
        private const val KEY_AUTO_CLIPBOARD = "auto_clipboard"

        val NAMING_PRESETS = listOf(
            "%(uploader)s_%(id)s.%(ext)s" to "Uploader & Video ID (yt-dl default)",
            "%(title)s.%(ext)s" to "Video Title Only",
            "%(uploader)s - %(title)s.%(ext)s" to "Uploader - Video Title",
            "%(id)s_%(title)s.%(ext)s" to "ID & Video Title",
            "MediaDL_%(timestamp)s.%(ext)s" to "MediaDL Timestamp"
        )
    }

    private val _settingsState = MutableStateFlow(loadSettings())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    fun getDefaultDownloadPath(): String {
        val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return try {
            if (publicDownloads.exists() || publicDownloads.mkdirs()) {
                publicDownloads.absolutePath
            } else {
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
                    ?: context.filesDir.absolutePath
            }
        } catch (e: Exception) {
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
                ?: context.filesDir.absolutePath
        }
    }

    fun getPresetPaths(): List<Pair<String, String>> {
        val baseStorage = "/storage/emulated/0"
        return listOf(
            "$baseStorage/Download" to "Downloads (Standard)",
            "$baseStorage/Download/MediaDL" to "MediaDL Folder",
            "$baseStorage/Movies" to "Movies",
            "$baseStorage/Music" to "Music",
            (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
                ?: context.filesDir.absolutePath) to "App Sandboxed Storage"
        )
    }

    private fun loadSettings(): SettingsState {
        val defaultPath = getDefaultDownloadPath()
        val path = prefs.getString(KEY_DOWNLOAD_PATH, defaultPath) ?: defaultPath
        val naming = prefs.getString(KEY_NAMING_TEMPLATE, "%(uploader)s_%(id)s.%(ext)s")
            ?: "%(uploader)s_%(id)s.%(ext)s"
        val quality = prefs.getString(KEY_DEFAULT_QUALITY, "Best Quality") ?: "Best Quality"
        val wifiOnly = prefs.getBoolean(KEY_WIFI_ONLY, false)
        val maxConcurrent = prefs.getInt(KEY_MAX_CONCURRENT, 3)
        val autoClipboard = prefs.getBoolean(KEY_AUTO_CLIPBOARD, true)

        val dir = File(path)
        var writable = false
        var freeBytes = 0L
        var totalBytes = 0L
        try {
            if (!dir.exists()) dir.mkdirs()
            writable = dir.canWrite()
            freeBytes = dir.usableSpace
            totalBytes = dir.totalSpace
        } catch (_: Exception) {
            writable = false
        }

        return SettingsState(
            downloadPath = path,
            namingTemplate = naming,
            defaultQuality = quality,
            wifiOnly = wifiOnly,
            maxConcurrent = maxConcurrent,
            autoClipboard = autoClipboard,
            isPathWritable = writable,
            freeSpaceBytes = freeBytes,
            totalSpaceBytes = totalBytes
        )
    }

    fun setDownloadPath(newPath: String): Boolean {
        val trimmed = newPath.trim()
        val dir = File(trimmed)
        var success = false
        try {
            if (!dir.exists()) {
                dir.mkdirs()
            }
            success = dir.exists() && (dir.canWrite() || dir.isDirectory)
        } catch (e: Exception) {
            success = false
        }

        prefs.edit().putString(KEY_DOWNLOAD_PATH, trimmed).apply()
        refreshSettings()
        return success
    }

    fun setNamingTemplate(template: String) {
        prefs.edit().putString(KEY_NAMING_TEMPLATE, template).apply()
        refreshSettings()
    }

    fun setDefaultQuality(quality: String) {
        prefs.edit().putString(KEY_DEFAULT_QUALITY, quality).apply()
        refreshSettings()
    }

    fun setWifiOnly(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIFI_ONLY, enabled).apply()
        refreshSettings()
    }

    fun setMaxConcurrent(count: Int) {
        prefs.edit().putInt(KEY_MAX_CONCURRENT, count.coerceIn(1, 5)).apply()
        refreshSettings()
    }

    fun setAutoClipboard(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CLIPBOARD, enabled).apply()
        refreshSettings()
    }

    fun refreshSettings() {
        _settingsState.value = loadSettings()
    }
}
