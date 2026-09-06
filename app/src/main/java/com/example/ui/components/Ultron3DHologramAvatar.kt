package com.example.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.UltronLipSyncEngine
import com.example.model.CyberFacet
import com.example.model.CyberWire
import com.example.model.CyberneticSkullFactory
import com.example.model.CyberneticSkullMesh
import com.example.model.SkullVertex
import com.example.model.Vector3D
import com.example.state.AssistantState
import com.example.ui.theme.UltronCrimson
import com.example.ui.theme.UltronCrimsonGlow
import com.example.ui.theme.UltronCyanGlow
import com.example.ui.theme.UltronNeonCyan
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * FULL 3D INTERACTIVE HOLOGRAPHIC ULTRON AVATAR
 *
 * Replaces any flat/circular core with a living 3D Holographic AI Skull:
 * - Highly detailed cybernetic skull wireframe & semi-transparent holographic facets
 * - Articulated mandible (lower jaw) that animates dynamically with real-time lip-sync during speech
 * - Glowing ocular centers (eyes) and floating internal neural core reactor
 * - True 3D perspective projection (focal distance: 820)
 * - Touch interactions:
 *     - One-finger drag: Free 360-degree rotation (yaw & pitch)
 *     - Drag release: Smooth inertia physics
 *     - Two-finger pinch: Smooth zoom in/out (0.6x to 2.4x)
 *     - Double tap: Animates back to front-facing default orientation
 * - Multi-layer gyroscope / accelerometer parallax tilt
 * - Orbiting holographic telemetry rings, particles, and volumetric scanlines
 * - Dynamic state transitions: IDLE, LISTENING, THINKING, SPEAKING, EXECUTING, ERROR
 */
