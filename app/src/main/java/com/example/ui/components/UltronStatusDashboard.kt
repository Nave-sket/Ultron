package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.UltronSurfaceVariant
import com.example.ui.theme.UltronTextMuted
import com.example.ui.theme.UltronTitanium
import com.example.ui.theme.UltronWarningAmber

@Composable
fun UltronStatusDashboard(
    isAccessibilityEnabled: Boolean,
    isMicGranted: Boolean,
    isListening: Boolean,
    voicePitch: Float,
    voiceRate: Float,
    onOpenAccessibilitySettings: () -> Unit,
    onRequestMicPermission: () -> Unit,
    onRefreshStatus: () -> Unit,
    onTestWakeAction: () -> Unit,
    onTestVoicePreview: () -> Unit,
    onUpdateVoiceSettings: (pitch: Float, rate: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showVoiceTuning by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // 1. Accessibility Service Status Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = UltronSurface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isAccessibilityEnabled) UltronNeonCyan.copy(alpha = 0.4f) else UltronCrimson.copy(alpha = 0.7f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("accessibility_status_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isAccessibilityEnabled) UltronNeonCyan.copy(alpha = 0.15f) else UltronCrimson.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessibilityNew,
                                contentDescription = "Accessibility",
                                tint = if (isAccessibilityEnabled) UltronNeonCyan else UltronCrimson,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ACCESSIBILITY BACK ACTION",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = UltronTextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isAccessibilityEnabled) "SERVICE CONNECTED & READY" else "SERVICE DISABLED",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAccessibilityEnabled) UltronSuccessGreen else UltronCrimson
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefreshStatus,
                        modifier = Modifier.size(36.dp).testTag("refresh_accessibility_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Status",
                            tint = UltronTitanium.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (!isAccessibilityEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Android requires manual activation to allow ULTRON to trigger the system Back navigation action.",
                        fontSize = 12.sp,
                        color = UltronTitanium.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenAccessibilitySettings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UltronCrimson,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("open_accessibility_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ENABLE IN ACCESSIBILITY SETTINGS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // 2. Microphone & Wake Word Detection Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = UltronSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, UltronCardBorder),
            modifier = Modifier.fillMaxWidth().testTag("wake_word_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isMicGranted) UltronNeonCyan.copy(alpha = 0.15f) else UltronWarningAmber.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Microphone",
                                tint = if (isMicGranted) UltronNeonCyan else UltronWarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "WAKE WORD DETECTION",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = UltronTextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "KEYWORD: \"ULTRON\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = UltronNeonCyan
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isMicGranted) UltronSuccessGreen.copy(alpha = 0.15f) else UltronWarningAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isMicGranted) "MIC OK" else "REQ PERMISSION",
                            color = if (isMicGranted) UltronSuccessGreen else UltronWarningAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (!isMicGranted) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onRequestMicPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = UltronSurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "GRANT MICROPHONE ACCESS",
                            color = UltronNeonCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Test Action Button
                OutlinedButton(
                    onClick = onTestWakeAction,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = UltronNeonCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, UltronNeonCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth().testTag("simulate_wake_action_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TEST WAKE SEQUENCE (\"Ultron\" → Back → \"Yes, Tony.\")",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // 3. Cinematic Voice Response Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = UltronSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, UltronCardBorder),
            modifier = Modifier.fillMaxWidth().testTag("voice_engine_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(UltronNeonCyan.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Voice Engine",
                                tint = UltronNeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "CINEMATIC VOICE RESPONSE",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = UltronTextMuted,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "OUTPUT: \"YES, TONY.\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = UltronTitanium
                            )
                        }
                    }

                    IconButton(
                        onClick = { showVoiceTuning = !showVoiceTuning },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Tune Voice",
                            tint = if (showVoiceTuning) UltronNeonCyan else UltronTitanium.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Characteristics: Deep baritone resonance, calm, authoritative, controlled cadence.",
                    fontSize = 11.sp,
                    color = UltronTitanium.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onTestVoicePreview,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UltronSurfaceVariant,
                        contentColor = UltronNeonCyan
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("preview_voice_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PREVIEW VOICE (\"Yes, Tony.\")",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                // Voice Tuning Controls
                AnimatedVisibility(visible = showVoiceTuning) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .background(Color(0xFF090D14), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "VOICE ACOUSTIC TUNING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = UltronNeonCyan
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Pitch Slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Pitch (Resonance)",
                                fontSize = 11.sp,
                                color = UltronTitanium
                            )
                            Text(
                                text = "%.2f".format(voicePitch),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = UltronNeonCyan
                            )
                        }
                        Slider(
                            value = voicePitch,
                            onValueChange = { onUpdateVoiceSettings(it, voiceRate) },
                            valueRange = 0.5f..1.2f,
                            colors = SliderDefaults.colors(
                                thumbColor = UltronNeonCyan,
                                activeTrackColor = UltronNeonCyan,
                                inactiveTrackColor = Color(0xFF1E2838)
                            )
                        )

                        // Speech Rate Slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Speech Rate (Cadence)",
                                fontSize = 11.sp,
                                color = UltronTitanium
                            )
                            Text(
                                text = "%.2f".format(voiceRate),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = UltronNeonCyan
                            )
                        }
                        Slider(
                            value = voiceRate,
                            onValueChange = { onUpdateVoiceSettings(voicePitch, it) },
                            valueRange = 0.7f..1.3f,
                            colors = SliderDefaults.colors(
                                thumbColor = UltronNeonCyan,
                                activeTrackColor = UltronNeonCyan,
                                inactiveTrackColor = Color(0xFF1E2838)
                            )
                        )

                        OutlinedButton(
                            onClick = { onUpdateVoiceSettings(0.76f, 0.90f) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = "Reset to Default (0.76 / 0.90)",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
