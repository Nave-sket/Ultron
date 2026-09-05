package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.state.AssistantState
import com.example.ui.theme.UltronCardBorder
import com.example.ui.theme.UltronCrimson
import com.example.ui.theme.UltronCrimsonGlow
import com.example.ui.theme.UltronCyanGlow
import com.example.ui.theme.UltronNeonCyan
import com.example.ui.theme.UltronSurfaceVariant
import com.example.ui.theme.UltronTitanium
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Advanced 3D Holographic ULTRON AI Core
 *
 * Implements:
 * - Genuine 3D depth via perspective matrix projection (camera distance = 750f)
 * - 360-degree continuous motion across Y, X, and Z axes
 * - Independent multi-speed, multi-inclination 3D rings
 * - Real-time phone gyroscope / accelerometer parallax tracking with software fallback
 * - Natural 3D floating translation (Left-Right, Up-Down, Forward-Backward)
 * - 3D orbiting satellites/energy nodes with front/back depth occlusion
 * - 3D particle cloud with perspective scaling and brightness modulation
 * - 3D audio waveform ribbon wrapping around the core
 * - Holographic volumetric scanlines and corner HUD tracking brackets
 * - Cinematic activation surge sequence ("ULTRON ACTIVATED")
 */
@Composable
fun UltronReactorCore(
    assistantState: AssistantState,
    isMasterOn: Boolean,
    rmsLevel: Float,
    onToggleMaster: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Gyroscope / accelerometer parallax state
    val sensorState by rememberParallaxSensorState()

    val infiniteTransition = rememberInfiniteTransition(label = "3d_hologram_core")

    // Dynamic speeds depending on state
    val speedMultiplier = when {
        assistantState == AssistantState.WAKE_DETECTED -> 3.2f
        assistantState == AssistantState.LISTENING -> 1.75f
        assistantState == AssistantState.SPEAKING -> 1.4f
        isMasterOn -> 1.0f
        else -> 0.35f
    }

    // 1. 360-degree Y-axis primary rotation (yaw)
    val baseRotDuration = (14000 / speedMultiplier).toInt().coerceAtLeast(1200)
    val coreYawDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(baseRotDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "core_yaw"
    )

    // 2. Slow X-axis oscillation (pitch tilt)
    val pitchOscDuration = (8000 / speedMultiplier).toInt().coerceAtLeast(1500)
    val corePitchDeg by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(pitchOscDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pitch"
    )

    // 3. Ring 2 (Gimbal) independent rotation
    val ring2Duration = (10500 / speedMultiplier).toInt().coerceAtLeast(1000)
    val ring2AngleDeg by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(ring2Duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2_angle"
    )

    // 4. Ring 3 (Equatorial telemetry) counter-rotation
    val ring3Duration = (18000 / speedMultiplier).toInt().coerceAtLeast(1800)
    val ring3AngleDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(ring3Duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring3_angle"
    )

    // 5. 3D Floating Motion: Left-Right (X)
    val floatX by infiniteTransition.animateFloat(
        initialValue = -16f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_x"
    )

    // 6. 3D Floating Motion: Up-Down (Y)
    val floatY by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    // 7. 3D Floating Motion: Forward-Backward (Z depth)
    val baseFloatZ by infiniteTransition.animateFloat(
        initialValue = -28f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(6300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_z"
    )

    // 8. 3D Orbiting Satellites
    val sat1AngleDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween((7200 / speedMultiplier).toInt().coerceAtLeast(800), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sat1_angle"
    )

    val sat2AngleDeg by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween((9400 / speedMultiplier).toInt().coerceAtLeast(900), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sat2_angle"
    )

    // 9. Particle cloud animation phase (0..1 loop)
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (assistantState == AssistantState.LISTENING) 1600 else 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )

    // 10. Scanline sweep phase (0..1)
    val scanlinePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline_phase"
    )

    // 11. Core energetic breathing pulse
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (assistantState == AssistantState.WAKE_DETECTED) 1.05f else 0.96f,
        targetValue = if (assistantState == AssistantState.WAKE_DETECTED) 1.24f else if (assistantState == AssistantState.SPEAKING) 1.10f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (assistantState == AssistantState.WAKE_DETECTED) 400 else if (assistantState == AssistantState.LISTENING) 750 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    // Color theme based on state
    val isCrimsonState = assistantState == AssistantState.WAKE_DETECTED ||
            assistantState == AssistantState.PROCESSING_BACK ||
            assistantState == AssistantState.SPEAKING

    val primaryColor = when {
        !isMasterOn -> Color(0xFF434E5E)
        isCrimsonState -> UltronCrimson
        else -> UltronNeonCyan
    }

    val glowColor = when {
        !isMasterOn -> Color.Transparent
        isCrimsonState -> UltronCrimsonGlow
        else -> UltronCyanGlow
    }

    // Static 3D particle positions in spherical shell (generated once)
    val particleSeedList = remember {
        val list = mutableListOf<ParticleSeed>()
        val count = 28
        for (i in 0 until count) {
            val theta = (i * (2f * PI.toFloat() / count))
            val phi = (((i * 7) % count) - count / 2f) / (count / 2f) * (PI.toFloat() * 0.42f)
            val radRatio = 0.55f + ((i * 13) % 10) * 0.05f
            list.add(ParticleSeed(theta, phi, radRatio, size = if (i % 3 == 0) 3.5f else 2.2f))
        }
        list
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Holographic 3D Viewport Box
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(310.dp)
                .padding(4.dp)
                .testTag("ultron_3d_hologram_core")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = (size.minDimension / 2f) - 22f

                // Combine floating translation + gyroscope parallax
                // Parallax displacement: near layers move more, far layers move less
                val gyroX = if (sensorState.hasHardwareSensor) sensorState.tiltY * 34f else floatX * 0.55f
                val gyroY = if (sensorState.hasHardwareSensor) -sensorState.tiltX * 28f else floatY * 0.55f

                // Euler tilt angles in radians
                val pitchRad = Math.toRadians((corePitchDeg + (if (sensorState.hasHardwareSensor) -sensorState.tiltX * 18f else 0f)).toDouble()).toFloat()
                val yawRad = Math.toRadians(coreYawDeg.toDouble()).toFloat()
                val rollRad = Math.toRadians((if (sensorState.hasHardwareSensor) sensorState.tiltY * 12f else sin(coreYawDeg * 0.015) * 4f).toDouble()).toFloat()

                // Forward surge on activation
                val activationSurgeZ = if (assistantState == AssistantState.WAKE_DETECTED) 65f else 0f
                val finalFloatZ = baseFloatZ + activationSurgeZ

                val coreCenter = Offset(
                    center.x + floatX * 0.6f + gyroX * 0.7f,
                    center.y + floatY * 0.6f + gyroY * 0.7f
                )

                // ----------------------------------------------------
                // LAYER 0: Background Holographic Grid & Deep Glow
                // ----------------------------------------------------
                if (isMasterOn) {
                    val bgParallax = Offset(center.x - gyroX * 0.25f, center.y - gyroY * 0.25f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor.copy(alpha = if (isCrimsonState) 0.35f else 0.22f), Color.Transparent),
                            center = bgParallax,
                            radius = baseRadius * 1.45f
                        ),
                        radius = baseRadius * 1.45f,
                        center = bgParallax
                    )

                    // Subtle background depth rings
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.08f),
                        radius = baseRadius * 1.15f,
                        center = bgParallax,
                        style = Stroke(width = 1f)
                    )
                }

                // ----------------------------------------------------
                // 3D RING DEFINITIONS
                // ----------------------------------------------------
                // Outer Energy Ring (Radius 1.08x, expands on activation, slow counter-spin)
                val outerExpansion = if (assistantState == AssistantState.WAKE_DETECTED) 1.25f else 1.0f
                val outerRadius = baseRadius * 1.02f * outerExpansion
                val outerPitch = pitchRad - 0.40f
                val outerYaw = -yawRad * 0.7f
                val outerRoll = rollRad + 0.20f

                // Middle Gimbal Ring (Radius 0.82x, tilted at +45 deg, medium spin)
                val gimbalRadius = baseRadius * 0.82f
                val gimbalPitch = pitchRad + 0.65f
                val gimbalYaw = Math.toRadians(ring2AngleDeg.toDouble()).toFloat()
                val gimbalRoll = rollRad - 0.35f

                // Equatorial Telemetry Ring (Radius 0.68x, horizontal, fine ticks)
                val eqRadius = baseRadius * 0.68f
                val eqPitch = pitchRad * 0.4f
                val eqYaw = Math.toRadians(ring3AngleDeg.toDouble()).toFloat()
                val eqRoll = rollRad * 0.3f

                // Inner Shield Ring (Radius 0.48x, fast counter-spin)
                val innerRadius = baseRadius * 0.48f
                val innerPitch = pitchRad + 0.25f
                val innerYaw = -yawRad * 1.3f
                val innerRoll = rollRad

                // ----------------------------------------------------
                // LAYER 1: BACK SEGMENTS OF 3D RINGS (z < 0)
                // ----------------------------------------------------
                draw3DRing(
                    center = coreCenter,
                    radius = outerRadius,
                    pitch = outerPitch,
                    yaw = outerYaw,
                    roll = outerRoll,
                    centerZ = finalFloatZ,
                    isFront = false,
                    color = primaryColor.copy(alpha = if (isMasterOn) 0.22f else 0.08f),
                    strokeWidth = 1.6f,
                    dashCount = 4,
                    gapRatio = 0.3f
                )

                draw3DRing(
                    center = coreCenter,
                    radius = gimbalRadius,
                    pitch = gimbalPitch,
                    yaw = gimbalYaw,
                    roll = gimbalRoll,
                    centerZ = finalFloatZ,
                    isFront = false,
                    color = primaryColor.copy(alpha = if (isMasterOn) 0.28f else 0.10f),
                    strokeWidth = 1.8f,
                    dashCount = 3,
                    gapRatio = 0.25f
                )

                draw3DRing(
                    center = coreCenter,
                    radius = eqRadius,
                    pitch = eqPitch,
                    yaw = eqYaw,
                    roll = eqRoll,
                    centerZ = finalFloatZ,
                    isFront = false,
                    color = primaryColor.copy(alpha = if (isMasterOn) 0.32f else 0.12f),
                    strokeWidth = 1.4f,
                    dashCount = 1,
                    gapRatio = 0f
                )

                // ----------------------------------------------------
                // LAYER 2: BACK 3D PARTICLES & SATELLITES (z < 0)
                // ----------------------------------------------------
                if (isMasterOn) {
                    render3DParticles(
                        center = coreCenter,
                        baseRadius = baseRadius,
                        seeds = particleSeedList,
                        phase = particlePhase,
                        isListening = assistantState == AssistantState.LISTENING,
                        isActivation = assistantState == AssistantState.WAKE_DETECTED,
                        pitch = pitchRad,
                        yaw = yawRad,
                        roll = rollRad,
                        centerZ = finalFloatZ,
                        isFront = false,
                        color = primaryColor
                    )

                    // Satellite 1: Passing behind core
                    render3DSatellite(
                        center = coreCenter,
                        orbitRadius = baseRadius * 0.92f,
                        angleDeg = sat1AngleDeg,
                        orbitPitch = pitchRad + 0.55f,
                        orbitRoll = rollRad - 0.40f,
                        centerZ = finalFloatZ,
                        isFront = false,
                        color = primaryColor
                    )

                    // Satellite 2: Passing behind core
                    render3DSatellite(
                        center = coreCenter,
                        orbitRadius = baseRadius * 0.75f,
                        angleDeg = sat2AngleDeg,
                        orbitPitch = pitchRad - 0.70f,
                        orbitRoll = rollRad + 0.60f,
                        centerZ = finalFloatZ,
                        isFront = false,
                        color = primaryColor
                    )
                }

                // ----------------------------------------------------
                // LAYER 3: CENTRAL 3D VOLUMETRIC AI CORE SPHERE
                // ----------------------------------------------------
                val coreZ = finalFloatZ
                val coreFactor = projectFactor(coreZ)
                val coreRadius = (baseRadius * 0.34f * pulseScale * coreFactor)
                    .coerceAtLeast(18f)

                // Core Atmospheric Outer Plasma Glow
                if (isMasterOn) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = if (isCrimsonState) 0.65f else 0.45f),
                                glowColor.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = coreCenter,
                            radius = coreRadius * 1.75f
                        ),
                        radius = coreRadius * 1.75f,
                        center = coreCenter
                    )
                }

                // 3D Spherical Latitude & Longitude Hologram Ribs
                render3DSphereWireframe(
                    center = coreCenter,
                    radius = coreRadius,
                    pitch = pitchRad,
                    yaw = yawRad,
                    roll = rollRad,
                    color = primaryColor.copy(alpha = if (isMasterOn) 0.65f else 0.20f)
                )

                // Core Robotic Iris & Glowing Aperture Lens
                drawCircle(
                    color = Color(0xFF080C14),
                    radius = coreRadius * 0.72f,
                    center = coreCenter
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isMasterOn) 0.85f else 0.25f),
                            primaryColor.copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = coreCenter,
                        radius = coreRadius * 0.72f
                    ),
                    radius = coreRadius * 0.72f,
                    center = coreCenter
                )

                // Center aperture pupil dot
                drawCircle(
                    color = if (isMasterOn) Color.White else Color.Gray,
                    radius = (coreRadius * 0.18f).coerceAtLeast(3f),
                    center = coreCenter
                )

                // ----------------------------------------------------
                // LAYER 4: 3D AUDIO WAVEFORM RIBBON (Listening/Speaking)
                // ----------------------------------------------------
                if (isMasterOn && (assistantState == AssistantState.LISTENING || assistantState == AssistantState.SPEAKING)) {
                    val waveAmp = if (assistantState == AssistantState.SPEAKING) 24f else (rmsLevel * 35f).coerceIn(4f, 32f)
                    render3DAudioRibbon(
                        center = coreCenter,
                        radius = coreRadius * 1.25f,
                        waveAmp = waveAmp,
                        pitch = pitchRad,
                        yaw = yawRad,
                        roll = rollRad,
                        centerZ = finalFloatZ,
                        color = primaryColor
                    )
                }

                // ----------------------------------------------------
                // LAYER 5: FRONT 3D PARTICLES & SATELLITES (z >= 0)
                // ----------------------------------------------------
                if (isMasterOn) {
                    render3DParticles(
                        center = coreCenter,
                        baseRadius = baseRadius,
                        seeds = particleSeedList,
                        phase = particlePhase,
                        isListening = assistantState == AssistantState.LISTENING,
                        isActivation = assistantState == AssistantState.WAKE_DETECTED,
                        pitch = pitchRad,
                        yaw = yawRad,
                        roll = rollRad,
                        centerZ = finalFloatZ,
                        isFront = true,
                        color = primaryColor
                    )

                    // Satellite 1: In front of core
                    render3DSatellite(
                        center = coreCenter,
                        orbitRadius = baseRadius * 0.92f,
                        angleDeg = sat1AngleDeg,
                        orbitPitch = pitchRad + 0.55f,
                        orbitRoll = rollRad - 0.40f,
                        centerZ = finalFloatZ,
                        isFront = true,
                        color = primaryColor
                    )

                    // Satellite 2: In front of core
                    render3DSatellite(
                        center = coreCenter,
                        orbitRadius = baseRadius * 0.75f,
                        angleDeg = sat2AngleDeg,
                        orbitPitch = pitchRad - 0.70f,
                        orbitRoll = rollRad + 0.60f,
                        centerZ = finalFloatZ,
                        isFront = true,
                        color = primaryColor
                    )
                }

                // ----------------------------------------------------
                // LAYER 6: FRONT SEGMENTS OF 3D RINGS (z >= 0)
                // ----------------------------------------------------
                // Inner Shield Ring front
                draw3DRing(
                    center = coreCenter,
                    radius = innerRadius,
                    pitch = innerPitch,
                    yaw = innerYaw,
                    roll = innerRoll,
                    centerZ = finalFloatZ,
                    isFront = true,
                    color = primaryColor.copy(alpha = if (isMasterOn) 0.85f else 0.30f),
                    strokeWidth = 2.4f,
                    dashCount = 6,
                    gapRatio = 0.2f
                )

                // Equatorial Telemetry Ring front (with ticks)
                draw3DRing(
                    center = coreCenter,
                    radius = eqRadius,
                    pitch = eqPitch,
                    yaw = eqYaw,
                    roll = eqRoll,
                    centerZ = finalFloatZ,
                    isFront = true,
                    color = primaryColor.copy(alpha = if (isMasterOn) 0.95f else 0.35f),
                    strokeWidth = 2.8f,
                    dashCount = 1,
                    gapRatio = 0f,
                    drawTicks = isMasterOn
                )

                // Middle Gimbal Ring front
                draw3DRing(
                    center = coreCenter,
                    radius = gimbalRadius,
                    pitch = gimbalPitch,
                    yaw = gimbalYaw,
                    roll = gimbalRoll,
                    centerZ = finalFloatZ,
                    isFront = true,
                    color = primaryColor.copy(alpha = if (isMasterOn) 0.95f else 0.35f),
                    strokeWidth = 3.6f,
                    dashCount = 3,
                    gapRatio = 0.25f,
                    drawNodes = isMasterOn
                )

                // Outer Energy Ring front
                draw3DRing(
                    center = coreCenter,
                    radius = outerRadius,
                    pitch = outerPitch,
                    yaw = outerYaw,
                    roll = outerRoll,
                    centerZ = finalFloatZ,
                    isFront = true,
                    color = primaryColor.copy(alpha = if (isMasterOn) 1.0f else 0.40f),
                    strokeWidth = 4.2f,
                    dashCount = 4,
                    gapRatio = 0.3f,
                    drawNodes = isMasterOn
                )

                // ----------------------------------------------------
                // LAYER 7: HOLOGRAPHIC DIGITAL SCANLINES & HUD BRACKETS
                // ----------------------------------------------------
                if (isMasterOn) {
                    val scanlineY = size.height * scanlinePhase
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                primaryColor.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(20f, scanlineY),
                        end = Offset(size.width - 20f, scanlineY),
                        strokeWidth = 1.5f
                    )

                    // 4 Corner HUD Brackets
                    val bSize = 14f
                    val bPad = 12f
                    val bracketAlpha = 0.35f
                    // Top-Left
                    drawLine(primaryColor.copy(alpha = bracketAlpha), Offset(bPad, bPad), Offset(bPad + bSize, bPad), 2f)
                    drawLine(primaryColor.copy(alpha = bracketAlpha), Offset(bPad, bPad), Offset(bPad, bPad + bSize), 2f)
                    // Top-Right
                    drawLine(primaryColor.copy(alpha = bracketAlpha), Offset(size.width - bPad, bPad), Offset(size.width - bPad - bSize, bPad), 2f)
                    drawLine(primaryColor.copy(alpha = bracketAlpha), Offset(size.width - bPad, bPad), Offset(size.width - bPad, bPad + bSize), 2f)
                    // Bottom-Left
                    drawLine(primaryColor.copy(alpha = bracketAlpha), Offset(bPad, size.height - bPad), Offset(bPad + bSize, size.height - bPad), 2f)
                    drawLine(primaryColor.copy(alpha = bracketAlpha), Offset(bPad, size.height - bPad), Offset(bPad, size.height - bPad - bSize), 2f)
                    // Bottom-Right
                    drawLine(primaryColor.copy(alpha = bracketAlpha), Offset(size.width - bPad, size.height - bPad), Offset(size.width - bPad - bSize, size.height - bPad), 2f)
                    drawLine(primaryColor.copy(alpha = bracketAlpha), Offset(size.width - bPad, size.height - bPad), Offset(size.width - bPad, size.height - bPad - bSize), 2f)
                }
            }

            // Central Holographic Text Overlay & Mic Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                if (isMasterOn && assistantState == AssistantState.LISTENING) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microphone Active",
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "ULTRON",
                    color = primaryColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp
                )

                val readoutText = when {
                    !isMasterOn -> "OFFLINE"
                    assistantState == AssistantState.WAKE_DETECTED -> "ULTRON ACTIVATED"
                    assistantState == AssistantState.PROCESSING_BACK -> "BACK TRIGGERED"
                    assistantState == AssistantState.SPEAKING -> "YES, TONY."
                    assistantState == AssistantState.LISTENING -> "LISTENING..."
                    else -> "SYSTEM READY"
                }

                Text(
                    text = readoutText,
                    color = if (isMasterOn) UltronTitanium else Color.Gray,
                    fontSize = if (readoutText.length > 12) 10.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large Futuristic Master ON/OFF Switch
        MasterPowerSwitch(
            isOn = isMasterOn,
            onToggle = onToggleMaster
        )
    }
}

