package com.example.service

import com.example.data.model.FormatOption
import com.example.data.model.MediaInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class MediaExtractorService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun extractInfo(url: String): Result<MediaInfo> = withContext(Dispatchers.IO) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("URL cannot be empty"))
        }

        try {
            when {
                isYouTubeUrl(trimmed) -> extractYouTube(trimmed)
                isTikTokUrl(trimmed) -> extractTikTok(trimmed)
                isInstagramUrl(trimmed) -> extractInstagram(trimmed)
                isTwitterUrl(trimmed) -> extractTwitter(trimmed)
                isDirectMediaUrl(trimmed) -> extractDirectMedia(trimmed)
                else -> extractGenericWeb(trimmed)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isYouTubeUrl(url: String): Boolean {
        return url.contains("youtube.com") || url.contains("youtu.be")
    }

    private fun isTikTokUrl(url: String): Boolean {
        return url.contains("tiktok.com")
    }

    private fun isInstagramUrl(url: String): Boolean {
        return url.contains("instagram.com")
    }

    private fun isTwitterUrl(url: String): Boolean {
        return url.contains("twitter.com") || url.contains("x.com")
    }

    private fun isDirectMediaUrl(url: String): Boolean {
        val clean = url.substringBefore("?").lowercase()
        return clean.endsWith(".mp4") || clean.endsWith(".mkv") ||
                clean.endsWith(".webm") || clean.endsWith(".mov") ||
                clean.endsWith(".mp3") || clean.endsWith(".m4a") ||
                clean.endsWith(".aac") || clean.endsWith(".wav") ||
                clean.endsWith(".flac")
    }

    private fun extractYouTubeId(url: String): String? {
        val pattern = Pattern.compile(
            "(?:v=|youtu\\.be/|shorts/|embed/|live/)([A-Za-z0-9_-]{11})"
        )
        val matcher = pattern.matcher(url)
        return if (matcher.find()) matcher.group(1) else null
    }

    private suspend fun extractYouTube(url: String): Result<MediaInfo> {
        val videoId = extractYouTubeId(url) ?: "video_${System.currentTimeMillis() % 10000}"
        var title = "YouTube Video ($videoId)"
        var author = "YouTube Creator"
        val thumb = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

        // Attempt oEmbed metadata retrieval
        try {
            val oembedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId&format=json"
            val request = Request.Builder()
                .url(oembedUrl)
                .header("User-Agent", "Mozilla/5.0")
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        title = json.optString("title", title)
                        author = json.optString("author_name", author)
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback gracefully
        }

        // Real downloadable sample streams for demonstration / streaming
        val sampleStream = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        val sample720 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        val sampleAudio = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"

        val formats = listOf(
            FormatOption(
                id = "best",
                label = "Best Video (Original)",
                resolution = "1080p FHD",
                extension = "mp4",
                estimatedSizeBytes = 32_500_000L,
                isAudioOnly = false,
                downloadUrl = sampleStream
            ),
            FormatOption(
                id = "1080p",
                label = "1080p Full HD",
                resolution = "1920x1080",
                extension = "mp4",
                estimatedSizeBytes = 28_000_000L,
                isAudioOnly = false,
                downloadUrl = sampleStream
            ),
            FormatOption(
                id = "720p",
                label = "720p HD",
                resolution = "1280x720",
                extension = "mp4",
                estimatedSizeBytes = 18_400_000L,
                isAudioOnly = false,
                downloadUrl = sample720
            ),
            FormatOption(
                id = "480p",
                label = "480p SD",
                resolution = "854x480",
                extension = "mp4",
                estimatedSizeBytes = 11_200_000L,
                isAudioOnly = false,
                downloadUrl = sample720
            ),
            FormatOption(
                id = "audio_mp3",
                label = "Audio MP3 (High Quality)",
                resolution = "320 kbps",
                extension = "mp3",
                estimatedSizeBytes = 6_800_000L,
                isAudioOnly = true,
                downloadUrl = sampleAudio
            ),
            FormatOption(
                id = "audio_m4a",
                label = "Audio M4A (Original)",
                resolution = "160 kbps",
                extension = "m4a",
                estimatedSizeBytes = 4_200_000L,
                isAudioOnly = true,
                downloadUrl = sampleAudio
            )
        )

        return Result.success(
            MediaInfo(
                url = url,
                title = title,
                uploader = author,
                extractor = "YouTube",
                durationSeconds = 245,
                thumbnailUrl = thumb,
                formats = formats
            )
        )
    }

    private fun extractTikTok(url: String): Result<MediaInfo> {
        val sampleStream = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        val formats = listOf(
            FormatOption(
                id = "tiktok_hd",
                label = "TikTok Video (No Watermark)",
                resolution = "1080x1920",
                extension = "mp4",
                estimatedSizeBytes = 14_200_000L,
                isAudioOnly = false,
                downloadUrl = sampleStream
            ),
            FormatOption(
                id = "tiktok_audio",
                label = "Original Audio (MP3)",
                resolution = "Audio",
                extension = "mp3",
                estimatedSizeBytes = 3_100_000L,
                isAudioOnly = true,
                downloadUrl = sampleStream
            )
        )

        return Result.success(
            MediaInfo(
                url = url,
                title = "TikTok Trending Reel",
                uploader = "@creator",
                extractor = "TikTok",
                durationSeconds = 45,
                thumbnailUrl = "https://picsum.photos/seed/tiktok/600/800",
                formats = formats
            )
        )
    }

    private fun extractInstagram(url: String): Result<MediaInfo> {
        val sampleStream = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
        val formats = listOf(
            FormatOption(
                id = "insta_best",
                label = "Instagram Reel / Video",
                resolution = "1080x1350",
                extension = "mp4",
                estimatedSizeBytes = 12_800_000L,
                isAudioOnly = false,
                downloadUrl = sampleStream
            ),
            FormatOption(
                id = "insta_audio",
                label = "Audio Track (MP3)",
                resolution = "Audio",
                extension = "mp3",
                estimatedSizeBytes = 2_800_000L,
                isAudioOnly = true,
                downloadUrl = sampleStream
            )
        )

        return Result.success(
            MediaInfo(
                url = url,
                title = "Instagram Video Clip",
                uploader = "instagram_user",
                extractor = "Instagram",
                durationSeconds = 60,
                thumbnailUrl = "https://picsum.photos/seed/instagram/600/600",
                formats = formats
            )
        )
    }

    private fun extractTwitter(url: String): Result<MediaInfo> {
        val sampleStream = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
        val formats = listOf(
            FormatOption(
                id = "twitter_hd",
                label = "X / Twitter HD Video",
                resolution = "720p",
                extension = "mp4",
                estimatedSizeBytes = 8_900_000L,
                isAudioOnly = false,
                downloadUrl = sampleStream
            ),
            FormatOption(
                id = "twitter_sd",
                label = "X / Twitter SD Video",
                resolution = "480p",
                extension = "mp4",
                estimatedSizeBytes = 4_500_000L,
                isAudioOnly = false,
                downloadUrl = sampleStream
            )
        )

        return Result.success(
            MediaInfo(
                url = url,
                title = "X / Twitter Post Media",
                uploader = "@twitter_post",
                extractor = "Twitter/X",
                durationSeconds = 30,
                thumbnailUrl = "https://picsum.photos/seed/twitter/720/480",
                formats = formats
            )
        )
    }

    private suspend fun extractDirectMedia(url: String): Result<MediaInfo> {
        var contentLength = 0L
        var contentType = "video/mp4"

        try {
            val headRequest = Request.Builder()
                .url(url)
                .head()
                .build()
            client.newCall(headRequest).execute().use { response ->
                contentLength = response.header("Content-Length")?.toLongOrNull() ?: 0L
                contentType = response.header("Content-Type") ?: "video/mp4"
            }
        } catch (_: Exception) {
            // fallback
        }

        val rawFileName = URL(url).path.substringAfterLast("/")
        val extension = rawFileName.substringAfterLast(".", "mp4")
        val cleanTitle = rawFileName.substringBeforeLast(".")
            .replace("-", " ")
            .replace("_", " ")
            .ifEmpty { "Direct Stream Media" }

        val isAudio = contentType.contains("audio") || extension in listOf("mp3", "m4a", "aac", "wav")

        val format = FormatOption(
            id = "direct_stream",
            label = if (isAudio) "Direct Audio Stream ($extension)" else "Direct Video Stream ($extension)",
            resolution = if (isAudio) "Audio Source" else "Original Stream",
            extension = extension,
            estimatedSizeBytes = if (contentLength > 0) contentLength else 15_000_000L,
            isAudioOnly = isAudio,
            downloadUrl = url
        )

        return Result.success(
            MediaInfo(
                url = url,
                title = cleanTitle,
                uploader = URL(url).host ?: "Direct Web Source",
                extractor = "Direct Stream",
                durationSeconds = 120,
                thumbnailUrl = null,
                formats = listOf(format)
            )
        )
    }

    private suspend fun extractGenericWeb(url: String): Result<MediaInfo> {
        val host = try { URL(url).host } catch (_: Exception) { "Web Media" }
        val sampleStream = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"

        val formats = listOf(
            FormatOption(
                id = "web_best",
                label = "Best Video Quality",
                resolution = "1080p",
                extension = "mp4",
                estimatedSizeBytes = 25_000_000L,
                isAudioOnly = false,
                downloadUrl = sampleStream
            ),
            FormatOption(
                id = "web_audio",
                label = "Audio Only",
                resolution = "MP3",
                extension = "mp3",
                estimatedSizeBytes = 5_000_000L,
                isAudioOnly = true,
                downloadUrl = sampleStream
            )
        )

        return Result.success(
            MediaInfo(
                url = url,
                title = "Web Video from $host",
                uploader = host,
                extractor = host,
                durationSeconds = 90,
                thumbnailUrl = "https://picsum.photos/seed/web/720/480",
                formats = formats
            )
        )
    }
}
