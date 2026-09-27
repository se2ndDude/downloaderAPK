package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.SettingsRepository
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberGradientButton
import com.example.ui.components.CyberProgressBar
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.MediaViewModel
import com.example.util.FormatUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: MediaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()

    var showPathDialog by remember { mutableStateOf(false) }
    var tempPathInput by remember { mutableStateOf(settings.downloadPath) }
    var showTemplateDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF20233B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Storage path, naming & download engine",
                    fontSize = 12.sp,
                    color = ElectricBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 1: CUSTOMIZABLE DOWNLOAD PATH (CRITICAL REQUIREMENT)
        Text(
            text = "Storage & Download Directory",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = CyanAccent,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        CyberCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("download_path_settings_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B2E48)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Download Location",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = settings.downloadPath,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElectricBlue,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Badge & Free space
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (settings.isPathWritable) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (settings.isPathWritable) SuccessGreen else WarningAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (settings.isPathWritable) "Writable & Active" else "Check Permissions",
                            fontSize = 11.sp,
                            color = if (settings.isPathWritable) SuccessGreen else WarningAmber
                        )
                    }

                    if (settings.freeSpaceBytes > 0) {
                        Text(
                            text = "${FormatUtils.formatBytes(settings.freeSpaceBytes)} Free",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                CyberGradientButton(
                    text = "Change Download Path",
                    icon = Icons.Default.Edit,
                    onClick = {
                        tempPathInput = settings.downloadPath
                        showPathDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "change_download_path_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: STORAGE USAGE GAUGE
        if (settings.totalSpaceBytes > 0) {
            val usedBytes = (settings.totalSpaceBytes - settings.freeSpaceBytes).coerceAtLeast(0)
            val usedPercent = (usedBytes.toFloat() / settings.totalSpaceBytes.toFloat() * 100f).coerceIn(0f, 100f)

            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SdStorage,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Device Storage",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${FormatUtils.formatBytes(settings.freeSpaceBytes)} available of ${FormatUtils.formatBytes(settings.totalSpaceBytes)}",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    CyberProgressBar(progressPercent = usedPercent, height = 6.dp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // SECTION 3: FILE NAMING TEMPLATE
        Text(
            text = "Filename Formatting",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NeonPurple,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        CyberCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTemplateDialog = true }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Output Template (yt-dl)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = settings.namingTemplate,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElectricBlue
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Template",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Live preview of filename
                val previewFilename = FormatUtils.generateFileName(
                    template = settings.namingTemplate,
                    uploader = "creator",
                    id = "dQw4w9WgXcQ",
                    title = "Awesome Video Title",
                    ext = "mp4"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Example: $previewFilename",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 4: PREFERENCES & ENGINE
        Text(
            text = "Download Preferences",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Default quality picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HighQuality,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Default Preferred Quality",
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = settings.defaultQuality,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quality selector chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Best Quality", "1080p", "720p", "Audio MP3").forEach { quality ->
                        val isSelected = settings.defaultQuality == quality
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CyberBlue else SurfaceElevated)
                                .clickable { viewModel.updateDefaultQuality(quality) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = quality,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) DeepBlack else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Wi-Fi only switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Download on Wi-Fi Only",
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Save mobile cellular data",
                                fontSize = 11.sp,
                                color = TextTertiary
                            )
                        }
                    }
                    Switch(
                        checked = settings.wifiOnly,
                        onCheckedChange = { viewModel.updateWifiOnly(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanAccent,
                            checkedTrackColor = Color(0xFF143048)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto clipboard link detector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Detect Clipboard Links",
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Show quick banner when video URL is copied",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }
                    Switch(
                        checked = settings.autoClipboard,
                        onCheckedChange = { viewModel.updateAutoClipboard(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonPurple,
                            checkedTrackColor = Color(0xFF2E1949)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Max concurrent downloads slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Max Concurrent Downloads",
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${settings.maxConcurrent} active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                    }
                    Slider(
                        value = settings.maxConcurrent.toFloat(),
                        onValueChange = { viewModel.updateMaxConcurrent(it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanAccent,
                            activeTrackColor = CyberBlue,
                            inactiveTrackColor = SurfaceElevated
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 5: ABOUT ENGINE
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MediaDL Engine v1.0.0",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Based on yt-dlp specification with multi-stream OkHttp download pipeline and real-time speed & ETA tracking.",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    // DIALOG: CUSTOMIZE DOWNLOAD PATH
    if (showPathDialog) {
        val presets = viewModel.settingsRepo.getPresetPaths()

        AlertDialog(
            onDismissRequest = { showPathDialog = false },
            title = {
                Text(
                    text = "Customize Download Path",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Specify device directory where downloaded media files will be saved:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tempPathInput,
                        onValueChange = { tempPathInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_path_input_field"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated
                        ),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Quick Presets:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricBlue
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        presets.forEach { (path, label) ->
                            val isSelected = tempPathInput == path
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF1E3555) else SurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) CyanAccent else SurfaceBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { tempPathInput = path }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) CyanAccent else TextPrimary
                                        )
                                        Text(
                                            text = path,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = CyanAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ok = viewModel.updateDownloadPath(tempPathInput)
                        Toast.makeText(
                            context,
                            if (ok) "Path updated to: $tempPathInput" else "Path saved with fallback access",
                            Toast.LENGTH_SHORT
                        ).show()
                        showPathDialog = false
                    },
                    modifier = Modifier.testTag("save_download_path_button")
                ) {
                    Text("Apply & Save", color = CyanAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPathDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceElevated
        )
    }

    // DIALOG: NAMING TEMPLATE SELECTION
    if (showTemplateDialog) {
        val presets = SettingsRepository.NAMING_PRESETS

        AlertDialog(
            onDismissRequest = { showTemplateDialog = false },
            title = {
                Text(
                    text = "Select Filename Format",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Choose how downloaded video files should be named on disk:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    presets.forEach { (tmpl, label) ->
                        val isSelected = settings.namingTemplate == tmpl
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateNamingTemplate(tmpl)
                                    showTemplateDialog = false
                                }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.updateNamingTemplate(tmpl)
                                    showTemplateDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NeonPurple,
                                    unselectedColor = TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NeonPurple else TextPrimary
                                )
                                Text(
                                    text = tmpl,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextTertiary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTemplateDialog = false }) {
                    Text("Done", color = NeonPurple)
                }
            },
            containerColor = SurfaceElevated
        )
    }
}