@Composable
fun Ultron3DHologramAvatar(
    assistantState: AssistantState,
    isMasterOn: Boolean,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Real-time lip-sync aperture & acoustic energy from voice engine
    val mouthAperture by UltronLipSyncEngine.mouthOpenness.collectAsStateWithLifecycle()
    val acousticEnergy by UltronLipSyncEngine.acousticEnergy.collectAsStateWithLifecycle()

    // 3D Skull Mesh (instantiated once)
    val skullMesh: CyberneticSkullMesh = remember { CyberneticSkullFactory.createCyberneticSkull() }

    // Orientation & Scale state
    var rotPitch by remember { mutableFloatStateOf(8f) }   // Up/Down tilt
    var rotYaw by remember { mutableFloatStateOf(0f) }     // Left/Right 360 rotation
    var userZoom by remember { mutableFloatStateOf(1.0f) }

    // Touch & Inertia tracking
    var isUserInteracting by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(0L) }
    var velPitch by remember { mutableFloatStateOf(0f) }
    var velYaw by remember { mutableFloatStateOf(0f) }

    // Idle animation timer
    var idleTimeSec by remember { mutableFloatStateOf(0f) }

    // Gyroscope / Accelerometer multi-layer parallax
    var gyroPitch by remember { mutableFloatStateOf(0f) }
    var gyroRoll by remember { mutableFloatStateOf(0f) }
    var hasHardwareSensor by remember { mutableStateOf(false) }

    // Register hardware sensors for tilt parallax
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        hasHardwareSensor = rotSensor != null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rMatrix, orientation)
                    gyroPitch = gyroPitch * 0.82f + (orientation[1] / 1.2f).coerceIn(-1f, 1f) * 0.18f
                    gyroRoll = gyroRoll * 0.82f + (-orientation[2] / 1.2f).coerceIn(-1f, 1f) * 0.18f
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val rawX = (event.values[0] / 9.81f).coerceIn(-1f, 1f)
                    val rawY = (event.values[1] / 9.81f).coerceIn(-1f, 1f)
                    gyroPitch = gyroPitch * 0.82f + rawY * 0.18f
                    gyroRoll = gyroRoll * 0.82f + rawX * 0.18f
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotSensor != null && sensorManager != null) {
            sensorManager.registerListener(listener, rotSensor, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // Continuous Frame Loop for Inertia, Auto-Idle, and Breathing Motion
    LaunchedEffect(isMasterOn) {
        var lastFrameNano = System.nanoTime()
        while (isActive) {
            val nowNano = System.nanoTime()
            val dt = ((nowNano - lastFrameNano) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
            lastFrameNano = nowNano

            val timeSinceTouch = System.currentTimeMillis() - lastInteractionTime

            if (!isUserInteracting) {
                // Apply inertia momentum
                if (abs(velPitch) > 0.05f || abs(velYaw) > 0.05f) {
                    rotPitch += velPitch * dt
                    rotYaw += velYaw * dt
                    velPitch *= 0.92f
                    velYaw *= 0.92f
                }

                // If user hasn't touched for > 2.2 seconds, resume natural floating & slow rotational orbit
                if (timeSinceTouch > 2200L) {
                    val idleYawSpeed = when (assistantState) {
                        AssistantState.WAKE_DETECTED -> 75f
                        AssistantState.LISTENING -> 22f
                        AssistantState.THINKING -> 55f
                        AssistantState.SPEAKING -> 18f
                        AssistantState.EXECUTING -> 45f
                        else -> 12f // Calm slow rotation
                    }
                    rotYaw += idleYawSpeed * dt

                    // Pull pitch gently back toward natural eye-level ~8 degrees
                    rotPitch += (8f - rotPitch) * (1.2f * dt)
                    // Pull zoom back to standard 1.0f
                    userZoom += (1.0f - userZoom) * (1.4f * dt)
                }
            }

            idleTimeSec += dt
            delay(16L) // ~60 FPS
        }
    }

    // Floating translation (idle motion in 3D)
    val floatX = sin(idleTimeSec * 1.4f) * 7f
    val floatY = cos(idleTimeSec * 1.8f) * 9f
    val floatZ = sin(idleTimeSec * 1.6f) * 14f

    // Activation forward surge when wake word is detected
    val surgeZ = if (assistantState == AssistantState.WAKE_DETECTED) 85f else if (assistantState == AssistantState.SPEAKING) 30f else 0f

    // Dynamic State Colors
    val isCrimson = assistantState == AssistantState.WAKE_DETECTED ||
            assistantState == AssistantState.PROCESSING_BACK ||
            assistantState == AssistantState.SPEAKING

    val primaryColor = when {
        !isMasterOn -> Color(0xFF424F60)
        assistantState == AssistantState.ERROR -> Color(0xFFFF3344)
        isCrimson -> UltronCrimson
        assistantState == AssistantState.THINKING -> Color(0xFFFFB300)
        assistantState == AssistantState.EXECUTING -> Color(0xFF00FF99)
        else -> UltronNeonCyan
    }

    val glowColor = when {
        !isMasterOn -> Color.Transparent
        assistantState == AssistantState.ERROR -> Color(0x66FF3344)
        isCrimson -> UltronCrimsonGlow
        assistantState == AssistantState.THINKING -> Color(0x55FFB300)
        assistantState == AssistantState.EXECUTING -> Color(0x5500FF99)
        else -> UltronCyanGlow
    }

    // Ambient floating particles
    val particleSeeds = remember {
        val list = mutableListOf<AvatarParticle>()
        val count = 38
        for (i in 0 until count) {
            val theta = (i * (2f * PI.toFloat() / count))
            val phi = (((i * 7) % count) - count / 2f) / (count / 2f) * (PI.toFloat() * 0.45f)
            val dist = 110f + ((i * 13) % 10) * 12f
            list.add(AvatarParticle(theta, phi, dist, size = if (i % 3 == 0) 3.6f else 2.2f))
        }
        list
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .testTag("ultron_3d_hologram_avatar")
            // Double tap returns to default front orientation
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        coroutineScope.launch {
                            isUserInteracting = false
                            val startP = rotPitch
                            val startY = rotYaw
                            val startZ = userZoom
                            val anim = Animatable(0f)
                            anim.animateTo(1f, tween(320)) {
                                rotPitch = startP + (8f - startP) * value
                                rotYaw = startY + (0f - startY) * value
                                userZoom = startZ + (1.0f - startZ) * value
                            }
                            velPitch = 0f
                            velYaw = 0f
                            lastInteractionTime = 0L
                        }
                    }
                )
            }
            // 2-finger zoom & pinch
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    isUserInteracting = true
                    lastInteractionTime = System.currentTimeMillis()
                    userZoom = (userZoom * zoom).coerceIn(0.60f, 2.4f)
                    velPitch = 0f
                    velYaw = 0f
                }
            }
            // 1-finger drag with momentum
            .pointerInput(Unit) {
                var lastDragTime = System.currentTimeMillis()
                detectDragGestures(
                    onDragStart = {
                        isUserInteracting = true
                        lastInteractionTime = System.currentTimeMillis()
                        lastDragTime = System.currentTimeMillis()
                        velPitch = 0f
                        velYaw = 0f
                    },
                    onDragEnd = {
                        isUserInteracting = false
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    onDragCancel = {
                        isUserInteracting = false
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val now = System.currentTimeMillis()
                        val dtMs = (now - lastDragTime).coerceAtLeast(1L)
                        lastDragTime = now
                        lastInteractionTime = now
                        isUserInteracting = true

                        val dYaw = dragAmount.x * 0.52f
                        val dPitch = -dragAmount.y * 0.52f

                        rotYaw += dYaw
                        rotPitch += dPitch

                        velYaw = (dYaw / (dtMs / 1000f)).coerceIn(-720f, 720f)
                        velPitch = (dPitch / (dtMs / 1000f)).coerceIn(-720f, 720f)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Tilt parallax displacement
            val parallaxX = if (hasHardwareSensor) gyroRoll * 32f else floatX * 0.7f
            val parallaxY = if (hasHardwareSensor) -gyroPitch * 28f else floatY * 0.7f

            val avatarCenter = Offset(
                center.x + floatX * 0.6f + parallaxX * 0.8f,
                center.y + floatY * 0.6f + parallaxY * 0.8f
            )

            val headZ = floatZ + surgeZ

            // Rotation angles
            val pitchRad = Math.toRadians((rotPitch + (if (hasHardwareSensor) -gyroPitch * 14f else 0f)).toDouble()).toFloat()
            val yawRad = Math.toRadians((rotYaw + (if (hasHardwareSensor) gyroRoll * 14f else 0f)).toDouble()).toFloat()
            val rollRad = Math.toRadians((sin(idleTimeSec * 0.9) * 2.5).toDouble()).toFloat()

            // -------------------------------------------------------------------------
            // LAYER 0: Background Deep Holographic Glow & Cybernetic Grid
            // -------------------------------------------------------------------------
            if (isMasterOn) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor.copy(alpha = if (isCrimson) 0.32f else 0.22f), Color.Transparent),
                        center = avatarCenter,
                        radius = size.minDimension * 0.68f
                    ),
                    radius = size.minDimension * 0.68f,
                    center = avatarCenter
                )

                // Background depth circles
                drawCircle(
                    color = primaryColor.copy(alpha = 0.08f),
                    radius = size.minDimension * 0.44f * userZoom,
                    center = avatarCenter,
                    style = Stroke(width = 1f)
                )
            }

            // -------------------------------------------------------------------------
            // LAYER 1: 3D TRANSFORM OF SKULL VERTICES (Including Mandible Lip-Sync)
            // -------------------------------------------------------------------------
            val projectedVertices = arrayOfNulls<ScreenVertex>(skullMesh.vertices.size)

            // Lip-sync jaw displacement: when mouthAperture > 0, mandible drops downward and forward
            val jawDropY = mouthAperture * 22f
            val jawDropZ = mouthAperture * 8f
            val jawPitchAngle = mouthAperture * 0.16f // slight rotational opening of jaw

            val scaleMultiplier = (size.minDimension / 320f) * userZoom * 0.95f

            for (i in skullMesh.vertices.indices) {
                val sv = skullMesh.vertices[i]
                var vx = sv.basePos.x
                var vy = sv.basePos.y
                var vz = sv.basePos.z

                // Articulate jaw for lip-sync
                if (sv.isJaw) {
                    vy += jawDropY
                    vz += jawDropZ
                    // Slight rotational hinge about jaw angle
                    val cosJ = cos(jawPitchAngle)
                    val sinJ = sin(jawPitchAngle)
                    val localY = vy - 10f
                    vy = 10f + (localY * cosJ - vz * sinJ)
                    vz = localY * sinJ + vz * cosJ
                }

                // Apply avatar scaling
                vx *= scaleMultiplier
                vy *= scaleMultiplier
                vz *= scaleMultiplier

                // 3D Euler Rotation
                val rot = rotate3DEuler(vx, vy, vz, pitchRad, yawRad, rollRad)
                val finalZ = rot[2] + headZ

                // Perspective Projection
                val cameraDist = 820f
                val distance = (cameraDist - finalZ).coerceAtLeast(100f)
                val projScale = cameraDist / distance

                val sx = avatarCenter.x + rot[0] * projScale
                val sy = avatarCenter.y + rot[1] * projScale

                projectedVertices[i] = ScreenVertex(
                    sx = sx,
                    sy = sy,
                    z = finalZ,
                    projScale = projScale,
                    vertex = sv
                )
            }

            // -------------------------------------------------------------------------
            // LAYER 2: BACK PARTICLES & BACK WIRE SEGMENTS (Z < 0)
            // -------------------------------------------------------------------------
            if (isMasterOn) {
                for (p in particleSeeds) {
                    renderAvatarParticle(
                        avatarCenter = avatarCenter,
                        particle = p,
                        scale = scaleMultiplier,
                        pitch = pitchRad,
                        yaw = yawRad,
                        roll = rollRad,
                        headZ = headZ,
                        isFront = false,
                        color = primaryColor,
                        phase = (idleTimeSec * 0.3f) % 1f
                    )
                }
            }

            // Draw Back Wires (Depth sorted or filtered by z < 0)
            for (wire in skullMesh.wires) {
                val v1 = projectedVertices[wire.v1Index] ?: continue
                val v2 = projectedVertices[wire.v2Index] ?: continue
                val avgZ = (v1.z + v2.z) / 2f

                if (avgZ < 0f) {
                    val alpha = if (isMasterOn) (wire.baseAlpha * 0.22f).coerceIn(0.04f, 0.5f) else 0.08f
                    val strokeW = (1.2f * v1.projScale).coerceAtLeast(0.6f)
                    drawLine(
                        color = primaryColor.copy(alpha = alpha),
                        start = Offset(v1.sx, v1.sy),
                        end = Offset(v2.sx, v2.sy),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }

            // -------------------------------------------------------------------------
            // LAYER 3: SEMI-TRANSPARENT HOLOGRAPHIC FACETS (Face Depth & Transparency)
            // -------------------------------------------------------------------------
            if (isMasterOn) {
                for (facet in skullMesh.facets) {
                    val v1 = projectedVertices[facet.v1Index] ?: continue
                    val v2 = projectedVertices[facet.v2Index] ?: continue
                    val v3 = projectedVertices[facet.v3Index] ?: continue

                    // Face normal Z-test
                    val crossZ = (v2.sx - v1.sx) * (v3.sy - v1.sy) - (v2.sy - v1.sy) * (v3.sx - v1.sx)
                    if (crossZ > 0f) {
                        val path = Path().apply {
                            moveTo(v1.sx, v1.sy)
                            lineTo(v2.sx, v2.sy)
                            lineTo(v3.sx, v3.sy)
                            close()
                        }
                        val facetAlpha = (facet.fillAlpha * (if (facet.isJawFacet) 1.2f else 1.0f)).coerceIn(0.05f, 0.35f)
                        drawPath(path, color = primaryColor.copy(alpha = facetAlpha), style = Fill)
                    }
                }
            }

            // -------------------------------------------------------------------------
            // LAYER 4: FRONT WIRES & GLOWING CIRCUITS (Z >= 0)
            // -------------------------------------------------------------------------
            for (wire in skullMesh.wires) {
                val v1 = projectedVertices[wire.v1Index] ?: continue
                val v2 = projectedVertices[wire.v2Index] ?: continue
                val avgZ = (v1.z + v2.z) / 2f

                if (avgZ >= 0f) {
                    val isCircuit = wire.isCircuitPath
                    val isJaw = wire.isJawWire

                    // If speech is happening, jaw circuits illuminate extra bright
                    val speechBoost = if (isJaw && mouthAperture > 0.05f) 0.35f else 0f
                    val alpha = if (isMasterOn) {
                        (wire.baseAlpha * 0.75f + speechBoost).coerceIn(0.15f, 1.0f)
                    } else {
                        0.18f
                    }

                    val strokeW = ((if (isCircuit) 2.6f else 1.8f) * v1.projScale).coerceAtLeast(0.8f)

                    drawLine(
                        color = primaryColor.copy(alpha = alpha),
                        start = Offset(v1.sx, v1.sy),
                        end = Offset(v2.sx, v2.sy),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )

                    // Circuit node energy spark
                    if (isCircuit && isMasterOn && ((wire.v1Index + (idleTimeSec * 8f).toInt()) % 5 == 0)) {
                        drawCircle(
                            color = if (isCrimson) Color.White else Color(0xFFCCFFFF),
                            radius = 3.2f * v1.projScale,
                            center = Offset(v1.sx, v1.sy)
                        )
                    }
                }
            }

            // -------------------------------------------------------------------------
            // LAYER 5: GLOWING EYE OPTICS & NEURAL CORE REACTOR
            // -------------------------------------------------------------------------
            if (isMasterOn) {
                val leftEye = projectedVertices[skullMesh.leftEyeIndex]
                val rightEye = projectedVertices[skullMesh.rightEyeIndex]
                val coreV = projectedVertices[skullMesh.coreIndex]

                // Glowing Eye Optics
                val eyeScale = if (assistantState == AssistantState.WAKE_DETECTED) 1.5f else if (assistantState == AssistantState.THINKING) 1.25f else 1.0f
                val eyeGlow = if (isCrimson) UltronCrimson else UltronNeonCyan

                if (leftEye != null && leftEye.z > -80f) {
                    val r = 7.5f * leftEye.projScale * eyeScale
                    drawCircle(eyeGlow.copy(alpha = 0.35f), radius = r * 2.2f, center = Offset(leftEye.sx, leftEye.sy))
                    drawCircle(eyeGlow, radius = r, center = Offset(leftEye.sx, leftEye.sy))
                    drawCircle(Color.White, radius = r * 0.45f, center = Offset(leftEye.sx, leftEye.sy))
                }

                if (rightEye != null && rightEye.z > -80f) {
                    val r = 7.5f * rightEye.projScale * eyeScale
                    drawCircle(eyeGlow.copy(alpha = 0.35f), radius = r * 2.2f, center = Offset(rightEye.sx, rightEye.sy))
                    drawCircle(eyeGlow, radius = r, center = Offset(rightEye.sx, rightEye.sy))
                    drawCircle(Color.White, radius = r * 0.45f, center = Offset(rightEye.sx, rightEye.sy))
                }

                // Internal Neural Core Reactor
                if (coreV != null) {
                    val cr = 16f * coreV.projScale * (1.0f + (rmsLevel / 20f).coerceIn(0f, 0.4f))
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, eyeGlow, Color.Transparent),
                            center = Offset(coreV.sx, coreV.sy),
                            radius = cr * 2.0f
                        ),
                        radius = cr * 2.0f,
                        center = Offset(coreV.sx, coreV.sy)
                    )
                }

                // -------------------------------------------------------------------------
                // LAYER 6: SPEECH ACOUSTIC ENERGY EMISSION (Mouth & Jaw Lip-Sync Sparks)
                // -------------------------------------------------------------------------
                if (mouthAperture > 0.08f) {
                    val chinIdx = skullMesh.vertices.indexOfFirst { it.isJaw && it.basePos.y > 50f }
                    if (chinIdx >= 0) {
                        val chinV = projectedVertices[chinIdx]
                        if (chinV != null) {
                            val mouthPos = Offset(chinV.sx, chinV.sy - 18f * chinV.projScale)
                            val energyRadius = (26f * chinV.projScale * acousticEnergy).coerceAtLeast(8f)

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(primaryColor.copy(alpha = 0.45f), Color.Transparent),
                                    center = mouthPos,
                                    radius = energyRadius
                                ),
                                radius = energyRadius,
                                center = mouthPos
                            )

                            // Acoustic energy sparks
                            val sparkCount = 6
                            for (s in 0 until sparkCount) {
                                val ang = (s * (2f * PI.toFloat() / sparkCount)) + (idleTimeSec * 14f)
                                val sparkR = energyRadius * 0.85f
                                val spx = mouthPos.x + cos(ang.toDouble()).toFloat() * sparkR
                                val spy = mouthPos.y + sin(ang.toDouble()).toFloat() * sparkR * 0.6f
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.4f * chinV.projScale,
                                    center = Offset(spx, spy)
                                )
                            }
                        }
                    }
                }

                // -------------------------------------------------------------------------
                // LAYER 7: THINKING SCANNING RINGS & TELEMETRY
                // -------------------------------------------------------------------------
                if (assistantState == AssistantState.THINKING) {
                    val ringAngle = (idleTimeSec * 140f) % 360f
                    val ringRad = size.minDimension * 0.38f * userZoom
                    drawHoloOrbitRing(
                        center = avatarCenter,
                        radius = ringRad,
                        angleDeg = ringAngle,
                        pitch = pitchRad + 0.3f,
                        color = Color(0xFFFFB300)
                    )
                }

                // -------------------------------------------------------------------------
                // LAYER 8: FRONT PARTICLES (Z >= 0)
                // -------------------------------------------------------------------------
                for (p in particleSeeds) {
                    renderAvatarParticle(
                        avatarCenter = avatarCenter,
                        particle = p,
                        scale = scaleMultiplier,
                        pitch = pitchRad,
                        yaw = yawRad,
                        roll = rollRad,
                        headZ = headZ,
                        isFront = true,
                        color = primaryColor,
                        phase = (idleTimeSec * 0.3f) % 1f
                    )
                }
            }

            // -------------------------------------------------------------------------
            // LAYER 9: HOLOGRAPHIC SCANLINE & HUD BRACKETS
            // -------------------------------------------------------------------------
            if (isMasterOn) {
                val scanlineY = size.height * ((idleTimeSec * 0.4f) % 1f)
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, primaryColor.copy(alpha = 0.4f), Color.Transparent)
                    ),
                    start = Offset(24f, scanlineY),
                    end = Offset(size.width - 24f, scanlineY),
                    strokeWidth = 1.4f
                )

                // 4 Corner HUD brackets
                val bSize = 16f
                val bPad = 14f
                val bAlpha = 0.35f
                drawLine(primaryColor.copy(alpha = bAlpha), Offset(bPad, bPad), Offset(bPad + bSize, bPad), 2f)
                drawLine(primaryColor.copy(alpha = bAlpha), Offset(bPad, bPad), Offset(bPad, bPad + bSize), 2f)
                drawLine(primaryColor.copy(alpha = bAlpha), Offset(size.width - bPad, bPad), Offset(size.width - bPad - bSize, bPad), 2f)
                drawLine(primaryColor.copy(alpha = bAlpha), Offset(size.width - bPad, bPad), Offset(size.width - bPad, bPad + bSize), 2f)
                drawLine(primaryColor.copy(alpha = bAlpha), Offset(bPad, size.height - bPad), Offset(bPad + bSize, size.height - bPad), 2f)
                drawLine(primaryColor.copy(alpha = bAlpha), Offset(bPad, size.height - bPad), Offset(bPad, size.height - bPad - bSize), 2f)
                drawLine(primaryColor.copy(alpha = bAlpha), Offset(size.width - bPad, size.height - bPad), Offset(size.width - bPad - bSize, size.height - bPad), 2f)
                drawLine(primaryColor.copy(alpha = bAlpha), Offset(size.width - bPad, size.height - bPad), Offset(size.width - bPad, size.height - bPad - bSize), 2f)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// INTERNAL 3D MATH & PARTICLE HELPERS
