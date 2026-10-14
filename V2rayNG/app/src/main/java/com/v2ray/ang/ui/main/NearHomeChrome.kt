package com.v2ray.ang.ui.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.extension.toSpeedString
import kotlinx.coroutines.launch

/**
 * Near Cloud home-screen chrome: the big connect ring, live traffic chips,
 * the floating glass navigation bar, and the simple stats overlay. All user
 * operations are delegated to the caller (MainScreen dispatches MainAction);
 * these composables hold no state beyond transient animation.
 */

@Composable
fun ConnectRing(
    isRunning: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val rotationAnim = remember { Animatable(0f) }

    LaunchedEffect(isRunning) {
        if (!isRunning) {
            rotationAnim.snapTo(0f)
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val borderColor = if (isRunning) {
        colorScheme.primary
    } else {
        colorScheme.primary.copy(alpha = 0.55f)
    }
    val backgroundAlpha = if (isRunning) 0.38f else 0.25f
    val accLabel = stringResource(if (isRunning) R.string.acc_stop else R.string.acc_start)

    Box(
        modifier = modifier
            .size(188.dp)
            .border(3.dp, borderColor, CircleShape)
            .background(colorScheme.surfaceContainer.copy(alpha = backgroundAlpha), CircleShape)
            .clip(CircleShape)
            .clickable {
                if (!isRunning) {
                    scope.launch {
                        rotationAnim.animateTo(
                            targetValue = 720f,
                            animationSpec = tween(durationMillis = 3000)
                        )
                    }
                }
                onToggle()
            }
            .semantics { contentDescription = accLabel },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(
                    if (isRunning) R.drawable.ic_stop_24dp else R.drawable.ic_play_24dp
                ),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(46.dp)
                    .graphicsLayer { rotationZ = rotationAnim.value }
            )
            Text(
                text = stringResource(if (isRunning) R.string.near_disconnect else R.string.near_connect),
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TrafficChips(
    uplinkBps: Long,
    downlinkBps: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TrafficChip(text = stringResource(R.string.near_upload) + "  ↑ " + uplinkBps.toSpeedString())
        TrafficChip(text = stringResource(R.string.near_download) + "  ↓ " + downlinkBps.toSpeedString())
    }
}

@Composable
private fun TrafficChip(text: String) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.66f),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

enum class NearTab { HOME, SERVERS, STATS, SETTINGS }

@Composable
fun GlassNavBar(
    selected: NearTab,
    onSelect: (NearTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        NearTab.HOME to R.string.near_nav_home,
        NearTab.SERVERS to R.string.near_nav_servers,
        NearTab.STATS to R.string.near_nav_stats,
        NearTab.SETTINGS to R.string.near_nav_settings
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f),
                    RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { (tab, labelRes) ->
                val isSelected = tab == selected
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                            } else {
                                Color.Transparent
                            },
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelect(tab) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatsOverlay(
    statusText: String,
    profileName: String?,
    profileAddress: String?,
    delayMillis: Long,
    uplinkBps: Long,
    downlinkBps: Long,
    onTest: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070D24).copy(alpha = 0.88f))
            .clickable(onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
                    RoundedCornerShape(24.dp)
                )
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.near_nav_stats),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (profileName != null) {
                Text(
                    text = profileName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (profileAddress != null) {
                    Text(
                        text = profileAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (delayMillis > 0) {
                Text(
                    text = delayMillis.toString() + " ms",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            TrafficChips(uplinkBps = uplinkBps, downlinkBps = downlinkBps)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatsPillButton(text = stringResource(R.string.near_test_ping), onClick = onTest)
                StatsPillButton(text = stringResource(R.string.near_nav_home), onClick = onClose)
            }
        }
    }
}

@Composable
private fun StatsPillButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
