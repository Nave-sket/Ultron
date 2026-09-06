package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class UltronVoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Cinematic deep masculine delivery parameters
    var pitch: Float = 0.76f
        set(value) {
            field = value
            tts?.setPitch(value)
        }

    var speechRate: Float = 0.90f
        set(value) {
            field = value
            tts?.setSpeechRate(value)
        }

    var volume: Float = 1.0f

    private var onSpeechDoneCallback: (() -> Unit)? = null
    private var lastSpokenText: String = "Yes, Tony."

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsInstance = tts ?: return
            val result = ttsInstance.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                configureCinematicVoice(ttsInstance)
                _isReady.value = true
                Log.i(TAG, "ULTRON Voice Engine initialized successfully")
            } else {
                Log.e(TAG, "English language not supported by TTS engine")
            }

            ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    UltronLipSyncEngine.startSpeakingAnimation(lastSpokenText)
                    Log.d(TAG, "Speech started: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    UltronLipSyncEngine.stopSpeakingAnimation()
                    Log.d(TAG, "Speech completed: $utteranceId")
                    onSpeechDoneCallback?.invoke()
                    onSpeechDoneCallback = null
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    UltronLipSyncEngine.stopSpeakingAnimation()
                    Log.e(TAG, "Speech error on $utteranceId")
                    onSpeechDoneCallback?.invoke()
                    onSpeechDoneCallback = null
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    UltronLipSyncEngine.stopSpeakingAnimation()
                    Log.e(TAG, "Speech error ($errorCode) on $utteranceId")
                    onSpeechDoneCallback?.invoke()
                    onSpeechDoneCallback = null
                }
            })
        } else {
            Log.e(TAG, "Failed to initialize TextToSpeech engine. Status: $status")
        }
    }

    private fun configureCinematicVoice(tts: TextToSpeech) {
        tts.setPitch(pitch)
        tts.setSpeechRate(speechRate)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        tts.setAudioAttributes(audioAttributes)

        try {
            val voices: Set<Voice>? = tts.voices
            if (!voices.isNullOrEmpty()) {
                // Select an authoritative deep/masculine English voice if available
                val masculineVoice = voices.firstOrNull { voice ->
                    val name = voice.name.lowercase(Locale.ROOT)
                    val lang = voice.locale.language.lowercase(Locale.ROOT)
                    lang == "en" && (name.contains("male") || name.contains("man") || name.contains("#m") || name.contains("en-us-x-sfg") || name.contains("en-us-x-iol"))
                } ?: voices.firstOrNull { it.locale.language == "en" && !it.isNetworkConnectionRequired }

                if (masculineVoice != null) {
                    tts.voice = masculineVoice
                    Log.i(TAG, "Selected voice: ${masculineVoice.name}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Voice enumeration not supported or failed: ${e.message}")
        }
    }

    /**
     * Speaks the target response (default: "Yes, Tony.").
     */
    fun speakResponse(text: String = "Yes, Tony.", onComplete: (() -> Unit)? = null) {
        val ttsInstance = tts
        if (ttsInstance == null || !_isReady.value) {
            Log.w(TAG, "TTS not ready, invoking onComplete immediately")
            onComplete?.invoke()
            return
        }

        lastSpokenText = text
        onSpeechDoneCallback = onComplete
        _isSpeaking.value = true

        ttsInstance.setPitch(pitch)
        ttsInstance.setSpeechRate(speechRate)

        val params = Bundle()
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume.coerceIn(0.1f, 1.0f))
        val utteranceId = "ultron_resp_${System.currentTimeMillis()}"

        ttsInstance.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        tts?.stop()
        UltronLipSyncEngine.stopSpeakingAnimation()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isReady.value = false
        _isSpeaking.value = false
    }

    companion object {
        private const val TAG = "UltronVoiceEngine"
    }
}