// -----------------------------------------------------------------------------

private data class ScreenVertex(
    val sx: Float,
    val sy: Float,
    val z: Float,
    val projScale: Float,
    val vertex: SkullVertex
)

private data class AvatarParticle(
    val theta: Float,
    val phi: Float,
    val distance: Float,
    val size: Float
)

private fun rotate3DEuler(
    x: Float, y: Float, z: Float,
    pitch: Float, yaw: Float, roll: Float
): FloatArray {
    // 1. Yaw (Y)
    val cosY = cos(yaw)
    val sinY = sin(yaw)
    val x1 = x * cosY + z * sinY
    val y1 = y
    val z1 = -x * sinY + z * cosY

    // 2. Pitch (X)
    val cosX = cos(pitch)
    val sinX = sin(pitch)
    val x2 = x1
    val y2 = y1 * cosX - z1 * sinX
    val z2 = y1 * sinX + z1 * cosX

    // 3. Roll (Z)
    val cosZ = cos(roll)
    val sinZ = sin(roll)
    val x3 = x2 * cosZ - y2 * sinZ
    val y3 = x2 * sinZ + y2 * cosZ
    val z3 = z2

    return floatArrayOf(x3, y3, z3)
}

private fun DrawScope.renderAvatarParticle(
    avatarCenter: Offset,
    particle: AvatarParticle,
    scale: Float,
    pitch: Float,
    yaw: Float,
    roll: Float,
    headZ: Float,
    isFront: Boolean,
    color: Color,
    phase: Float
) {
    val dynamicDist = particle.distance * scale * (0.92f + sin((particle.theta + phase * 2f * PI).toDouble()).toFloat() * 0.12f)
    val px = dynamicDist * cos(particle.phi.toDouble()).toFloat() * cos(particle.theta.toDouble()).toFloat()
    val py = dynamicDist * sin(particle.phi.toDouble()).toFloat()
    val pz = dynamicDist * cos(particle.phi.toDouble()).toFloat() * sin(particle.theta.toDouble()).toFloat()

    val rot = rotate3DEuler(px, py, pz, pitch, yaw, roll)
    val finalZ = rot[2] + headZ

    val isPartFront = finalZ >= 0f
    if (isPartFront != isFront) return

    val cameraDist = 820f
    val distance = (cameraDist - finalZ).coerceAtLeast(100f)
    val projScale = cameraDist / distance

    val sx = avatarCenter.x + rot[0] * projScale
    val sy = avatarCenter.y + rot[1] * projScale

    val alpha = if (isFront) (0.55f + 0.35f * (finalZ / 150f)).coerceIn(0.2f, 0.95f) else 0.18f
    val radius = (particle.size * projScale).coerceAtLeast(1.2f)

    drawCircle(
        color = color.copy(alpha = alpha),
        radius = radius,
        center = Offset(sx, sy)
    )
}

private fun DrawScope.drawHoloOrbitRing(
    center: Offset,
    radius: Float,
    angleDeg: Float,
    pitch: Float,
    color: Color
) {
    val steps = 36
    val radStep = (2f * PI.toFloat()) / steps
    val yawRad = Math.toRadians(angleDeg.toDouble()).toFloat()

    for (i in 0 until steps) {
        if (i % 3 == 0) continue // dashed aesthetic
        val a1 = i * radStep
        val a2 = (i + 1) * radStep

        val p1 = rotate3DEuler(radius * cos(a1), 0f, radius * sin(a1), pitch, yawRad, 0f)
        val p2 = rotate3DEuler(radius * cos(a2), 0f, radius * sin(a2), pitch, yawRad, 0f)

        val camDist = 820f
        val s1 = Offset(center.x + p1[0] * (camDist / (camDist - p1[2])), center.y + p1[1] * (camDist / (camDist - p1[2])))
        val s2 = Offset(center.x + p2[0] * (camDist / (camDist - p2[2])), center.y + p2[1] * (camDist / (camDist - p2[2])))

        drawLine(color = color.copy(alpha = 0.65f), start = s1, end = s2, strokeWidth = 1.8f)
    }
}
