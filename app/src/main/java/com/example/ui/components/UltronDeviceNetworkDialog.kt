package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ConnectedDevice
import com.example.model.DeviceStatus
import com.example.model.DeviceType
import com.example.service.UltronDeviceNetworkManager
import com.example.state.UltronAssistantManager
import com.example.ui.theme.UltronCardBorder
import com.example.ui.theme.UltronCrimson
import com.example.ui.theme.UltronNeonCyan
import com.example.ui.theme.UltronSuccessGreen
import com.example.ui.theme.UltronSurface
import com.example.ui.theme.UltronSurfaceVariant
import com.example.ui.theme.UltronTextMuted
import com.example.ui.theme.UltronTitanium
import com.example.ui.theme.UltronWarningAmber

/**
 * Floating Holographic Device Network Visualization Dialog
 *
 * Displays:
 * - Phone as central node
 * - Authorized/paired devices around it
 * - Signal, status, and legitimate device actions (Volume control, Bluetooth sync, authorization notice)
 * - Safe security policy notice (Never bypasses lock screens or accesses unauthorized peripherals)
 */
@Composable
fun UltronDeviceNetworkDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val devices by UltronDeviceNetworkManager.devices.collectAsStateWithLifecycle()
    val isBtEnabled by UltronDeviceNetworkManager.isBluetoothEnabled.collectAsStateWithLifecycle()

    var selectedDevice by remember { mutableStateOf<ConnectedDevice?>(null) }
    var actionFeedback by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D14)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, UltronNeonCyan.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("dialog_device_network")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = null,
                            tint = UltronNeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DEVICE NETWORK",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = UltronTitanium,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "AUTHORIZED NEURAL MESH",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = UltronNeonCyan,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(UltronSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = UltronTitanium,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Network Status Summary Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F1824))
                        .border(1.dp, UltronCardBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "STATUS: ${if (isBtEnabled) "BLUETOOTH ACTIVE" else "LOCAL MESH ONLY"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isBtEnabled) UltronSuccessGreen else UltronWarningAmber
                            )
                            Text(
                                text = "${devices.size} Nodes Synced • Legit Authorization Active",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = UltronTextMuted
                            )
                        }

                        IconButton(
                            onClick = {
                                UltronDeviceNetworkManager.refreshDevices(context)
                                actionFeedback = "Network scanned. Nodes synchronized."
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan",
                                tint = UltronNeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (actionFeedback != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "› $actionFeedback",
                        color = UltronNeonCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Device List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(devices) { dev ->
                        val isSelected = selectedDevice?.id == dev.id
                        DeviceCard(
                            device = dev,
                            isSelected = isSelected,
                            onClick = {
                                selectedDevice = if (isSelected) null else dev
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Device Actions Panel (if device selected)
                if (selectedDevice != null) {
                    val dev = selectedDevice!!
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(UltronSurfaceVariant)
                            .border(1.dp, UltronNeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "ACTIONS: ${dev.name}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = UltronTitanium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        val vol = UltronDeviceNetworkManager.adjustVolume(context, 1)
                                        actionFeedback = "Volume amplified to level $vol"
                                        UltronAssistantManager.addLog("Volume amplified on ${dev.name}", isSuccess = true, tag = "DEVICE")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = UltronSurface),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = UltronNeonCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Vol +", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = UltronTitanium)
                                }

                                Button(
                                    onClick = {
                                        val vol = UltronDeviceNetworkManager.adjustVolume(context, -1)
                                        actionFeedback = "Volume reduced to level $vol"
                                        UltronAssistantManager.addLog("Volume reduced on ${dev.name}", isSuccess = true, tag = "DEVICE")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = UltronSurface),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.VolumeDown, contentDescription = null, tint = UltronNeonCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Vol -", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = UltronTitanium)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Security Policy Footnote
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = UltronSuccessGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Authorized Protocol: No bypass of biometric/lock credentials permitted.",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = UltronTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: ConnectedDevice,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val devIcon = when (device.type) {
        DeviceType.PHONE -> Icons.Default.PhoneAndroid
        DeviceType.BLUETOOTH_AUDIO -> Icons.Default.Headphones
        DeviceType.SMART_WATCH -> Icons.Default.Watch
        DeviceType.SMART_DEVICE -> Icons.Default.Hub
        DeviceType.SATELLITE_NODE -> Icons.Default.Bluetooth
    }

    val statusColor = when (device.status) {
        DeviceStatus.CONNECTED -> UltronSuccessGreen
        DeviceStatus.ONLINE -> UltronNeonCyan
        DeviceStatus.STANDBY -> UltronWarningAmber
        DeviceStatus.DISCONNECTED -> UltronCrimson
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF132030) else UltronSurface)
            .border(
                1.dp,
                if (isSelected) UltronNeonCyan else UltronCardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
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
                        .background(UltronSurfaceVariant)
                        .border(1.dp, statusColor.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = devIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = device.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = UltronTitanium
                    )
                    Text(
                        text = "Signal: ${device.signalStrength}% • Battery: ${device.batteryLevel}%",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = UltronTextMuted
                    )
                }
            }

            Text(
                text = device.status.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = statusColor
            )
        }
    }
}
