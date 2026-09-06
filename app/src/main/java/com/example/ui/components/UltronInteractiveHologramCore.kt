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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.UltronLipSyncEngine
import com.example.model.CyberneticSkullFactory
import com.example.model.CyberneticSkullMesh
import com.example.model.SkullVertex
import com.example.state.AssistantState
import com.example.ui.theme.UltronCrimson
import com.example.ui.theme.UltronCrimsonGlow
import com.example.ui.theme.UltronCyanGlow
import com.example.ui.theme.UltronNeonCyan
import com.example.ui.theme.UltronTitanium
import com.example.ui.theme.UltronWarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * TRUE INTERACTIVE 3D HOLOGRAPHIC ULTRON CORE
 *
 * Full Touch & Gesture Capabilities:
 * - One-finger drag: Rotates the 3D core freely (pitch, yaw) with 360-degree viewing
 * - Swipe left/right / up/down: Horizontal & vertical rotation
 * - Pinch & Two-finger gesture: Zoom in/out (core scale 0.5f to 2.2f)
 * - Inertia & Momentum: Smooth physics deceleration after releasing touch
 * - Double tap: Returns instantly with spring/tween animation to default orientation and scale
 * - Auto-idle resumption: Smoothly resumes automatic floating & orbital rotation after 2.5s of no touch
 *
 * Visual & 3D Depth Engine:
 * - True 3D perspective projection with camera focal distance & depth Z-buffering
 * - Multi-layered transparent 3D gimbal and energetic telemetry rings with depth occlusion (front vs back)
 * - Independent rotating orbiting satellites with real front/back rendering
 * - 3D spherical particle cloud modulated by perspective scale and depth illumination
 * - 3D audio reactive waveform ribbon
 * - Multi-layer gyroscope/accelerometer parallax: foreground moves more, background moves less
 * - Holographic scanlines, corner telemetry brackets, and subtle bloom
 * - Distinct visual appearances across AI states:
 *     IDLE, LISTENING, THINKING, SPEAKING, EXECUTING, ERROR
 */
