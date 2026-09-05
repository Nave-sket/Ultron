package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class UltronWakeWordDetector(
    private val context: Context,
    private val onWakeWordDetected: (detectedWord: String) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _lastDetectedPhrase = MutableStateFlow("")
    val lastDetectedPhrase: StateFlow<String> = _lastDetectedPhrase.asStateFlow()

    @Volatile
    private var isEnabled = false

    @Volatile
    private var isPausedForSpeaking = false

    private val restartRunnable = Runnable {
        if (isEnabled && !isPausedForSpeaking) {
            startListeningInternal()
        }
    }

    fun start() {
        mainHandler.post {
            isEnabled = true
            isPausedForSpeaking = false
            initRecognizerIfNeeded()
            startListeningInternal()
        }
    }

    fun stop() {
        mainHandler.post {
            isEnabled = false
            isPausedForSpeaking = false
            mainHandler.removeCallbacks(restartRunnable)
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping recognizer: ${e.message}")
            }
            _isListening.value = false
            _rmsLevel.value = 0f
        }
    }

    fun pauseForSpeech() {
        mainHandler.post {
            isPausedForSpeaking = true
            mainHandler.removeCallbacks(restartRunnable)
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.w(TAG, "Error pausing recognizer: ${e.message}")
            }
            _isListening.value = false
            _rmsLevel.value = 0f
        }
    }

    fun resumeAfterSpeech() {
        mainHandler.postDelayed({
            isPausedForSpeaking = false
            if (isEnabled) {
                startListeningInternal()
            }
        }, 300L) // Small delay to let audio channel clear
    }

    fun destroy() {
        mainHandler.post {
            stop()
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
    }

    private fun initRecognizerIfNeeded() {
        if (speechRecognizer == null) {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Log.e(TAG, "SpeechRecognizer is not available on this device")
                return
            }
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }
        }
    }

    private fun startListeningInternal() {
        if (!isEnabled || isPausedForSpeaking) return

        try {
            initRecognizerIfNeeded()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                // Optimize for wake word response latency
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 800L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 800L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 800L)
            }
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening: ${e.message}")
            scheduleRestart(1000L)
        }
    }

    private fun scheduleRestart(delayMillis: Long = 250L) {
        mainHandler.removeCallbacks(restartRunnable)
        if (isEnabled && !isPausedForSpeaking) {
            mainHandler.postDelayed(restartRunnable, delayMillis)
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
            }

            override fun onBeginningOfSpeech() {
                _isListening.value = true
            }

            override fun onRmsChanged(rmsdB: Float) {
                // rmsdB typically ranges from -2 to 10
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                _rmsLevel.value = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
                _rmsLevel.value = 0f
            }

            override fun onError(error: Int) {
                _isListening.value = false
                _rmsLevel.value = 0f
                Log.d(TAG, "SpeechRecognizer error code: $error")
                // Errors like NO_MATCH or SPEECH_TIMEOUT are normal during continuous listening
                val delay = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> 150L
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> 400L
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> 2000L
                    else -> 500L
                }
                scheduleRestart(delay)
            }

            override fun onResults(results: Bundle?) {
                handleSpeechResults(results)
                scheduleRestart(200L)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val detected = handleSpeechResults(partialResults)
                if (detected) {
                    // Stop current recognizer immediately if wake word was matched in partial results
                    try {
                        speechRecognizer?.stopListening()
                    } catch (e: Exception) {
                        Log.w(TAG, "Error stopping after partial match: ${e.message}")
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun handleSpeechResults(resultsBundle: Bundle?): Boolean {
        val matches = resultsBundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return false
        for (candidate in matches) {
            val trimmed = candidate.trim()
            if (trimmed.isNotEmpty()) {
                _lastDetectedPhrase.value = trimmed
            }
            if (matchesWakeWord(candidate)) {
                Log.i(TAG, "Wake word 'Ultron' detected in: \"$candidate\"")
                onWakeWordDetected(candidate)
                return true
            }
        }
        return false
    }

    companion object {
        private const val TAG = "UltronWakeDetector"

        /**
         * Robust phonetic and keyword matcher for "Ultron".
         */
        fun matchesWakeWord(rawText: String): Boolean {
            val text = rawText.lowercase(Locale.ROOT)
                .replace(Regex("[^a-z0-9\\s]"), " ")
                .trim()

            // 1. Direct word token check
            val words = text.split(Regex("\\s+"))
            val targetTokens = setOf("ultron", "altron", "eltron", "voltron")
            if (words.any { it in targetTokens }) {
                return true
            }

            // 2. Multi-word phrase variants
            val phrases = listOf(
                "all tron",
                "ul tron",
                "ultra on",
                "ultra",
                "old tron",
                "ole tron",
                "el tron",
                "al tron",
                "otron"
            )
            return phrases.any { text.contains(it) }
        }
    }
}
