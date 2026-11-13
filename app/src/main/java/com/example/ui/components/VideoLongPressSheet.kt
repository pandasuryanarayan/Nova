package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.browser.BrowserUrlUtils
import com.example.data.DetectedVideoInfo
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoLongPressBottomSheet(
    videoInfo: DetectedVideoInfo,
    onSelectQuality: (Int) -> Unit,
    onDownloadMp4Click: () -> Unit,
    onCopyVideoLinkClick: (String) -> Unit,
    onOpenInNewTabClick: (String) -> Unit,
    onPlayInBackgroundClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedQuality = videoInfo.selectedQuality
    val isProtectedOrStreaming = selectedQuality == null ||
        selectedQuality.isStreaming ||
        !selectedQuality.isDirectMp4 ||
        BrowserUrlUtils.isProtectedOrStreamingUrl(
            selectedQuality.url,
            selectedQuality.probedContentType ?: selectedQuality.mimeType
        )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .testTag("video_long_press_sheet")
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Videocam,
                            contentDescription = "Detected Video",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = videoInfo.pageTitle.ifBlank { "Detected Web Video" },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = videoInfo.domain,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (videoInfo.isIframe) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "IFRAME VIDEO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Video Thumbnail + Metadata Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0B1021),
                                Color(0xFF1E294B),
                                Color(0xFF281847)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                if (videoInfo.posterUrl.isNotBlank()) {
                    var posterBitmap by remember(videoInfo.posterUrl) { mutableStateOf<ImageBitmap?>(null) }
                    LaunchedEffect(videoInfo.posterUrl) {
                        posterBitmap = withContext(Dispatchers.IO) {
                            try {
                                val conn = (URL(videoInfo.posterUrl).openConnection() as HttpURLConnection).apply {
                                    connectTimeout = 5000
                                    readTimeout = 5000
                                }
                                conn.inputStream.use { stream ->
                                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                                }
                            } catch (_: Exception) {
                                null
                            }
                        }
                    }
                    posterBitmap?.let { bmp ->
                        Image(
                            bitmap = bmp,
                            contentDescription = "Video Thumbnail Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.38f))
                    )
                }

                // Center Play / Video icon overlay
                Icon(
                    imageVector = Icons.Filled.PlayCircleFilled,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )

                // Top-left Domain & Mime Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isProtectedOrStreaming) {
                                "STREAMING / PROTECTED"
                            } else {
                                (selectedQuality?.probedContentType ?: selectedQuality?.mimeType ?: "video/mp4").uppercase()
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isProtectedOrStreaming) Color(0xFFFF8A80) else Color(0xFF00E5FF),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (videoInfo.dimensions.isNotBlank()) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.72f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = videoInfo.dimensions,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Bottom bar inside preview: Estimated File Size + Duration
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedQuality?.isProbing == true && !isProtectedOrStreaming) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Checking file size from headers…",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        } else {
                            val sizeLabel = when {
                                isProtectedOrStreaming -> "Direct download blocked (HLS / DASH / Protected)"
                                (selectedQuality?.estimatedBytes ?: -1L) > 0L ->
                                    "Estimated Size: ${BrowserUrlUtils.formatBytes(selectedQuality?.estimatedBytes ?: -1L)}"
                                else -> "Direct MP4 Stream • Ready to save"
                            }
                            Text(
                                text = sizeLabel,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    if (videoInfo.durationSeconds > 0) {
                        Text(
                            text = BrowserUrlUtils.formatDuration(videoInfo.durationSeconds),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF00E5FF)
                        )
                    }
                }
            }

            // Multiple Qualities Selector (if > 1 quality detected)
            if (videoInfo.qualities.size > 1) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.HighQuality,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Choose Video Quality (${videoInfo.qualities.size} streams found)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    videoInfo.qualities.forEachIndexed { idx, q ->
                        val isSelected = idx == videoInfo.selectedQualityIndex
                        val sizeSuffix = if (q.estimatedBytes > 0L) {
                            " • ${BrowserUrlUtils.formatBytes(q.estimatedBytes)}"
                        } else {
                            ""
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectQuality(idx) }
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .testTag("video_quality_chip_$idx")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = "${q.label}$sizeSuffix",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Protected / Streaming Warning Box if applicable
            if (isProtectedOrStreaming) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "This video is streaming / protected and cannot be downloaded",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // 1. Primary Action: Download Video (MP4)
            Button(
                onClick = onDownloadMp4Click,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isProtectedOrStreaming) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    contentColor = if (isProtectedOrStreaming) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("download_video_mp4_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Download Video MP4"
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Download Video (MP4)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Actions Row: Copy Video Link + Open Video in New Tab
            val currentVideoUrl = selectedQuality?.url ?: videoInfo.primaryUrl
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onCopyVideoLinkClick(currentVideoUrl) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("copy_video_link_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy Video Link",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Copy Video Link",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = { onOpenInNewTabClick(currentVideoUrl) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_video_new_tab_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open Video in New Tab",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open in New Tab",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Play in Background Action
            OutlinedButton(
                onClick = onPlayInBackgroundClick,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("play_in_background_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Headphones,
                    contentDescription = "Play in Background",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Play in Background",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Saves directly to: Download/NovaBrowser/Videos/",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
