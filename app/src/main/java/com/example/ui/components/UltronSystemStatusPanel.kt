package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
 * Futuristic System Status Panel:
 * ULTRON SYSTEM
 * - Core: ONLINE / OFFLINE
 * - Voice: ONLINE / OFFLINE
 * - Microphone: READY / NOT GRANTED
 * - Accessibility: CONNECTED / DISCONNECTED
 * - Wake Word: ACTIVE / INACTIVE
 * With animated blinking LED status nodes
 */
@Composable
fun UltronSystemStatusPanel(
    isMasterOn: Boolean,
    isVoiceOnline: Boolean,
    isMicGranted: Boolean,
    isAccessibilityConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink_alpha"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = UltronSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltronCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("ultron_system_status_panel")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = UltronNeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ULTRON SYSTEM STATUS",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = UltronTitanium,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Core
                SystemStatusRow(
                    label = "Core",
                    status = if (isMasterOn) "ONLINE" else "OFFLINE",
                    isGood = isMasterOn,
                    blinkAlpha = if (isMasterOn) blinkAlpha else 1f
                )

                // 2. Voice
                SystemStatusRow(
                    label = "Voice",
                    status = if (isVoiceOnline) "ONLINE" else "INITIALIZING",
                    isGood = isVoiceOnline,
                    blinkAlpha = if (isVoiceOnline) blinkAlpha else 1f
                )

                // 3. Microphone
                SystemStatusRow(
                    label = "Microphone",
                    status = if (isMicGranted) "READY" else "NOT GRANTED",
                    isGood = isMicGranted,
                    blinkAlpha = if (isMicGranted) blinkAlpha else 1f
                )

                // 4. Accessibility
                SystemStatusRow(
                    label = "Accessibility",
                    status = if (isAccessibilityConnected) "CONNECTED" else "DISCONNECTED",
                    isGood = isAccessibilityConnected,
                    blinkAlpha = if (isAccessibilityConnected) blinkAlpha else 1f
                )

                // 5. Wake Word
                SystemStatusRow(
                    label = "Wake Word (\"Ultron\")",
                    status = if (isMasterOn && isMicGranted) "ACTIVE" else "INACTIVE",
                    isGood = isMasterOn && isMicGranted,
                    blinkAlpha = if (isMasterOn && isMicGranted) blinkAlpha else 1f
                )
            }
        }
    }
}

@Composable
private fun SystemStatusRow(
    label: String,
    status: String,
    isGood: Boolean,
    blinkAlpha: Float
) {
    val indicatorColor = if (isGood) UltronSuccessGreen else UltronCrimson

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(indicatorColor.copy(alpha = blinkAlpha))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = UltronTitanium.copy(alpha = 0.85f)
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(indicatorColor.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = status,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = indicatorColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}
