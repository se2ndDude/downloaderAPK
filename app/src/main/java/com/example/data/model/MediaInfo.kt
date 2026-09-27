package com.example.data.model

data class FormatOption(
    val id: String,
    val label: String,
    val resolution: String,
    val extension: String,
    val estimatedSizeBytes: Long,
    val isAudioOnly: Boolean,
    val downloadUrl: String
)

data class MediaInfo(
    val url: String,
    val title: String,
    val uploader: String,
    val extractor: String,
    val durationSeconds: Long,
    val thumbnailUrl: String?,
    val formats: List<FormatOption>,
    val isCarousel: Boolean = false,
    val itemCount: Int = 1
)
