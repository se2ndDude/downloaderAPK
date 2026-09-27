package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.FormatOption
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberGradientButton
import com.example.ui.components.ExtractorBadge
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VividPurple
import com.example.ui.viewmodel.MediaViewModel
import com.example.util.FormatUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: MediaViewModel,
    onNavigateToActive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val urlInput by viewModel.urlInput.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analyzedMedia by viewModel.analyzedMedia.collectAsStateWithLifecycle()
    val selectedFormat by viewModel.selectedFormat.collectAsStateWithLifecycle()
    val analysisError by viewModel.analysisError.collectAsStateWithLifecycle()
    val detectedClipboardUrl by viewModel.detectedClipboardUrl.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // App Header with Glowing Logo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CyberBlue, NeonPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "MediaDL",
                    tint = DeepBlack,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "MediaDL",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "High-Speed yt-dl Media Downloader",
                    fontSize = 12.sp,
                    color = ElectricBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Clipboard quick detection banner
        AnimatedVisibility(visible = detectedClipboardUrl != null) {
            CyberCard(
                borderColor = CyanAccent.copy(alpha = 0.6f),
                backgroundColor = Color(0xFF0F1B29),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Link detected from clipboard",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanAccent
                        )
                        Text(
                            text = detectedClipboardUrl ?: "",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyanAccent)
                            .clickable { viewModel.applyDetectedUrl() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Paste & Fetch", color = DeepBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Input Card
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Enter Video / Audio URL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Supports YouTube, TikTok, Instagram, Twitter, Direct MP4/MP3",
                    fontSize = 12.sp,
                    color = TextTertiary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { viewModel.updateUrlInput(it) },
                    placeholder = {
                        Text(
                            "Paste video link here...",
                            color = TextTertiary,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("url_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberBlue,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = CyanAccent,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    ),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (urlInput.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.updateUrlInput("") },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                    if (!clip.isNullOrBlank()) {
                                        viewModel.updateUrlInput(clip)
                                        viewModel.analyzeUrl(clip)
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("paste_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste",
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    CyberGradientButton(
                        text = if (isAnalyzing) "Analyzing..." else "Analyze Link",
                        icon = if (isAnalyzing) null else Icons.Default.Search,
                        onClick = { viewModel.analyzeUrl() },
                        enabled = !isAnalyzing && urlInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "analyze_button"
                    )
                }

                // Storage destination hint
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Saving to: ",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                    Text(
                        text = settingsState.downloadPath.takeLast(35),
                        fontSize = 11.sp,
                        color = ElectricBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Analysis Loading Indicator
        AnimatedVisibility(visible = isAnalyzing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = NeonPurple,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Connecting to yt-dl extractor...",
                    fontSize = 13.sp,
                    color = ElectricBlue
                )
            }
        }

        // Analysis Error
        AnimatedVisibility(visible = analysisError != null) {
            CyberCard(
                borderColor = ErrorRed.copy(alpha = 0.5f),
                backgroundColor = Color(0xFF261014),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = analysisError ?: "",
                        color = ErrorRed,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Analyzed Media Metadata Card
        AnimatedVisibility(visible = analyzedMedia != null && !isAnalyzing) {
            analyzedMedia?.let { media ->
                Spacer(modifier = Modifier.height(16.dp))
                CyberCard(
                    borderColor = NeonPurple.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Thumbnail & Duration
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceElevated)
                        ) {
                            if (!media.thumbnailUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = media.thumbnailUrl,
                                    contentDescription = media.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF1E2135), Color(0xFF161828))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }

                            // Extractor badge top left
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                            ) {
                                ExtractorBadge(extractor = media.extractor)
                            }

                            // Duration badge bottom right
                            if (media.durationSeconds > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DeepBlack.copy(alpha = 0.8f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = FormatUtils.formatDuration(media.durationSeconds),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Title
                        Text(
                            text = media.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Uploader
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = media.uploader,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Format / Quality Chips
                        Text(
                            text = "Select Quality / Format:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            media.formats.forEach { format ->
                                val isSelected = selectedFormat?.id == format.id
                                FormatChip(
                                    format = format,
                                    isSelected = isSelected,
                                    onClick = { viewModel.selectFormat(format) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Download Button
                        CyberGradientButton(
                            text = "Start Download Now",
                            icon = Icons.Default.Download,
                            onClick = {
                                if (viewModel.startDownload()) {
                                    onNavigateToActive()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "start_download_button"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Demo Links for Instant Real Testing
        Text(
            text = "Quick Demo Test Media",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
        )
        Text(
            text = "Tap any test video to test download speed and saving:",
            fontSize = 12.sp,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.height(10.dp))

        val demoLinks = listOf(
            DemoLink(
                name = "Big Buck Bunny (1080p FHD)",
                desc = "Open-source animated film sample (MP4)",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                icon = Icons.Default.Movie
            ),
            DemoLink(
                name = "Tears of Steel (Sci-Fi 720p)",
                desc = "Blender VFX Sci-Fi short film (MP4)",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                icon = Icons.Default.Movie
            ),
            DemoLink(
                name = "Elephant's Dream (Audio / Video)",
                desc = "Open movie stream demonstration",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                icon = Icons.Default.PlayArrow
            )
        )

        demoLinks.forEach { demo ->
            DemoCard(demo = demo) {
                viewModel.updateUrlInput(demo.url)
                viewModel.analyzeUrl(demo.url)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

data class DemoLink(
    val name: String,
    val desc: String,
    val url: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun DemoCard(
    demo: DemoLink,
    onClick: () -> Unit
) {
    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = SurfaceElevated
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF282C4A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = demo.icon,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = demo.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = demo.desc,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Test",
                tint = ElectricBlue,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun FormatChip(
    format: FormatOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) NeonPurple else SurfaceBorder
    val bgColor = if (isSelected) Color(0xFF2B1948) else SurfaceElevated

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (format.isAudioOnly) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isSelected) NeonPurple else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = format.label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) TextPrimary else TextSecondary
                )
            }
            Text(
                text = "${format.resolution} • ${FormatUtils.formatBytes(format.estimatedSizeBytes)}",
                fontSize = 10.sp,
                color = if (isSelected) ElectricBlue else TextTertiary
            )
        }
    }
}
