package com.sayzen.campfiresdk.video.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sayzen.campfiresdk.R

private val PLAYBACK_SPEEDS = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)

@Composable
internal fun PlayerControls(
    isPlaying: Boolean,
    isBuffering: Boolean,
    position: () -> Long,
    duration: Long,
    scrubbing: Boolean,
    scrubValue: Float,
    playbackSpeed: Float,
    speedMenuOpen: Boolean,
    downloading: Boolean,
    onPlayPause: () -> Unit,
    onBack: () -> Unit,
    onDownload: () -> Unit,
    onSpeedMenuOpenChange: (Boolean) -> Unit,
    onSpeedSelected: (Float) -> Unit,
    onScrub: (Float) -> Unit,
    onScrubFinished: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!isBuffering) {
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .size(64.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause
                        else Icons.Default.PlayArrow,
                        contentDescription = stringResource(
                            if (isPlaying) R.string.player_pause else R.string.player_play
                        ),
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerBackButton(onBack)
            Spacer(Modifier.weight(1f))
            Box {
                SpeedButton(
                    playbackSpeed = playbackSpeed,
                    onClick = { onSpeedMenuOpenChange(true) },
                )
                DropdownMenu(
                    expanded = speedMenuOpen,
                    onDismissRequest = { onSpeedMenuOpenChange(false) }
                ) {
                    PLAYBACK_SPEEDS.forEach { speed ->
                        DropdownMenuItem(
                            text = { Text(formatSpeed(speed)) },
                            trailingIcon = {
                                if (speed == playbackSpeed) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            onClick = { onSpeedSelected(speed) }
                        )
                    }
                }
            }
            DownloadButton(downloading = downloading, onClick = onDownload)
        }

        PlayerSeekBar(
            position = position,
            duration = duration,
            scrubbing = scrubbing,
            scrubValue = scrubValue,
            onScrub = onScrub,
            onScrubFinished = onScrubFinished,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 16.dp),
        )
    }
}

@Composable
internal fun PlayerBackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onBack, modifier = modifier) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.player_back),
            tint = Color.White
        )
    }
}

@Composable
private fun DownloadButton(downloading: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = !downloading) {
        if (downloading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Icon(
                Icons.Default.Download,
                contentDescription = stringResource(R.string.player_download),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun SpeedButton(
    playbackSpeed: Float,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .heightIn(min = 40.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Icon(
            Icons.Default.Speed,
            contentDescription = stringResource(R.string.player_speed),
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = formatSpeed(playbackSpeed),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun PlayerSeekBar(
    position: () -> Long,
    duration: Long,
    scrubbing: Boolean,
    scrubValue: Float,
    onScrub: (Float) -> Unit,
    onScrubFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val positionMs = position()
    val displayPos = if (scrubbing && duration > 0) (scrubValue * duration).toLong() else positionMs
    val sliderValue = when {
        scrubbing -> scrubValue
        duration > 0 -> (positionMs.toFloat() / duration).coerceIn(0f, 1f)
        else -> 0f
    }

    Column(modifier = modifier) {
        // systemGestureExclusion stops Android's edge-swipe handlers from
        // cancelling drags after a single event.
        Slider(
            value = sliderValue,
            onValueChange = onScrub,
            onValueChangeFinished = onScrubFinished,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.White.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .systemGestureExclusion()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(formatPlayerTime(displayPos), color = Color.White)
            Spacer(Modifier.weight(1f))
            Text(formatPlayerTime(duration), color = Color.White)
        }
    }
}

internal fun formatPlayerTime(ms: Long): String {
    if (ms < 0) return "0:00"
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%d:%02d".format(m, s)
}

internal fun formatSpeed(s: Float): String {
    val rounded = (s * 100).toInt() / 100f
    return if (rounded == rounded.toInt().toFloat()) "${rounded.toInt()}x"
    else "%sx".format(rounded.toString().trimEnd('0').trimEnd('.'))
}

@Preview
@Composable
private fun PlayerControlsPreview() {
    Surface(color = Color.Black) {
        PlayerControls(
            isPlaying = true,
            isBuffering = false,
            position = { 12_000 },
            duration = 32_000,
            scrubbing = false,
            scrubValue = 0f,
            playbackSpeed = 1f,
            speedMenuOpen = false,
            downloading = false,
            onPlayPause = {},
            onBack = {},
            onDownload = {},
            onSpeedMenuOpenChange = {},
            onSpeedSelected = {},
            onScrub = {},
            onScrubFinished = {},
        )
    }
}
