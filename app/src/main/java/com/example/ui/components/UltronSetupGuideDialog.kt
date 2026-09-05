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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.UltronCardBorder
import com.example.ui.theme.UltronNeonCyan
import com.example.ui.theme.UltronSurface
import com.example.ui.theme.UltronSurfaceVariant
import com.example.ui.theme.UltronTextMuted
import com.example.ui.theme.UltronTitanium

@Composable
fun UltronSetupGuideDialog(
    onDismiss: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = UltronSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, UltronNeonCyan.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .testTag("setup_guide_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = UltronNeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SETUP INSTRUCTIONS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = UltronTitanium,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_guide_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = UltronTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GuideStepCard(
                    stepNumber = "1",
                    title = "Install APK on your Phone",
                    description = "Download and install the APK. If Android displays 'Install unknown apps', tap Settings and allow your browser or file manager to install the package."
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideStepCard(
                    stepNumber = "2",
                    title = "Enable Accessibility Service",
                    description = "Go to: Settings > Accessibility > Installed Apps / Downloaded Apps > Select 'ULTRON Back Action Service' > Turn ON.",
                    actionButton = {
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenAccessibilitySettings()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = UltronNeonCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("OPEN ACCESSIBILITY SETTINGS", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideStepCard(
                    stepNumber = "3",
                    title = "Grant Microphone & Notifications",
                    description = "Allow the microphone permission so ULTRON can listen for 'Ultron'. Allow notification permission so the foreground service remains active in the background."
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideStepCard(
                    stepNumber = "4",
                    title = "Say 'Ultron' from Any App",
                    description = "Turn ON the Master Power Switch. Switch to any app (browser, social media, settings). Say 'Ultron' clearly — ULTRON will immediately navigate Back and confirm with 'Yes, Tony.'"
                )

                Spacer(modifier = Modifier.height(12.dp))

                GuideStepCard(
                    stepNumber = "5",
                    title = "Optional: Unrestricted Battery",
                    description = "To ensure Android doesn't kill the foreground listening service in deep sleep: App Info > Battery > Select 'Unrestricted'."
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UltronSurfaceVariant, contentColor = UltronTitanium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("I UNDERSTAND // PROCEED", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun GuideStepCard(
    stepNumber: String,
    title: String,
    description: String,
    actionButton: (@Composable () -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090E17)),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltronCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(UltronNeonCyan.copy(alpha = 0.2f))
                        .border(1.dp, UltronNeonCyan, CircleShape)
                ) {
                    Text(
                        text = stepNumber,
                        color = UltronNeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = UltronTitanium,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 11.sp,
                color = UltronTextMuted,
                lineHeight = 15.sp
            )

            if (actionButton != null) {
                Spacer(modifier = Modifier.height(10.dp))
                actionButton()
            }
        }
    }
}