// -------------------------------------------------------------------------------------------------
// 3D MATHEMATICAL PROJECTION HELPERS
// -------------------------------------------------------------------------------------------------

private const val CAMERA_DIST = 750f

private fun projectFactor(z: Float): Float {
    return CAMERA_DIST / (CAMERA_DIST - z).coerceAtLeast(120f)
}

/**
 * 3D Euler rotation around Y (yaw), X (pitch), and Z (roll) axes.
 */
private fun rotate3D(
    x: Float, y: Float, z: Float,
    pitch: Float, yaw: Float, roll: Float
): FloatArray {
    // 1. Yaw (Y-axis)
    val cosY = cos(yaw)
    val sinY = sin(yaw)
    val x1 = x * cosY + z * sinY
    val y1 = y
    val z1 = -x * sinY + z * cosY

    // 2. Pitch (X-axis)
    val cosX = cos(pitch)
    val sinX = sin(pitch)
    val x2 = x1
    val y2 = y1 * cosX - z1 * sinX
    val z2 = y1 * sinX + z1 * cosX

    // 3. Roll (Z-axis)
    val cosZ = cos(roll)
    val sinZ = sin(roll)
    val x3 = x2 * cosZ - y2 * sinZ
    val y3 = x2 * sinZ + y2 * cosZ
    val z3 = z2

    return floatArrayOf(x3, y3, z3)
}

