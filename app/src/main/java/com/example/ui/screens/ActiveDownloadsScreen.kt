package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.DownloadEntity
import com.example.data.model.DownloadStatus
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberProgressBar
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.MediaViewModel
import com.example.util.FormatUtils

@Composable
fun ActiveDownloadsScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val activeDownloads by viewModel.activeDownloads.collectAsStateWithLifecycle()
    val liveProgressMap by viewModel.liveProgress.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Active Queue",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${activeDownloads.size} items in progress",
                    fontSize = 12.sp,
                    color = ElectricBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeDownloads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Active Downloads",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Paste a video link in the Downloader tab to start",
                        fontSize = 13.sp,
                        color = TextTertiary
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(activeDownloads, key = { it.id }) { item ->
                    val live = liveProgressMap[item.id]

                    val downloaded = live?.downloadedBytes ?: item.downloadedBytes
                    val total = live?.totalBytes ?: item.totalBytes
                    val speed = live?.speedBytesPerSec ?: item.speedBytesPerSec
                    val eta = live?.etaSeconds ?: item.etaSeconds
                    val percent = live?.percent ?: item.progressPercent
                    val currentStatus = live?.status?.name ?: item.status

                    ActiveDownloadCard(
                        item = item,
                        downloadedBytes = downloaded,
                        totalBytes = total,
                        speedBytes = speed,
                        etaSeconds = eta,
                        progressPercent = percent,
                        status = currentStatus,
                        onPause = { viewModel.pauseDownload(item.id) },
                        onResume = { viewModel.resumeDownload(item.id) },
                        onCancel = { viewModel.cancelDownload(item.id) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

private val ElectricBlue = Color(0xFF60A5FA)

@Composable
fun ActiveDownloadCard(
    item: DownloadEntity,
    downloadedBytes: Long,
    totalBytes: Long,
    speedBytes: Long,
    etaSeconds: Long,
    progressPercent: Float,
    status: String,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit
) {
    val isPaused = status == DownloadStatus.PAUSED.name

    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_download_item_${item.id}"),
        borderColor = if (isPaused) WarningAmber.copy(alpha = 0.5f) else CyberBlue.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Icon, Title, Status & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isAudio) Icons.Default.MusicNote else Icons.Default.Movie,
                        contentDescription = null,
                        tint = if (item.isAudio) NeonPurple else CyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${item.uploader} • ${item.formatLabel}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPaused) {
                        IconButton(
                            onClick = onResume,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                tint = CyanAccent
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onPause,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = WarningAmber
                            )
                        }
                    }

                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = ErrorRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            CyberProgressBar(progressPercent = progressPercent, height = 8.dp)

            Spacer(modifier = Modifier.height(10.dp))

            // Exact yt-dl stats line:
            // [%] downloaded/total  speed/s  ETA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Percentage badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPaused) Color(0xFF332A15) else Color(0xFF16243E))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = String.format("%.1f%%", progressPercent),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaused) WarningAmber else ElectricBlue
                    )
                }

                // Downloaded / Total
                Text(
                    text = "${FormatUtils.formatBytes(downloadedBytes)} / ${FormatUtils.formatBytes(totalBytes)}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )

                // Speed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isPaused) "Paused" else FormatUtils.formatSpeed(speedBytes),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isPaused) TextTertiary else CyanAccent
                    )
                }

                // ETA
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isPaused) "--" else "ETA ${FormatUtils.formatEta(etaSeconds)}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isPaused) TextTertiary else NeonPurple
                    )
                }
            }
        }
    }
}
