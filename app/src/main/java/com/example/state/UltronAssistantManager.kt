package com.example.state

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.audio.UltronVoiceEngine
import com.example.service.UltronAccessibilityService
import com.example.service.UltronListeningService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AssistantState {
    OFF,
    STANDBY,
    LISTENING,
    WAKE_DETECTED,
    PROCESSING_BACK,
    SPEAKING
}

data class UltronLogItem(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String,
    val message: String,
    val isSuccess: Boolean = true,
    val tag: String = "INFO"
)

object UltronAssistantManager {

    private const val TAG = "UltronAssistantMgr"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private var voiceEngine: UltronVoiceEngine? = null

    private val _assistantState = MutableStateFlow(AssistantState.OFF)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _isMasterSwitchOn = MutableStateFlow(false)
    val isMasterSwitchOn: StateFlow<Boolean> = _isMasterSwitchOn.asStateFlow()

    private val _isAccessibilityConnected = MutableStateFlow(false)
    val isAccessibilityConnected: StateFlow<Boolean> = _isAccessibilityConnected.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private val _voicePitch = MutableStateFlow(0.76f)
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _voiceRate = MutableStateFlow(0.90f)
    val voiceRate: StateFlow<Float> = _voiceRate.asStateFlow()

    private val _voiceVolume = MutableStateFlow(1.0f)
    val voiceVolume: StateFlow<Float> = _voiceVolume.asStateFlow()

    private val _isVoiceReady = MutableStateFlow(false)
    val isVoiceReady: StateFlow<Boolean> = _isVoiceReady.asStateFlow()

    private val _logs = MutableStateFlow<List<UltronLogItem>>(emptyList())
    val logs: StateFlow<List<UltronLogItem>> = _logs.asStateFlow()

    private var appContext: Context? = null

    fun initialize(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        val engine = UltronVoiceEngine(context).apply {
            pitch = _voicePitch.value
            speechRate = _voiceRate.value
            volume = _voiceVolume.value
        }
        voiceEngine = engine

        scope.launch {
            engine.isReady.collect { ready ->
                _isVoiceReady.value = ready
            }
        }

        // Collect accessibility connection updates
        scope.launch {
            UltronAccessibilityService.isServiceConnected.collect { connected ->
                _isAccessibilityConnected.value = connected
                if (connected) {
                    addLog("Accessibility Service connected and ready", isSuccess = true, tag = "SYSTEM")
                } else {
                    addLog("Accessibility Service is offline / not enabled", isSuccess = false, tag = "SYSTEM")
                }
            }
        }

        addLog("ULTRON AI Assistant initialized. Core systems online.", isSuccess = true, tag = "SYSTEM")
    }

    fun updateRms(level: Float) {
        _rmsLevel.value = level
    }

    fun updateRecognizedText(text: String) {
        _lastRecognizedText.value = text
    }

    fun updateVoiceSettings(pitch: Float, rate: Float, volume: Float = _voiceVolume.value) {
        _voicePitch.value = pitch
        _voiceRate.value = rate
        _voiceVolume.value = volume
        voiceEngine?.pitch = pitch
        voiceEngine?.speechRate = rate
        voiceEngine?.volume = volume
    }

    fun setMasterSwitch(enabled: Boolean, context: Context) {
        _isMasterSwitchOn.value = enabled
        val intent = Intent(context, UltronListeningService::class.java).apply {
            action = if (enabled) {
                UltronListeningService.ACTION_START_LISTENING
            } else {
                UltronListeningService.ACTION_STOP_LISTENING
            }
        }

        if (enabled) {
            _assistantState.value = AssistantState.LISTENING
            addLog("Master switch engaged. Starting foreground voice detection...", isSuccess = true, tag = "SERVICE")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            _assistantState.value = AssistantState.OFF
            _rmsLevel.value = 0f
            addLog("Master switch disengaged. Listening suspended.", isSuccess = true, tag = "SERVICE")
            context.stopService(intent)
        }
    }

    /**
     * Executes the core CUJ:
     * 1. Detects "Ultron"
     * 2. Shows activation animation ("ULTRON ACTIVATED")
     * 3. Triggers Android Back action via AccessibilityService
     * 4. Speaks: "Yes, Tony."
     */
    fun onWakeWordDetected(source: String = "VOICE") {
        val context = appContext ?: return
        triggerHaptic(context)

        addLog("Wake word 'Ultron' detected via $source", isSuccess = true, tag = "WAKE")

        scope.launch {
            // Activation state: Core expands, rings spin fast, displays "ULTRON ACTIVATED"
            _assistantState.value = AssistantState.WAKE_DETECTED
            kotlinx.coroutines.delay(400L)

            // Step 2: Trigger Android Back action
            _assistantState.value = AssistantState.PROCESSING_BACK
            val backSuccess = UltronAccessibilityService.performBackAction()

            if (backSuccess) {
                addLog("Android Back action executed successfully", isSuccess = true, tag = "ACTION")
                _assistantState.value = AssistantState.SPEAKING

                // Step 3: Speak exactly: "Yes, Tony."
                voiceEngine?.speakResponse("Yes, Tony.") {
                    scope.launch {
                        addLog("ULTRON response voiced: \"Yes, Tony.\"", isSuccess = true, tag = "VOICE")
                        _assistantState.value = if (_isMasterSwitchOn.value) AssistantState.LISTENING else AssistantState.STANDBY
                        UltronListeningService.resumeDetectorAfterSpeech()
                    }
                }
            } else {
                val isEnabled = UltronAccessibilityService.isAccessibilityServiceEnabled(context)
                val reason = if (!isEnabled) {
                    "Accessibility Service is NOT enabled in system settings"
                } else {
                    "Service running but Back action rejected by OS"
                }
                addLog("Failed to perform Back: $reason", isSuccess = false, tag = "ERROR")
                _assistantState.value = if (_isMasterSwitchOn.value) AssistantState.LISTENING else AssistantState.STANDBY
                UltronListeningService.resumeDetectorAfterSpeech()
            }
        }
    }

    /**
     * Test button to verify the voice delivery: "Yes, Tony."
     */
    fun testVoicePreview() {
        _assistantState.value = AssistantState.SPEAKING
        addLog("Testing ULTRON voice response: \"Yes, Tony.\"", isSuccess = true, tag = "TEST")
        voiceEngine?.speakResponse("Yes, Tony.") {
            scope.launch {
                _assistantState.value = if (_isMasterSwitchOn.value) AssistantState.LISTENING else AssistantState.STANDBY
            }
        }
    }

    /**
     * Test Back action directly via AccessibilityService
     */
    fun testBackAction(): Boolean {
        addLog("Manual test: Triggering Back action via Accessibility...", isSuccess = true, tag = "TEST")
        val success = UltronAccessibilityService.performBackAction()
        if (success) {
            addLog("Manual Back action succeeded", isSuccess = true, tag = "ACTION")
        } else {
            addLog("Manual Back action failed. Verify Accessibility permission is enabled.", isSuccess = false, tag = "ERROR")
        }
        return success
    }

    fun addLog(message: String, isSuccess: Boolean = true, tag: String = "INFO") {
        val newEntry = UltronLogItem(
            timestamp = timeFormat.format(Date()),
            message = message,
            isSuccess = isSuccess,
            tag = tag
        )
        _logs.value = (listOf(newEntry) + _logs.value).take(50)
        Log.d(TAG, "[$tag] $message")
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    private fun triggerHaptic(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50L)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Haptic feedback unavailable: ${e.message}")
        }
    }
}
