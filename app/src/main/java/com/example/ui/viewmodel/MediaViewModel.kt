package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.DownloadEntity
import com.example.data.model.FormatOption
import com.example.data.model.MediaInfo
import com.example.data.repository.SettingsRepository
import com.example.data.repository.SettingsState
import com.example.service.DownloadManagerService
import com.example.service.LiveDownloadProgress
import com.example.service.MediaExtractorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val settingsRepo = SettingsRepository(application)
    private val extractorService = MediaExtractorService()
    val downloadManager = DownloadManagerService(application, database)

    val settingsState: StateFlow<SettingsState> = settingsRepo.settingsState

    val activeDownloads: StateFlow<List<DownloadEntity>> = database.downloadDao()
        .getActiveDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedDownloads: StateFlow<List<DownloadEntity>> = database.downloadDao()
        .getCompletedDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val liveProgress: StateFlow<Map<String, LiveDownloadProgress>> = downloadManager.liveProgress

    // UI state for Downloader
    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analyzedMedia = MutableStateFlow<MediaInfo?>(null)
    val analyzedMedia: StateFlow<MediaInfo?> = _analyzedMedia.asStateFlow()

    private val _selectedFormat = MutableStateFlow<FormatOption?>(null)
    val selectedFormat: StateFlow<FormatOption?> = _selectedFormat.asStateFlow()

    private val _analysisError = MutableStateFlow<String?>(null)
    val analysisError: StateFlow<String?> = _analysisError.asStateFlow()

    private val _detectedClipboardUrl = MutableStateFlow<String?>(null)
    val detectedClipboardUrl: StateFlow<String?> = _detectedClipboardUrl.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun updateUrlInput(url: String) {
        _urlInput.value = url
        if (url.isBlank()) {
            _analyzedMedia.value = null
            _selectedFormat.value = null
            _analysisError.value = null
        }
    }

    fun setClipboardDetectedUrl(url: String?) {
        if (settingsState.value.autoClipboard) {
            _detectedClipboardUrl.value = url
        }
    }

    fun applyDetectedUrl() {
        val url = _detectedClipboardUrl.value ?: return
        _urlInput.value = url
        _detectedClipboardUrl.value = null
        analyzeUrl(url)
    }

    fun dismissDetectedUrl() {
        _detectedClipboardUrl.value = null
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun analyzeUrl(urlToAnalyze: String = _urlInput.value) {
        val target = urlToAnalyze.trim()
        if (target.isEmpty()) {
            _analysisError.value = "Please enter a valid video or media URL"
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisError.value = null
            val result = extractorService.extractInfo(target)
            result.onSuccess { info ->
                _analyzedMedia.value = info
                // Automatically match default quality
                val prefQuality = settingsState.value.defaultQuality
                val matched = info.formats.find { it.label.contains(prefQuality, ignoreCase = true) }
                    ?: info.formats.firstOrNull()
                _selectedFormat.value = matched
                _analysisError.value = null
            }.onFailure { error ->
                _analysisError.value = error.localizedMessage ?: "Failed to extract video information"
                _analyzedMedia.value = null
            }
            _isAnalyzing.value = false
        }
    }

    fun selectFormat(format: FormatOption) {
        _selectedFormat.value = format
    }

    fun startDownload(): Boolean {
        val media = _analyzedMedia.value ?: return false
        val format = _selectedFormat.value ?: media.formats.firstOrNull() ?: return false

        val currentSettings = settingsState.value
        val saveDir = currentSettings.downloadPath
        val namingTemplate = currentSettings.namingTemplate

        downloadManager.enqueueDownload(
            mediaInfo = media,
            format = format,
            saveDir = saveDir,
            namingTemplate = namingTemplate
        )

        _userMessage.value = "Download started for: ${media.title.take(30)}..."
        return true
    }

    fun pauseDownload(id: String) {
        downloadManager.pauseDownload(id)
    }

    fun resumeDownload(id: String) {
        downloadManager.resumeDownload(id)
    }

    fun cancelDownload(id: String) {
        downloadManager.cancelDownload(id)
    }

    fun deleteCompleted(id: String, deleteFile: Boolean) {
        viewModelScope.launch {
            downloadManager.deleteDownloadRecord(id, deleteFile)
        }
    }

    fun clearAllCompleted() {
        viewModelScope.launch {
            downloadManager.clearCompleted()
        }
    }

    fun updateDownloadPath(newPath: String): Boolean {
        val success = settingsRepo.setDownloadPath(newPath)
        _userMessage.value = if (success) {
            "Download directory updated successfully!"
        } else {
            "Directory created. Note: Ensure storage permission is allowed."
        }
        return success
    }

    fun updateNamingTemplate(template: String) {
        settingsRepo.setNamingTemplate(template)
    }

    fun updateDefaultQuality(quality: String) {
        settingsRepo.setDefaultQuality(quality)
    }

    fun updateWifiOnly(enabled: Boolean) {
        settingsRepo.setWifiOnly(enabled)
    }

    fun updateMaxConcurrent(count: Int) {
        settingsRepo.setMaxConcurrent(count)
    }

    fun updateAutoClipboard(enabled: Boolean) {
        settingsRepo.setAutoClipboard(enabled)
    }
}
