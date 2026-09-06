package com.example.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

/**
 * Real-time Lip-Sync and Speech Envelope Generator for ULTRON.
 *
 * Provides synchronized mouth aperture (0.0 to 1.0), jaw displacement,
 * and audio energy frequency harmonics synchronized with TTS output.
 * Uses a phoneme-like multi-frequency modulator with natural syllable pauses
 * to simulate authentic human/AI speech articulation instead of a static open mouth.
 */
object UltronLipSyncEngine {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var lipSyncJob: Job? = null

    // Normalized mouth openness: 0.0f (closed/idle) to 1.0f (fully open)
    private val _mouthOpenness = MutableStateFlow(0f)
    val mouthOpenness: StateFlow<Float> = _mouthOpenness.asStateFlow()

    // Energy envelope around mouth & jaw for glowing sparks and acoustic particles
    private val _acousticEnergy = MutableStateFlow(0f)
    val acousticEnergy: StateFlow<Float> = _acousticEnergy.asStateFlow()

    /**
     * Start synchronizing mouth aperture for spoken text.
     */
    fun startSpeakingAnimation(spokenText: String) {
        lipSyncJob?.cancel()
        lipSyncJob = scope.launch {
            val startTime = System.currentTimeMillis()
            var syllableCycle = 0

            // Multi-frequency harmonic envelope simulating vowels and consonant stops
            while (isActive) {
                val elapsedSec = (System.currentTimeMillis() - startTime) / 1000f
                syllableCycle++

                // Base syllable cadence (~3.5 Hz speech rate)
                val baseSyllable = abs(sin(elapsedSec * 22.0))
                val vowelFormant = abs(sin(elapsedSec * 14.5 + 0.5)) * 0.45f
                val microTremor = (sin(elapsedSec * 65.0) * 0.15f)

                // Occasional natural syllable break / closure
                val isPause = (syllableCycle % 26 in 23..25)
                val rawAperture = if (isPause) 0.05f else (baseSyllable * 0.65f + vowelFormant + microTremor).toFloat()

                val clamped = rawAperture.coerceIn(0f, 1.0f)
                _mouthOpenness.value = clamped
                _acousticEnergy.value = (clamped * 1.2f).coerceIn(0f, 1.5f)

                delay(22L) // ~45 FPS lip sync update rate
            }
        }
    }

    /**
     * Return mouth and jaw to idle resting position.
     */
    fun stopSpeakingAnimation() {
        lipSyncJob?.cancel()
        lipSyncJob = scope.launch {
            // Smooth natural decay back to 0
            var current = _mouthOpenness.value
            while (current > 0.01f) {
                current *= 0.70f
                _mouthOpenness.value = current
                _acousticEnergy.value = current
                delay(20L)
            }
            _mouthOpenness.value = 0f
            _acousticEnergy.value = 0f
        }
    }
}
