package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.UltronAccessibilityService
import com.example.state.AssistantState
import com.example.state.UltronAssistantManager
import com.example.ui.components.UltronReactorCore
import com.example.ui.components.UltronSettingsScreen
import com.example.ui.components.UltronSetupGuideDialog
import com.example.ui.components.UltronStatusIndicators
import com.example.ui.components.UltronSystemStatusPanel
import com.example.ui.components.UltronTerminalLog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.UltronBackground
import com.example.ui.theme.UltronCardBorder
import com.example.ui.theme.UltronNeonCyan
import com.example.ui.theme.UltronSuccessGreen
import com.example.ui.theme.UltronSurface
import com.example.ui.theme.UltronSurfaceVariant
import com.example.ui.theme.UltronTextMuted
import com.example.ui.theme.UltronTitanium

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize state manager and audio subsystem
        UltronAssistantManager.initialize(this)

        setContent {
            MyApplicationTheme {
                UltronApp()
            }
        }
    }
}

@Composable
fun UltronApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val assistantState by UltronAssistantManager.assistantState.collectAsStateWithLifecycle()
    val isMasterOn by UltronAssistantManager.isMasterSwitchOn.collectAsStateWithLifecycle()
    val isAccessibilityConnected by UltronAssistantManager.isAccessibilityConnected.collectAsStateWithLifecycle()
    val isVoiceReady by UltronAssistantManager.isVoiceReady.collectAsStateWithLifecycle()
    val rmsLevel by UltronAssistantManager.rmsLevel.collectAsStateWithLifecycle()
    val voicePitch by UltronAssistantManager.voicePitch.collectAsStateWithLifecycle()
    val voiceRate by UltronAssistantManager.voiceRate.collectAsStateWithLifecycle()
    val voiceVolume by UltronAssistantManager.voiceVolume.collectAsStateWithLifecycle()
    val logs by UltronAssistantManager.logs.collectAsStateWithLifecycle()

    var isMicGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isAccessibilityEnabledBySetting by remember {
        mutableStateOf(UltronAccessibilityService.isAccessibilityServiceEnabled(context))
    }

    var showSettingsScreen by remember { mutableStateOf(false) }
    var showSetupGuide by remember { mutableStateOf(false) }

    // Permission Request Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val micResult = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        isMicGranted = micResult
        if (micResult) {
            UltronAssistantManager.addLog("Microphone access granted by user.", isSuccess = true, tag = "PERMISSION")
        } else {
            UltronAssistantManager.addLog("Microphone permission denied. Voice listening cannot start.", isSuccess = false, tag = "PERMISSION")
        }
    }

    fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            UltronAssistantManager.addLog("Opened Accessibility Settings. Turn ON 'ULTRON Back Action Service'.", isSuccess = true, tag = "NAV")
        } catch (e: Exception) {
            UltronAssistantManager.addLog("Could not launch Accessibility Settings: ${e.message}", isSuccess = false, tag = "ERROR")
        }
    }

    // Refresh accessibility status onResume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isMicGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                isAccessibilityEnabledBySetting = UltronAccessibilityService.isAccessibilityServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(UltronBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = UltronBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Dashboard View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // TOP: ULTRON AI ASSISTANT Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(UltronSurfaceVariant)
                                .border(1.5.dp, UltronNeonCyan.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ultron_logo),
                                contentDescription = "ULTRON Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "ULTRON",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = UltronTitanium,
                                letterSpacing = 3.sp
                            )
                            Text(
                                text = "AI ASSISTANT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = UltronNeonCyan,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Settings Button
                        IconButton(
                            onClick = { showSettingsScreen = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(UltronSurface)
                                .testTag("btn_open_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Open Settings",
                                tint = UltronNeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Help / Setup Guide Button
                        IconButton(
                            onClick = { showSetupGuide = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(UltronSurface)
                                .testTag("help_guide_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Setup Guide",
                                tint = UltronTitanium.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CENTER: Animated ULTRON AI Core
                UltronReactorCore(
                    assistantState = assistantState,
                    isMasterOn = isMasterOn,
                    rmsLevel = rmsLevel,
                    onToggleMaster = { enable ->
                        if (enable) {
                            if (!isMicGranted) {
                                checkAndRequestPermissions()
                            }
                            UltronAssistantManager.setMasterSwitch(true, context)
                        } else {
                            UltronAssistantManager.setMasterSwitch(false, context)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Below Core: "System Ready" / Current Status Label
                val currentSystemLabel = when {
                    !isMasterOn -> "Core Dormant - Switch ON to activate"
                    assistantState == AssistantState.WAKE_DETECTED -> "Wake Word Detected: \"Ultron\""
                    assistantState == AssistantState.PROCESSING_BACK -> "Executing System Back Action..."
                    assistantState == AssistantState.SPEAKING -> "Voicing: \"Yes, Tony.\""
                    assistantState == AssistantState.LISTENING -> "Active & Listening for \"Ultron\""
                    else -> "System Ready"
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = currentSystemLabel,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isMasterOn) UltronNeonCyan else Color.Gray,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Indicators: Microphone, Accessibility, Voice
                UltronStatusIndicators(
                    isMicGranted = isMicGranted,
                    isAccessibilityConnected = isAccessibilityConnected || isAccessibilityEnabledBySetting,
                    isVoiceReady = isVoiceReady,
                    isMasterOn = isMasterOn
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 9. SYSTEM STATUS PANEL
                UltronSystemStatusPanel(
                    isMasterOn = isMasterOn,
                    isVoiceOnline = isVoiceReady,
                    isMicGranted = isMicGranted,
                    isAccessibilityConnected = isAccessibilityConnected || isAccessibilityEnabledBySetting
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Diagnostic Testing Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { UltronAssistantManager.testVoicePreview() },
                        colors = ButtonDefaults.buttonColors(containerColor = UltronSurfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_quick_test_voice")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = UltronNeonCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Voice (\"Yes, Tony.\")", fontSize = 11.sp, color = UltronNeonCyan, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = { UltronAssistantManager.testBackAction() },
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, UltronCardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_quick_test_back")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccessibilityNew, contentDescription = null, tint = UltronTitanium, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Back Action", fontSize = 11.sp, color = UltronTitanium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { UltronAssistantManager.onWakeWordDetected("MANUAL_TEST") },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, UltronNeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulate_wake_action_btn")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = UltronNeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TEST WAKE SEQUENCE (\"Ultron\" → Back → \"Yes, Tony.\")",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = UltronNeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time Telemetry Terminal Logs
                UltronTerminalLog(
                    logs = logs,
                    onClearLogs = { UltronAssistantManager.clearLogs() }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Settings Screen Overlay
            AnimatedVisibility(
                visible = showSettingsScreen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                UltronSettingsScreen(
                    isMasterOn = isMasterOn,
                    onToggleMaster = { enable ->
                        if (enable) {
                            if (!isMicGranted) {
                                checkAndRequestPermissions()
                            }
                            UltronAssistantManager.setMasterSwitch(true, context)
                        } else {
                            UltronAssistantManager.setMasterSwitch(false, context)
                        }
                    },
                    isMicGranted = isMicGranted,
                    onRequestMicPermission = { checkAndRequestPermissions() },
                    isAccessibilityConnected = isAccessibilityConnected || isAccessibilityEnabledBySetting,
                    onOpenAccessibilitySettings = { openAccessibilitySettings() },
                    speechRate = voiceRate,
                    volume = voiceVolume,
                    pitch = voicePitch,
                    onVoiceSettingsChanged = { pitch, rate, volume ->
                        UltronAssistantManager.updateVoiceSettings(pitch, rate, volume)
                    },
                    onTestVoice = { UltronAssistantManager.testVoicePreview() },
                    onTestBack = { UltronAssistantManager.testBackAction() },
                    onCloseSettings = { showSettingsScreen = false }
                )
            }
        }
    }

    if (showSetupGuide) {
        UltronSetupGuideDialog(
            onDismiss = { showSetupGuide = false },
            onOpenAccessibilitySettings = { openAccessibilitySettings() }
        )
    }
}

/**
 * Compatible preview and testing composable
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "ULTRON AI Assistant - $name", modifier = modifier)
}