/**
 * Draws either the front (z >= 0) or back (z < 0) half of an independent 3D ring.
 */
private fun DrawScope.draw3DRing(
    center: Offset,
    radius: Float,
    pitch: Float,
    yaw: Float,
    roll: Float,
    centerZ: Float,
    isFront: Boolean,
    color: Color,
    strokeWidth: Float,
    dashCount: Int = 1,
    gapRatio: Float = 0f,
    drawTicks: Boolean = false,
    drawNodes: Boolean = false
) {
    val steps = 64
    val stepAngle = (2f * PI.toFloat()) / steps

    for (i in 0 until steps) {
        val a1 = i * stepAngle
        val a2 = (i + 1) * stepAngle

        // Dash logic
        if (dashCount > 1 && gapRatio > 0f) {
            val segFraction = (a1 / (2f * PI.toFloat()) * dashCount) % 1f
            if (segFraction > (1f - gapRatio)) continue
        }

        val p1 = rotate3D(radius * cos(a1), 0f, radius * sin(a1), pitch, yaw, roll)
        val p2 = rotate3D(radius * cos(a2), 0f, radius * sin(a2), pitch, yaw, roll)

        val z1 = p1[2] + centerZ
        val z2 = p2[2] + centerZ
        val avgZ = (z1 + z2) / 2f

        val segmentIsFront = avgZ >= 0f
        if (segmentIsFront != isFront) continue

        val factor1 = projectFactor(z1)
        val factor2 = projectFactor(z2)

        val s1 = Offset(center.x + p1[0] * factor1, center.y + p1[1] * factor1)
        val s2 = Offset(center.x + p2[0] * factor2, center.y + p2[1] * factor2)

        val depthAlpha = if (isFront) (0.7f + 0.3f * (avgZ / radius)).coerceIn(0.4f, 1f) else 0.45f
        val effectiveStroke = (strokeWidth * (if (isFront) factor1 else factor1 * 0.8f)).coerceAtLeast(0.8f)

        drawLine(
            color = color.copy(alpha = (color.alpha * depthAlpha).coerceIn(0.05f, 1f)),
            start = s1,
            end = s2,
            strokeWidth = effectiveStroke,
            cap = StrokeCap.Round
        )

        // Circuit Nodes at arc endpoints
        if (drawNodes && isFront && i % (steps / dashCount.coerceAtLeast(1)) == 0) {
            drawCircle(
                color = color,
                radius = 3.5f * factor1,
                center = s1
            )
        }

        // Telemetry Graduation Ticks
        if (drawTicks && isFront && i % 4 == 0) {
            val tickLen = if (i % 8 == 0) 7f else 4f
            val ptOuter = rotate3D((radius + tickLen) * cos(a1), 0f, (radius + tickLen) * sin(a1), pitch, yaw, roll)
            val factorOuter = projectFactor(ptOuter[2] + centerZ)
            val sOuter = Offset(center.x + ptOuter[0] * factorOuter, center.y + ptOuter[1] * factorOuter)
            drawLine(
                color = color.copy(alpha = 0.65f),
                start = s1,
                end = sOuter,
                strokeWidth = if (i % 8 == 0) 2.0f else 1.2f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Draws 3D latitude & longitude wireframe for the central volumetric holographic sphere.
 */
private fun DrawScope.render3DSphereWireframe(
    center: Offset,
    radius: Float,
    pitch: Float,
    yaw: Float,
    roll: Float,
    color: Color
) {
    val latCount = 3
    for (lat in -latCount..latCount) {
        val latAngle = lat * (PI.toFloat() / (latCount * 2.5f))
        val rLat = radius * cos(latAngle)
        val yLat = radius * sin(latAngle)

        val steps = 32
        for (i in 0 until steps) {
            val a1 = i * (2f * PI.toFloat() / steps)
            val a2 = (i + 1) * (2f * PI.toFloat() / steps)

            val p1 = rotate3D(rLat * cos(a1), yLat, rLat * sin(a1), pitch, yaw, roll)
            val p2 = rotate3D(rLat * cos(a2), yLat, rLat * sin(a2), pitch, yaw, roll)

            // Draw only front-facing hemisphere for clean look
            if (p1[2] < 0 && p2[2] < 0) continue

            val factor1 = projectFactor(p1[2])
            val factor2 = projectFactor(p2[2])

            val s1 = Offset(center.x + p1[0] * factor1, center.y + p1[1] * factor1)
            val s2 = Offset(center.x + p2[0] * factor2, center.y + p2[1] * factor2)

            drawLine(
                color = color.copy(alpha = (0.25f + 0.45f * (p1[2] / radius)).coerceIn(0.1f, 0.7f)),
                start = s1,
                end = s2,
                strokeWidth = 1.2f
            )
        }
    }
}

/**
 * Renders 3D particles distributed in a spherical shell, sorting front vs back.
 */
private fun DrawScope.render3DParticles(
    center: Offset,
    baseRadius: Float,
    seeds: List<ParticleSeed>,
    phase: Float,
    isListening: Boolean,
    isActivation: Boolean,
    pitch: Float,
    yaw: Float,
    roll: Float,
    centerZ: Float,
    isFront: Boolean,
    color: Color
) {
    for ((index, seed) in seeds.withIndex()) {
        // Listening: Inward vortex spiral
        val effectiveRadius = if (isActivation) {
            baseRadius * (1f - ((phase + index * 0.05f) % 1f)) * 0.8f + 12f
        } else if (isListening) {
            baseRadius * (1f - ((phase + index * 0.035f) % 1f)) * 0.9f + 18f
        } else {
            baseRadius * seed.radRatio * (0.85f + 0.15f * sin((phase * 2f * PI + index).toDouble()).toFloat())
        }

        val pTheta = seed.theta + (phase * 2f * PI.toFloat() * (if (isListening) 2f else 0.5f))
        val px = effectiveRadius * cos(seed.phi) * cos(pTheta)
        val py = effectiveRadius * sin(seed.phi)
        val pz = effectiveRadius * cos(seed.phi) * sin(pTheta)

        val rot = rotate3D(px, py, pz, pitch, yaw, roll)
        val z = rot[2] + centerZ

        val particleIsFront = z >= 0f
        if (particleIsFront != isFront) continue

        val factor = projectFactor(z)
        val screenPos = Offset(center.x + rot[0] * factor, center.y + rot[1] * factor)
        val alpha = if (isFront) (0.6f + 0.4f * (z / baseRadius)).coerceIn(0.3f, 1f) else 0.25f
        val pSize = (seed.size * factor).coerceIn(1.5f, 6.5f)

        drawCircle(
            color = color.copy(alpha = alpha),
            radius = pSize,
            center = screenPos
        )
    }
}

/**
 * Renders a small high-speed satellite energy node tracing a 3D elliptical orbit.
 */
private fun DrawScope.render3DSatellite(
    center: Offset,
    orbitRadius: Float,
    angleDeg: Float,
    orbitPitch: Float,
    orbitRoll: Float,
    centerZ: Float,
    isFront: Boolean,
    color: Color
) {
    val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
    val x = orbitRadius * cos(angleRad)
    val z = orbitRadius * sin(angleRad)

    val rot = rotate3D(x, 0f, z, orbitPitch, 0f, orbitRoll)
    val finalZ = rot[2] + centerZ

    val satIsFront = finalZ >= 0f
    if (satIsFront != isFront) return

    val factor = projectFactor(finalZ)
    val pos = Offset(center.x + rot[0] * factor, center.y + rot[1] * factor)
    val satRadius = (4.5f * factor).coerceIn(2.5f, 8.5f)

    // Outer glow ring
    drawCircle(
        color = color.copy(alpha = if (isFront) 0.35f else 0.15f),
        radius = satRadius * 2.2f,
        center = pos
    )

    // Solid core
    drawCircle(
        color = if (isFront) Color.White else color.copy(alpha = 0.5f),
        radius = satRadius,
        center = pos
    )
}

/**
 * Renders an audio-reactive waveform ribbon wrapping around the 3D core.
 */
private fun DrawScope.render3DAudioRibbon(
    center: Offset,
    radius: Float,
    waveAmp: Float,
    pitch: Float,
    yaw: Float,
    roll: Float,
    centerZ: Float,
    color: Color
) {
    val count = 28
    val step = (2f * PI.toFloat()) / count

    for (i in 0 until count) {
        val a1 = i * step
        val a2 = (i + 1) * step

        val ampMod1 = (sin(i * 1.8) * 0.6 + cos(i * 3.4) * 0.4).toFloat() * waveAmp
        val ampMod2 = (sin((i + 1) * 1.8) * 0.6 + cos((i + 1) * 3.4) * 0.4).toFloat() * waveAmp

        val r1 = radius + ampMod1
        val r2 = radius + ampMod2

        val p1 = rotate3D(r1 * cos(a1), ampMod1 * 0.5f, r1 * sin(a1), pitch, yaw, roll)
        val p2 = rotate3D(r2 * cos(a2), ampMod2 * 0.5f, r2 * sin(a2), pitch, yaw, roll)

        val z1 = p1[2] + centerZ
        val z2 = p2[2] + centerZ

        val factor1 = projectFactor(z1)
        val factor2 = projectFactor(z2)

        val s1 = Offset(center.x + p1[0] * factor1, center.y + p1[1] * factor1)
        val s2 = Offset(center.x + p2[0] * factor2, center.y + p2[1] * factor2)

        val avgZ = (z1 + z2) / 2f
        val isFront = avgZ >= 0f
        val alpha = if (isFront) (0.6f + 0.4f * (avgZ / radius)).coerceIn(0.4f, 1f) else 0.22f

        drawLine(
            color = color.copy(alpha = alpha),
            start = s1,
            end = s2,
            strokeWidth = if (isFront) 3.5f else 1.5f,
            cap = StrokeCap.Round
        )
    }
}

private data class ParticleSeed(
    val theta: Float,
    val phi: Float,
    val radRatio: Float,
    val size: Float
)

@Composable
fun MasterPowerSwitch(
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isOn) UltronNeonCyan else UltronCardBorder
    val glowShadow = if (isOn) UltronCyanGlow else Color.Transparent

    Surface(
        onClick = { onToggle(!isOn) },
        shape = RoundedCornerShape(24.dp),
        color = if (isOn) UltronSurfaceVariant else Color(0xFF0F141E),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        modifier = modifier
            .padding(horizontal = 24.dp)
            .shadow(if (isOn) 14.dp else 0.dp, shape = RoundedCornerShape(24.dp), ambientColor = glowShadow, spotColor = glowShadow)
            .testTag("master_power_switch")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 14.dp)
                .width(280.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isOn) UltronNeonCyan.copy(alpha = 0.2f) else Color(0xFF1E2634))
                        .border(1.dp, if (isOn) UltronNeonCyan else Color(0xFF323F52), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = if (isOn) "Turn Off ULTRON" else "Turn On ULTRON",
                        tint = if (isOn) UltronNeonCyan else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "ULTRON CORE POWER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = UltronTitanium.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isOn) "ONLINE // ACTIVE" else "OFFLINE // DORMANT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isOn) UltronNeonCyan else Color.Gray,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Futuristic Toggle Pill
            Box(
                contentAlignment = if (isOn) Alignment.CenterEnd else Alignment.CenterStart,
                modifier = Modifier
                    .width(52.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isOn) UltronNeonCyan.copy(alpha = 0.25f) else Color(0xFF1A2230))
                    .border(1.dp, if (isOn) UltronNeonCyan else Color(0xFF2C3B50), RoundedCornerShape(14.dp))
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isOn) UltronNeonCyan else Color.Gray)
                )
            }
        }
    }
}
