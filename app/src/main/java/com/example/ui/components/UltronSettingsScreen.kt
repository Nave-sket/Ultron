package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun UltronSettingsScreen(
    isMasterOn: Boolean,
    onToggleMaster: (Boolean) -> Unit,
    isMicGranted: Boolean,
    onRequestMicPermission: () -> Unit,
    isAccessibilityConnected: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    speechRate: Float,
    volume: Float,
    pitch: Float,
    onVoiceSettingsChanged: (pitch: Float, rate: Float, volume: Float) -> Unit,
    onTestVoice: () -> Unit,
    onTestBack: () -> Unit,
    onCloseSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080B10))
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(scrollState)
            .testTag("ultron_settings_screen")
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onCloseSettings,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(UltronSurface)
                    .testTag("btn_close_settings")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Main",
                    tint = UltronNeonCyan
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "SETTINGS & DIAGNOSTICS",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = UltronNeonCyan,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "ULTRON AI Configuration",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = UltronTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. ULTRON ON/OFF Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UltronSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, UltronCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ULTRON POWER SWITCH",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = UltronTitanium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isMasterOn) "Active - Listening in foreground service" else "Dormant - All background processes stopped",
                        fontSize = 11.sp,
                        color = if (isMasterOn) UltronNeonCyan else UltronTextMuted
                    )
                }

                Switch(
                    checked = isMasterOn,
                    onCheckedChange = onToggleMaster,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = UltronNeonCyan,
                        checkedTrackColor = UltronNeonCyan.copy(alpha = 0.3f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF1E2634)
                    ),
                    modifier = Modifier.testTag("settings_master_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Wake Word & Permissions Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UltronSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, UltronCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "TRIGGERS & PERMISSIONS",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = UltronNeonCyan,
                    letterSpacing = 1.sp
                )

                // Wake Word Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = UltronNeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Wake Word",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = UltronTitanium
                            )
                            Text(
                                text = "Keyword: \"Ultron\"",
                                fontSize = 10.sp,
                                color = UltronTextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(UltronNeonCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isMasterOn) "ACTIVE" else "INACTIVE",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isMasterOn) UltronNeonCyan else Color.Gray
                        )
                    }
                }

                // Microphone Permission
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (isMicGranted) UltronSuccessGreen else UltronWarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Microphone Permission",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = UltronTitanium
                            )
                            Text(
                                text = if (isMicGranted) "Granted & Continuous" else "Required for voice wake",
                                fontSize = 10.sp,
                                color = if (isMicGranted) UltronSuccessGreen else UltronWarningAmber
                            )
                        }
                    }

                    if (!isMicGranted) {
                        Button(
                            onClick = onRequestMicPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = UltronNeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_grant_mic")
                        ) {
                            Text("Grant", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(UltronSuccessGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("READY", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = UltronSuccessGreen)
                        }
                    }
                }

                // Accessibility Service Status & Open Settings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessibilityNew,
                            contentDescription = null,
                            tint = if (isAccessibilityConnected) UltronSuccessGreen else UltronWarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Accessibility Service",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = UltronTitanium
                            )
                            Text(
                                text = if (isAccessibilityConnected) "Connected (Back Action Ready)" else "Needs Permission in Settings",
                                fontSize = 10.sp,
                                color = if (isAccessibilityConnected) UltronSuccessGreen else UltronWarningAmber
                            )
                        }
                    }

                    Button(
                        onClick = onOpenAccessibilitySettings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAccessibilityConnected) Color(0xFF1E2634) else UltronWarningAmber
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_open_accessibility_settings")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isAccessibilityConnected) "Settings" else "Enable",
                                fontSize = 11.sp,
                                color = if (isAccessibilityConnected) UltronTitanium else Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = if (isAccessibilityConnected) UltronTitanium else Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Audio & Voice Tuning Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UltronSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, UltronCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "ULTRON CINEMATIC VOICE TUNING",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = UltronNeonCyan,
                    letterSpacing = 1.sp
                )

                // Speech Speed Slider
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = UltronTitanium, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Speech Delivery Rate", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = UltronTitanium)
                        }
                        Text("${"%.2f".format(speechRate)}x", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = UltronNeonCyan)
                    }
                    Slider(
                        value = speechRate,
                        onValueChange = { onVoiceSettingsChanged(pitch, it, volume) },
                        valueRange = 0.65f..1.35f,
                        colors = SliderDefaults.colors(
                            thumbColor = UltronNeonCyan,
                            activeTrackColor = UltronNeonCyan,
                            inactiveTrackColor = Color(0xFF1E2634)
                        ),
                        modifier = Modifier.testTag("slider_speech_rate")
                    )
                }

                // Voice Volume Slider
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = UltronTitanium, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Voice Volume", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = UltronTitanium)
                        }
                        Text("${(volume * 100).toInt()}%", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = UltronNeonCyan)
                    }
                    Slider(
                        value = volume,
                        onValueChange = { onVoiceSettingsChanged(pitch, speechRate, it) },
                        valueRange = 0.2f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = UltronNeonCyan,
                            activeTrackColor = UltronNeonCyan,
                            inactiveTrackColor = Color(0xFF1E2634)
                        ),
                        modifier = Modifier.testTag("slider_voice_volume")
                    )
                }

                // Voice Pitch Tuning (Masculine resonance)
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = UltronTitanium, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Masculine Pitch Resonance", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = UltronTitanium)
                        }
                        Text("${"%.2f".format(pitch)}x", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = UltronNeonCyan)
                    }
                    Slider(
                        value = pitch,
                        onValueChange = { onVoiceSettingsChanged(it, speechRate, volume) },
                        valueRange = 0.55f..1.05f,
                        colors = SliderDefaults.colors(
                            thumbColor = UltronNeonCyan,
                            activeTrackColor = UltronNeonCyan,
                            inactiveTrackColor = Color(0xFF1E2634)
                        ),
                        modifier = Modifier.testTag("slider_pitch")
                    )
                }

                // Action Buttons: Test Voice & Test Back
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onTestVoice,
                        colors = ButtonDefaults.buttonColors(containerColor = UltronNeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_test_voice")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Voice", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    OutlinedButton(
                        onClick = onTestBack,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, UltronNeonCyan.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_test_back")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccessibilityNew, contentDescription = null, tint = UltronNeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Back", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UltronNeonCyan)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. About ULTRON AI Assistant
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UltronSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, UltronCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = UltronNeonCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ABOUT ULTRON AI ASSISTANT",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = UltronTitanium,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "ULTRON AI Assistant is an original futuristic, voice-triggered assistant interface. It listens continuously for the \"Ultron\" wake word, performs the system Back navigation gesture via Android Accessibility Services, and acknowledges with an authoritative synthetic delivery: \"Yes, Tony.\"",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = UltronTextMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Protocol Version", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = UltronTextMuted)
                    Text("v2.4.0-CINEMATIC", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = UltronNeonCyan)
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Target Response", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = UltronTextMuted)
                    Text("\"Yes, Tony.\"", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = UltronSuccessGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