@Composable
fun UltronInteractiveHologramCore(
    assistantState: AssistantState,
    isMasterOn: Boolean,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Real-time lip-sync mouth aperture & acoustic energy from voice engine
    val mouthAperture by UltronLipSyncEngine.mouthOpenness.collectAsStateWithLifecycle()
    val acousticEnergy by UltronLipSyncEngine.acousticEnergy.collectAsStateWithLifecycle()

    // 3D Cybernetic Skull Mesh
    val skullMesh: CyberneticSkullMesh = remember { CyberneticSkullFactory.createCyberneticSkull() }

    // ----------------------------------------------------
    // 3D ORIENTATION & GESTURE INTERACTION STATE
    // ----------------------------------------------------
    var rotX by remember { mutableFloatStateOf(15f) } // Pitch
    var rotY by remember { mutableFloatStateOf(0f) }  // Yaw
    var userScale by remember { mutableFloatStateOf(1.0f) }

    // Touch & Inertia tracking
    var isUserInteracting by remember { mutableStateOf(false) }
    var lastInteractionTime by remember { mutableLongStateOf(0L) }
    var velX by remember { mutableFloatStateOf(0f) }
    var velY by remember { mutableFloatStateOf(0f) }

    // Automatic idle time clock
    var idleTimeSec by remember { mutableFloatStateOf(0f) }

    // Hardware Gyroscope / Accelerometer multi-layer parallax state
    var gyroTiltX by remember { mutableFloatStateOf(0f) }
    var gyroTiltY by remember { mutableFloatStateOf(0f) }
    var hasHardwareGyro by remember { mutableStateOf(false) }

    // Register hardware sensors
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        hasHardwareGyro = rotSensor != null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rMatrix, orientation)
                    // orientation[1] is pitch, orientation[2] is roll
                    gyroTiltX = gyroTiltX * 0.85f + (orientation[1] / 1.2f).coerceIn(-1f, 1f) * 0.15f
                    gyroTiltY = gyroTiltY * 0.85f + (-orientation[2] / 1.2f).coerceIn(-1f, 1f) * 0.15f
                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val rawX = (event.values[0] / 9.81f).coerceIn(-1f, 1f)
                    val rawY = (event.values[1] / 9.81f).coerceIn(-1f, 1f)
                    gyroTiltX = gyroTiltX * 0.85f + rawY * 0.15f
                    gyroTiltY = gyroTiltY * 0.85f + rawX * 0.15f
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

    // Continuous Frame Loop for Inertia, Auto-Idle, and Orbiting Layers
    LaunchedEffect(isMasterOn) {
        var lastFrameNano = System.nanoTime()
        while (isActive) {
            val nowNano = System.nanoTime()
            val dt = ((nowNano - lastFrameNano) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
            lastFrameNano = nowNano

            val timeSinceTouch = System.currentTimeMillis() - lastInteractionTime

            if (isUserInteracting) {
                // User is actively touching, velocity is recorded directly by touch handlers
            } else {
                // Handle inertia momentum
                if (abs(velX) > 0.05f || abs(velY) > 0.05f) {
                    rotX += velX * dt
                    rotY += velY * dt
                    velX *= 0.92f // friction
                    velY *= 0.92f
                }

                // If user hasn't touched for > 2.5 seconds, smoothly transition back to idle rotation & orientation
                if (timeSinceTouch > 2500L) {
                    val idleSpeed = when (assistantState) {
                        AssistantState.WAKE_DETECTED -> 120f
                        AssistantState.LISTENING -> 65f
                        AssistantState.THINKING -> 85f
                        AssistantState.SPEAKING -> 50f
                        AssistantState.EXECUTING -> 90f
                        else -> 24f // slow 360 rotation
                    }
                    rotY += idleSpeed * dt

                    // Smoothly pull rotX back toward comfortable default ~15 degrees
                    rotX += (15f - rotX) * (1.2f * dt)
                    // Smoothly pull userScale back toward 1.0f
                    userScale += (1.0f - userScale) * (1.5f * dt)
                }
            }

            idleTimeSec += dt
            delay(16L) // ~60 FPS
        }
    }

    // AI State Dynamic Colors & Accents
    val isCrimson = assistantState == AssistantState.WAKE_DETECTED ||
            assistantState == AssistantState.PROCESSING_BACK ||
            assistantState == AssistantState.SPEAKING

    val isThinking = assistantState == AssistantState.THINKING
    val isExecuting = assistantState == AssistantState.EXECUTING
    val isError = assistantState == AssistantState.ERROR

    val primaryColor = when {
        !isMasterOn -> Color(0xFF4A5568)
        isError -> Color(0xFFFF3344)
        isCrimson -> UltronCrimson
        isThinking -> Color(0xFFFFB300) // Amber gold scanning rings
        isExecuting -> Color(0xFF00FF99) // Emerald neon
        else -> UltronNeonCyan
    }

    val glowColor = when {
        !isMasterOn -> Color.Transparent
        isError -> Color(0x66FF3344)
        isCrimson -> UltronCrimsonGlow
        isThinking -> Color(0x55FFB300)
        isExecuting -> Color(0x5500FF99)
        else -> UltronCyanGlow
    }

    // Floating translation (idle motion)
    val floatY = sin(idleTimeSec * 1.8f) * 8f
    val floatX = cos(idleTimeSec * 1.3f) * 6f
    val floatZ = sin(idleTimeSec * 1.5f) * 12f

    // Scale pulse depending on AI state
    val statePulse = when (assistantState) {
        AssistantState.WAKE_DETECTED -> 1.18f + sin(idleTimeSec * 15f) * 0.05f
        AssistantState.SPEAKING -> 1.08f + sin(idleTimeSec * 8f) * 0.06f
        AssistantState.LISTENING -> 1.04f + (rmsLevel / 12f).coerceIn(0f, 0.12f)
        AssistantState.THINKING -> 1.05f + sin(idleTimeSec * 10f) * 0.04f
        AssistantState.EXECUTING -> 1.06f + sin(idleTimeSec * 12f) * 0.04f
        else -> 1.0f + sin(idleTimeSec * 2.0f) * 0.03f
    }

    // Static 3D particle positions
    val particles = remember {
        val list = mutableListOf<Particle3D>()
        val count = 36
        for (i in 0 until count) {
            val theta = (i * (2f * PI.toFloat() / count))
            val phi = (((i * 7) % count) - count / 2f) / (count / 2f) * (PI.toFloat() * 0.44f)
            val radRatio = 0.50f + ((i * 11) % 10) * 0.055f
            list.add(Particle3D(theta, phi, radRatio, size = if (i % 3 == 0) 3.8f else 2.2f))
        }
        list
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Holographic 3D Viewport Box with Touch Gestures
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(360.dp)
                .padding(2.dp)
                .testTag("ultron_3d_hologram_core")
                // Double tap gesture to return to default orientation
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            coroutineScope.launch {
                                isUserInteracting = false
                                val startX = rotX
                                val startY = rotY
                                val startS = userScale
                                val anim = Animatable(0f)
                                anim.animateTo(1f, tween(350)) {
                                    rotX = startX + (15f - startX) * value
                                    rotY = startY + (0f - startY) * value
                                    userScale = startS + (1.0f - startS) * value
                                }
                                velX = 0f
                                velY = 0f
                                lastInteractionTime = 0L
                            }
                        }
                    )
                }
                // Transform gestures: 2-finger zoom & pinch
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        isUserInteracting = true
                        lastInteractionTime = System.currentTimeMillis()
                        userScale = (userScale * zoom).coerceIn(0.55f, 2.2f)
                        velX = 0f
                        velY = 0f
                    }
                }
                // Drag gestures: 1-finger rotate freely in 3D (pitch & yaw) with momentum
                .pointerInput(Unit) {
                    var lastDragTime = System.currentTimeMillis()
                    detectDragGestures(
                        onDragStart = {
                            isUserInteracting = true
                            lastInteractionTime = System.currentTimeMillis()
                            lastDragTime = System.currentTimeMillis()
                            velX = 0f
                            velY = 0f
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

                            // Map horizontal drag to yaw (rotY), vertical drag to pitch (rotX)
                            val dYaw = dragAmount.x * 0.55f
                            val dPitch = -dragAmount.y * 0.55f

                            rotY += dYaw
                            rotX += dPitch

                            // Calculate instant velocity for inertia
                            velY = (dYaw / (dtMs / 1000f)).coerceIn(-720f, 720f)
                            velX = (dPitch / (dtMs / 1000f)).coerceIn(-720f, 720f)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = (size.minDimension / 2f - 24f) * userScale * statePulse

                // Parallax shift from phone tilt (foreground layers move more, background less)
                val parallaxX = if (hasHardwareGyro) gyroTiltY * 26f else floatX * 0.6f
                val parallaxY = if (hasHardwareGyro) -gyroTiltX * 22f else floatY * 0.6f

                val coreCenter = Offset(
                    center.x + floatX * 0.5f + parallaxX * 0.7f,
                    center.y + floatY * 0.5f + parallaxY * 0.7f
                )

                val radPitch = Math.toRadians((rotX + (if (hasHardwareGyro) -gyroTiltX * 16f else 0f)).toDouble()).toFloat()
                val radYaw = Math.toRadians((rotY + (if (hasHardwareGyro) gyroTiltY * 16f else 0f)).toDouble()).toFloat()
                val radRoll = Math.toRadians((sin(idleTimeSec * 0.8) * 3f).toDouble()).toFloat()

                val coreZ = floatZ + (if (assistantState == AssistantState.WAKE_DETECTED) 60f else 0f)

                // ----------------------------------------------------
                // LAYER 0: Background Holographic Grid & Deep Aura
                // ----------------------------------------------------
                if (isMasterOn) {
                    val bgCenter = Offset(center.x - parallaxX * 0.3f, center.y - parallaxY * 0.3f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = if (isCrimson) 0.38f else if (isThinking) 0.28f else 0.22f),
                                Color.Transparent
                            ),
                            center = bgCenter,
                            radius = baseRadius * 1.55f
                        ),
                        radius = baseRadius * 1.55f,
                        center = bgCenter
                    )

                    // Ambient depth rings in background
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.07f),
                        radius = baseRadius * 1.18f,
                        center = bgCenter,
                        style = Stroke(width = 1f)
                    )
                }

                // Ring Orientations in 3D Space
                val outerPitch = radPitch - 0.38f
                val outerYaw = -radYaw * 0.8f + (idleTimeSec * 0.3f)
                val outerRoll = radRoll + 0.18f

                val gimbalPitch = radPitch + 0.62f
                val gimbalYaw = radYaw * 1.1f + (idleTimeSec * 0.6f)
                val gimbalRoll = radRoll - 0.30f

                val eqPitch = radPitch * 0.4f
                val eqYaw = -radYaw * 1.3f + (idleTimeSec * 0.8f)
                val eqRoll = radRoll * 0.2f

                val innerPitch = radPitch + 0.28f
                val innerYaw = radYaw * 1.6f - (idleTimeSec * 1.2f)
                val innerRoll = radRoll

                // ----------------------------------------------------
                // LAYER 1: BACK SEGMENTS OF 3D RINGS (Z < 0)
                // ----------------------------------------------------
                // Outer Ring Back
                draw3DHoloRing(
                    center = coreCenter,
                    radius = baseRadius * 1.15f,
                    pitch = outerPitch,
                    yaw = outerYaw,
                    roll = outerRoll,
                    centerZ = coreZ,
                    isFront = false,
                    color = primaryColor.copy(alpha = if (isMasterOn) 0.20f else 0.08f),
                    strokeWidth = 1.6f,
                    dashCount = 4,
                    gapRatio = 0.3f
                )

                // Gimbal Ring Back
                draw3DHoloRing(
                    center = coreCenter,
                    radius = baseRadius * 0.95f,
                    pitch = gimbalPitch,
                    yaw = gimbalYaw,
                    roll = gimbalRoll,
                    centerZ = coreZ,
                    isFront = false,
                    color = primaryColor.copy(alpha = if (isMasterOn) 0.22f else 0.08f),
                    strokeWidth = 1.8f,
                    dashCount = 3,
                    gapRatio = 0.25f
                )

                // Equatorial Ring Back
                draw3DHoloRing(
                    center = coreCenter,
                    radius = baseRadius * 0.80f,
                    pitch = eqPitch,
                    yaw = eqYaw,
                    roll = eqRoll,
                    centerZ = coreZ,
                    isFront = false,
                    color = primaryColor.copy(alpha = if (isMasterOn) 0.24f else 0.09f),
                    strokeWidth = 1.5f,
                    dashCount = 1,
                    gapRatio = 0f
                )

                // Inner Shield Ring Back
                draw3DHoloRing(
                    center = coreCenter,
                    radius = baseRadius * 0.58f,
                    pitch = innerPitch,
                    yaw = innerYaw,
                    roll = innerRoll,
                    centerZ = coreZ,
                    isFront = false,
                    color = primaryColor.copy(alpha = if (isMasterOn) 0.26f else 0.10f),
                    strokeWidth = 1.4f,
                    dashCount = 6,
                    gapRatio = 0.2f
                )

                // ----------------------------------------------------
                // LAYER 2: BACK SATELLITES & PARTICLES (Z < 0)
                // ----------------------------------------------------
                if (isMasterOn) {
                    // Back Satellites
                    draw3DSatelliteNode(
                        center = coreCenter,
                        orbitRadius = baseRadius * 0.94f,
                        angleDeg = (idleTimeSec * 50f) % 360f,
                        orbitPitch = radPitch + 0.5f,
                        orbitRoll = radRoll - 0.4f,
                        centerZ = coreZ,
                        isFront = false,
                        color = primaryColor
                    )
                    draw3DSatelliteNode(
                        center = coreCenter,
                        orbitRadius = baseRadius * 0.78f,
                        angleDeg = -(idleTimeSec * 65f) % 360f,
                        orbitPitch = radPitch - 0.6f,
                        orbitRoll = radRoll + 0.5f,
                        centerZ = coreZ,
                        isFront = false,
                        color = primaryColor
                    )

                    // Back 3D Particle Cloud
                    for (p in particles) {
                        render3DParticle(
                            center = coreCenter,
                            particle = p,
                            baseRadius = baseRadius,
                            pitch = radPitch,
                            yaw = radYaw,
                            roll = radRoll,
                            centerZ = coreZ,
                            phase = (idleTimeSec * 0.35f) % 1f,
                            isFront = false,
                            color = primaryColor
                        )
                    }
                }

                // ----------------------------------------------------
                // LAYER 3: 3D CYBERNETIC SKULL AVATAR & ARTICULATED MANDIBLE
                // ----------------------------------------------------
                val coreProj = projectPerspective(0f, 0f, coreZ, 780f)
                val centralPt = Offset(coreCenter.x + coreProj.x, coreCenter.y + coreProj.y)
                val depthScale = coreProj.scale

                // 3D Skull Vertex Projection & Jaw Articulation for real-time lip-sync
                val skullScale = (baseRadius / 150f) * 1.25f
                val jawDropY = mouthAperture * 22f * skullScale
                val jawDropZ = mouthAperture * 7f * skullScale
                val jawAngle = mouthAperture * 0.15f

                val projectedSkullVerts = arrayOfNulls<ProjPoint>(skullMesh.vertices.size)
                for (vi in skullMesh.vertices.indices) {
                    val sv = skullMesh.vertices[vi]
                    var vx = sv.basePos.x * skullScale
                    var vy = sv.basePos.y * skullScale
                    var vz = sv.basePos.z * skullScale

                    if (sv.isJaw) {
                        vy += jawDropY
                        vz += jawDropZ
                        val cosJ = cos(jawAngle)
                        val sinJ = sin(jawAngle)
                        val localY = vy - (10f * skullScale)
                        vy = (10f * skullScale) + (localY * cosJ - vz * sinJ)
                        vz = localY * sinJ + vz * cosJ
                    }

                    val rot = rotateEuler(vx, vy, vz, radPitch, radYaw, radRoll)
                    val pz = rot.z + coreZ
                    val pp = projectPerspective(rot.x, rot.y, pz, 780f)
                    projectedSkullVerts[vi] = pp
                }

                // Draw Semi-transparent holographic facets
                if (isMasterOn) {
                    for (facet in skullMesh.facets) {
                        val p1 = projectedSkullVerts[facet.v1Index] ?: continue
                        val p2 = projectedSkullVerts[facet.v2Index] ?: continue
                        val p3 = projectedSkullVerts[facet.v3Index] ?: continue

                        val cross = (p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x)
                        if (cross > 0f) {
                            val fPath = Path().apply {
                                moveTo(coreCenter.x + p1.x, coreCenter.y + p1.y)
                                lineTo(coreCenter.x + p2.x, coreCenter.y + p2.y)
                                lineTo(coreCenter.x + p3.x, coreCenter.y + p3.y)
                                close()
                            }
                            drawPath(
                                path = fPath,
                                color = primaryColor.copy(alpha = (facet.fillAlpha * (if (facet.isJawFacet) 1.35f else 1.15f)).coerceIn(0.06f, 0.40f)),
                                style = Fill
                            )
                        }
                    }
                }

                // Draw Cybernetic Skull Wireframe
                for (wire in skullMesh.wires) {
                    val p1 = projectedSkullVerts[wire.v1Index] ?: continue
                    val p2 = projectedSkullVerts[wire.v2Index] ?: continue
                    val avgZ = (p1.z + p2.z) / 2f

                    val isFrontWire = avgZ >= 0f
                    val alpha = if (isMasterOn) {
                        if (isFrontWire) {
                            val jawBoost = if (wire.isJawWire && mouthAperture > 0.05f) 0.30f else 0f
                            (wire.baseAlpha * 0.85f + jawBoost).coerceIn(0.25f, 1.0f)
                        } else {
                            (wire.baseAlpha * 0.25f).coerceIn(0.08f, 0.45f)
                        }
                    } else {
                        if (isFrontWire) 0.25f else 0.08f
                    }

                    val sWidth = ((if (wire.isCircuitPath) 2.6f else 1.8f) * p1.scale).coerceAtLeast(1.0f)

                    drawLine(
                        color = primaryColor.copy(alpha = alpha),
                        start = Offset(coreCenter.x + p1.x, coreCenter.y + p1.y),
                        end = Offset(coreCenter.x + p2.x, coreCenter.y + p2.y),
                        strokeWidth = sWidth,
                        cap = StrokeCap.Round
                    )

                    // Circuit node pulse
                    if (isMasterOn && wire.isCircuitPath && isFrontWire && ((wire.v1Index + (idleTimeSec * 7f).toInt()) % 4 == 0)) {
                        drawCircle(
                            color = if (isCrimson) Color.White else Color(0xFFE0FFFF),
                            radius = 3.2f * p1.scale,
                            center = Offset(coreCenter.x + p1.x, coreCenter.y + p1.y)
                        )
                    }
                }

                // Glowing Optic Eyes & Neural Core
                if (isMasterOn) {
                    val eyeL = projectedSkullVerts[skullMesh.leftEyeIndex]
                    val eyeR = projectedSkullVerts[skullMesh.rightEyeIndex]
                    val eyeScale = if (assistantState == AssistantState.WAKE_DETECTED) 1.4f else 1.0f
                    val eyeColor = if (isCrimson) UltronCrimson else UltronNeonCyan

                    if (eyeL != null && eyeL.z > -60f) {
                        val er = 7.5f * eyeL.scale * eyeScale
                        val ep = Offset(coreCenter.x + eyeL.x, coreCenter.y + eyeL.y)
                        drawCircle(eyeColor.copy(alpha = 0.40f), radius = er * 2.5f, center = ep)
                        drawCircle(eyeColor, radius = er, center = ep)
                        drawCircle(Color.White, radius = er * 0.45f, center = ep)
                    }

                    if (eyeR != null && eyeR.z > -60f) {
                        val er = 7.5f * eyeR.scale * eyeScale
                        val ep = Offset(coreCenter.x + eyeR.x, coreCenter.y + eyeR.y)
                        drawCircle(eyeColor.copy(alpha = 0.40f), radius = er * 2.5f, center = ep)
                        drawCircle(eyeColor, radius = er, center = ep)
                        drawCircle(Color.White, radius = er * 0.45f, center = ep)
                    }

                    // Central Neural Core Reactor
                    val coreV = projectedSkullVerts[skullMesh.coreIndex]
                    if (coreV != null) {
                        val cr = 18f * coreV.scale * (1.0f + (rmsLevel / 18f).coerceIn(0f, 0.4f))
                        val cp = Offset(coreCenter.x + coreV.x, coreCenter.y + coreV.y)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, eyeColor, Color.Transparent),
                                center = cp,
                                radius = cr * 2.2f
                            ),
                            radius = cr * 2.2f,
                            center = cp
                        )
                    }

                    // Acoustic Lip-Sync Energy Sparks around mouth
                    if (mouthAperture > 0.08f) {
                        val chinP = projectedSkullVerts.getOrNull(skullMesh.vertices.indexOfFirst { it.isJaw && it.basePos.y > 50f })
                        if (chinP != null) {
                            val mp = Offset(coreCenter.x + chinP.x, coreCenter.y + chinP.y - 14f * chinP.scale)
                            val acousticRad = (24f * chinP.scale * acousticEnergy).coerceAtLeast(9f)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(primaryColor.copy(alpha = 0.50f), Color.Transparent),
                                    center = mp,
                                    radius = acousticRad
                                ),
                                radius = acousticRad,
                                center = mp
                            )

                            // Acoustic energy sparks
                            val sparkCount = 6
                            for (s in 0 until sparkCount) {
                                val ang = (s * (2f * PI.toFloat() / sparkCount)) + (idleTimeSec * 15f)
                                val sparkR = acousticRad * 0.82f
                                val spx = mp.x + cos(ang.toDouble()).toFloat() * sparkR
                                val spy = mp.y + sin(ang.toDouble()).toFloat() * sparkR * 0.65f
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.4f * chinP.scale,
                                    center = Offset(spx, spy)
                                )
                            }
                        }
                    }

                    // 3D Audio Reactive Waveform Ribbon (wraps around core)
                    if (assistantState == AssistantState.LISTENING || assistantState == AssistantState.SPEAKING) {
                        draw3DAudioRibbon(
                            center = centralPt,
                            radius = (baseRadius * 0.36f) * depthScale,
                            rms = rmsLevel,
                            timeSec = idleTimeSec,
                            color = primaryColor
                        )
                    }
                } else {
                    // Dormant Core
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.35f),
                        radius = (baseRadius * 0.28f) * depthScale,
                        center = centralPt,
                        style = Stroke(width = 1.8f)
                    )
                }

                // ----------------------------------------------------
                // LAYER 4: FRONT SATELLITES & PARTICLES (Z >= 0)
                // ----------------------------------------------------
                if (isMasterOn) {
                    for (p in particles) {
                        render3DParticle(
                            center = coreCenter,
                            particle = p,
                            baseRadius = baseRadius,
                            pitch = radPitch,
                            yaw = radYaw,
                            roll = radRoll,
                            centerZ = coreZ,
                            phase = (idleTimeSec * 0.35f) % 1f,
                            isFront = true,
                            color = primaryColor
                        )
                    }

                    draw3DSatelliteNode(
                        center = coreCenter,
                        orbitRadius = baseRadius * 0.94f,
                        angleDeg = (idleTimeSec * 50f) % 360f,
                        orbitPitch = radPitch + 0.5f,
                        orbitRoll = radRoll - 0.4f,
                        centerZ = coreZ,
                        isFront = true,
                        color = primaryColor
                    )
                    draw3DSatelliteNode(
                        center = coreCenter,
                        orbitRadius = baseRadius * 0.78f,
                        angleDeg = -(idleTimeSec * 65f) % 360f,
                        orbitPitch = radPitch - 0.6f,
                        orbitRoll = radRoll + 0.5f,
                        centerZ = coreZ,
                        isFront = true,
                        color = primaryColor
                    )
                }

                // ----------------------------------------------------
                // LAYER 5: FRONT SEGMENTS OF 3D RINGS (Z >= 0)
                // ----------------------------------------------------
                // Inner Shield Ring Front
                draw3DHoloRing(
                    center = coreCenter,
                    radius = baseRadius * 0.58f,
                    pitch = innerPitch,
                    yaw = innerYaw,
                    roll = innerRoll,
                    centerZ = coreZ,
                    isFront = true,
                    color = primaryColor.copy(alpha = if (isMasterOn) 0.85f else 0.30f),
                    strokeWidth = 2.4f,
                    dashCount = 6,
                    gapRatio = 0.2f
                )

                // Equatorial Telemetry Ring Front (with graduation ticks)
                draw3DHoloRing(
                    center = coreCenter,
                    radius = baseRadius * 0.80f,
                    pitch = eqPitch,
                    yaw = eqYaw,
                    roll = eqRoll,
                    centerZ = coreZ,
                    isFront = true,
                    color = primaryColor.copy(alpha = if (isMasterOn) 0.95f else 0.35f),
                    strokeWidth = 2.8f,
                    dashCount = 1,
                    gapRatio = 0f,
                    drawTicks = isMasterOn
                )

                // Middle Gimbal Ring Front
                draw3DHoloRing(
                    center = coreCenter,
                    radius = baseRadius * 0.95f,
                    pitch = gimbalPitch,
                    yaw = gimbalYaw,
                    roll = gimbalRoll,
                    centerZ = coreZ,
                    isFront = true,
                    color = primaryColor.copy(alpha = if (isMasterOn) 0.95f else 0.35f),
                    strokeWidth = 3.6f,
                    dashCount = 3,
                    gapRatio = 0.25f,
                    drawNodes = isMasterOn
                )

                // Outer Energy Ring Front
                draw3DHoloRing(
                    center = coreCenter,
                    radius = baseRadius * 1.15f,
                    pitch = outerPitch,
                    yaw = outerYaw,
                    roll = outerRoll,
                    centerZ = coreZ,
                    isFront = true,
                    color = primaryColor.copy(alpha = if (isMasterOn) 1.0f else 0.40f),
                    strokeWidth = 4.2f,
                    dashCount = 4,
                    gapRatio = 0.3f,
                    drawNodes = isMasterOn
                )

                // ----------------------------------------------------
                // LAYER 6: SCANLINES & HUD TELEMETRY BRACKETS
                // ----------------------------------------------------
                if (isMasterOn) {
                    val scanlineY = size.height * ((idleTimeSec * 0.35f) % 1f)
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

                    // 4 Corner HUD brackets
                    val bSize = 14f
                    val bPad = 12f
                    val bAlpha = 0.35f
                    // Top-Left
                    drawLine(primaryColor.copy(alpha = bAlpha), Offset(bPad, bPad), Offset(bPad + bSize, bPad), 2f)
                    drawLine(primaryColor.copy(alpha = bAlpha), Offset(bPad, bPad), Offset(bPad, bPad + bSize), 2f)
                    // Top-Right
                    drawLine(primaryColor.copy(alpha = bAlpha), Offset(size.width - bPad, bPad), Offset(size.width - bPad - bSize, bPad), 2f)
                    drawLine(primaryColor.copy(alpha = bAlpha), Offset(size.width - bPad, bPad), Offset(size.width - bPad, bPad + bSize), 2f)
                    // Bottom-Left
                    drawLine(primaryColor.copy(alpha = bAlpha), Offset(bPad, size.height - bPad), Offset(bPad + bSize, size.height - bPad), 2f)
                    drawLine(primaryColor.copy(alpha = bAlpha), Offset(bPad, size.height - bPad), Offset(bPad, size.height - bPad - bSize), 2f)
                    // Bottom-Right
                    drawLine(primaryColor.copy(alpha = bAlpha), Offset(size.width - bPad, size.height - bPad), Offset(size.width - bPad - bSize, size.height - bPad), 2f)
                    drawLine(primaryColor.copy(alpha = bAlpha), Offset(size.width - bPad, size.height - bPad), Offset(size.width - bPad, size.height - bPad - bSize), 2f)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Holographic Status Readout below 3D Skull Avatar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            val readoutText = when {
                !isMasterOn -> "OFFLINE"
                assistantState == AssistantState.WAKE_DETECTED -> "ULTRON ACTIVATED"
                assistantState == AssistantState.PROCESSING_BACK -> "BACK TRIGGERED"
                assistantState == AssistantState.THINKING -> "NEURAL PROCESSING..."
                assistantState == AssistantState.EXECUTING -> "EXECUTING DIRECTIVE"
                assistantState == AssistantState.SPEAKING -> "YES, TONY."
                assistantState == AssistantState.LISTENING -> "LISTENING..."
                assistantState == AssistantState.ERROR -> "SYSTEM ERROR"
                else -> "SYSTEM READY"
            }

            Text(
                text = readoutText,
                color = if (isMasterOn) primaryColor else Color.Gray,
                fontSize = if (readoutText.length > 12) 11.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ----------------------------------------------------
// 3D PERSPECTIVE MATH & DRAWING UTILITIES
// ----------------------------------------------------

private data class Point3D(val x: Float, val y: Float, val z: Float)
private data class ProjPoint(val x: Float, val y: Float, val scale: Float, val z: Float)
private data class Particle3D(val theta: Float, val phi: Float, val radRatio: Float, val size: Float)

private fun projectPerspective(x: Float, y: Float, z: Float, cameraDist: Float): ProjPoint {
    val distance = (cameraDist - z).coerceAtLeast(100f)
    val scale = cameraDist / distance
    return ProjPoint(x * scale, y * scale, scale, z)
}

private fun rotateEuler(x: Float, y: Float, z: Float, pitch: Float, yaw: Float, roll: Float): Point3D {
    // Pitch (around X)
    val cp = cos(pitch); val sp = sin(pitch)
    val y1 = y * cp - z * sp
    val z1 = y * sp + z * cp

    // Yaw (around Y)
    val cy = cos(yaw); val sy = sin(yaw)
    val x2 = x * cy + z1 * sy
    val z2 = -x * sy + z1 * cy

    // Roll (around Z)
    val cr = cos(roll); val sr = sin(roll)
    val x3 = x2 * cr - y1 * sr
    val y3 = x2 * sr + y1 * cr

    return Point3D(x3, y3, z2)
}

private fun DrawScope.draw3DHoloRing(
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
    drawNodes: Boolean = false,
    drawTicks: Boolean = false
) {
    val steps = 96
    val dAngle = (2f * PI.toFloat()) / steps

    var pathStarted = false
    val path = Path()

    for (i in 0..steps) {
        val angle = i * dAngle

        if (dashCount > 1 && gapRatio > 0f) {
            val segAngle = (2f * PI.toFloat()) / dashCount
            val localAngle = (angle % segAngle + segAngle) % segAngle
            if (localAngle > segAngle * (1f - gapRatio)) {
                if (pathStarted) {
                    drawPath(path, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                    path.reset()
                    pathStarted = false
                }
                continue
            }
        }

        val rx = cos(angle) * radius
        val ry = sin(angle) * radius
        val rz = 0f

        val rotated = rotateEuler(rx, ry, rz, pitch, yaw, roll)
        val finalZ = rotated.z + centerZ

        val belongsHere = if (isFront) finalZ >= 0f else finalZ < 0f
        if (!belongsHere) {
            if (pathStarted) {
                drawPath(path, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                path.reset()
                pathStarted = false
            }
            continue
        }

        val proj = projectPerspective(rotated.x, rotated.y, finalZ, 780f)
        val screenX = center.x + proj.x
        val screenY = center.y + proj.y

        if (!pathStarted) {
            path.moveTo(screenX, screenY)
            pathStarted = true
        } else {
            path.lineTo(screenX, screenY)
        }

        // Optional nodes on segments
        if (drawNodes && i % (steps / 4) == 0 && belongsHere) {
            drawCircle(
                color = color,
                radius = (strokeWidth * 1.5f) * proj.scale,
                center = Offset(screenX, screenY)
            )
        }

        // Optional fine ticks
        if (drawTicks && i % 4 == 0 && belongsHere) {
            val tickNorm = rotateEuler(cos(angle) * (radius + 6f), sin(angle) * (radius + 6f), 0f, pitch, yaw, roll)
            val tickProj = projectPerspective(tickNorm.x, tickNorm.y, tickNorm.z + centerZ, 780f)
            drawLine(
                color = color.copy(alpha = color.alpha * 0.7f),
                start = Offset(screenX, screenY),
                end = Offset(center.x + tickProj.x, center.y + tickProj.y),
                strokeWidth = 1f
            )
        }
    }

    if (pathStarted) {
        drawPath(path, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    }
}

private fun DrawScope.draw3DSatelliteNode(
    center: Offset,
    orbitRadius: Float,
    angleDeg: Float,
    orbitPitch: Float,
    orbitRoll: Float,
    centerZ: Float,
    isFront: Boolean,
    color: Color
) {
    val rad = Math.toRadians(angleDeg.toDouble()).toFloat()
    val sx = cos(rad) * orbitRadius
    val sy = sin(rad) * orbitRadius
    val rotated = rotateEuler(sx, sy, 0f, orbitPitch, 0f, orbitRoll)
    val finalZ = rotated.z + centerZ

    val belongsHere = if (isFront) finalZ >= 0f else finalZ < 0f
    if (!belongsHere) return

    val proj = projectPerspective(rotated.x, rotated.y, finalZ, 780f)
    val screenPt = Offset(center.x + proj.x, center.y + proj.y)
    val nodeRadius = 5.5f * proj.scale

    drawCircle(
        color = color,
        radius = nodeRadius,
        center = screenPt
    )
    drawCircle(
        color = color.copy(alpha = 0.35f),
        radius = nodeRadius * 2.2f,
        center = screenPt,
        style = Stroke(width = 1.2f)
    )
}

private fun DrawScope.render3DParticle(
    center: Offset,
    particle: Particle3D,
    baseRadius: Float,
    pitch: Float,
    yaw: Float,
    roll: Float,
    centerZ: Float,
    phase: Float,
    isFront: Boolean,
    color: Color
) {
    val dynRadius = baseRadius * particle.radRatio + sin((phase + particle.theta) * 2f * PI.toFloat()) * 8f
    val px = dynRadius * cos(particle.phi) * cos(particle.theta + phase * 2f * PI.toFloat())
    val py = dynRadius * cos(particle.phi) * sin(particle.theta + phase * 2f * PI.toFloat())
    val pz = dynRadius * sin(particle.phi)

    val rotated = rotateEuler(px, py, pz, pitch, yaw, roll)
    val finalZ = rotated.z + centerZ

    val belongsHere = if (isFront) finalZ >= 0f else finalZ < 0f
    if (!belongsHere) return

    val proj = projectPerspective(rotated.x, rotated.y, finalZ, 780f)
    val screenPt = Offset(center.x + proj.x, center.y + proj.y)
    val pSize = (particle.size * proj.scale).coerceAtLeast(1.2f)

    drawCircle(
        color = color.copy(alpha = if (isFront) 0.85f else 0.35f),
        radius = pSize,
        center = screenPt
    )
}

private fun DrawScope.draw3DAudioRibbon(
    center: Offset,
    radius: Float,
    rms: Float,
    timeSec: Float,
    color: Color
) {
    val points = 32
    val dAngle = (2f * PI.toFloat()) / points
    val path = Path()

    for (i in 0..points) {
        val angle = i * dAngle
        val waveMod = sin(angle * 6f + timeSec * 12f) * (rms * 1.6f).coerceIn(2f, 14f)
        val r = radius + waveMod
        val x = center.x + cos(angle) * r
        val y = center.y + sin(angle) * r

        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    drawPath(
        path = path,
        color = color.copy(alpha = 0.75f),
        style = Stroke(width = 1.8f)
    )
}
