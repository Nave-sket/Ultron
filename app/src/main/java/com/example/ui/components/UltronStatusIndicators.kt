package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.UltronCardBorder
import com.example.ui.theme.UltronCrimson
import com.example.ui.theme.UltronNeonCyan
import com.example.ui.theme.UltronSuccessGreen
import com.example.ui.theme.UltronSurface
import com.example.ui.theme.UltronTextMuted
import com.example.ui.theme.UltronTitanium
import com.example.ui.theme.UltronWarningAmber

/**
 * Three primary status indicators positioned below the central core:
 * - 🎤 Microphone: Ready
 * - ♿ Accessibility: Connected / Offline
 * - 🔊 Voice: Ready
 */
@Composable
fun UltronStatusIndicators(
    isMicGranted: Boolean,
    isAccessibilityConnected: Boolean,
    isVoiceReady: Boolean,
    isMasterOn: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("ultron_status_indicators")
    ) {
        StatusIndicatorPill(
            icon = Icons.Default.Mic,
            title = "MIC",
            status = if (isMicGranted) "Ready" else "Req Perm",
            isOk = isMicGranted,
            modifier = Modifier.weight(1f)
        )

        StatusIndicatorPill(
            icon = Icons.Default.AccessibilityNew,
            title = "ACCESSIBILITY",
            status = if (isAccessibilityConnected) "Connected" else "Offline",
            isOk = isAccessibilityConnected,
            modifier = Modifier.weight(1.2f)
        )

        StatusIndicatorPill(
            icon = Icons.Default.VolumeUp,
            title = "VOICE",
            status = if (isVoiceReady) "Ready" else "Init",
            isOk = isVoiceReady,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatusIndicatorPill(
    icon: ImageVector,
    title: String,
    status: String,
    isOk: Boolean,
    modifier: Modifier = Modifier
) {
    val statusColor = if (isOk) UltronSuccessGreen else UltronWarningAmber

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(UltronSurface)
            .border(1.dp, if (isOk) UltronCardBorder else statusColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = statusColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = UltronTextMuted,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = status,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}
