package com.example.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.math.cos
import kotlin.math.sin

/**
 * Encapsulates real-time phone gyroscope / accelerometer tilt data
 * with smooth dampening, parallax layer multipliers, and automatic
 * software fallback if hardware sensors are absent.
 */
class ParallaxSensorState(
    val tiltX: Float, // Normalized -1f..+1f (pitch: tilt up/down)
    val tiltY: Float, // Normalized -1f..+1f (roll: tilt left/right)
    val hasHardwareSensor: Boolean
)

@Composable
fun rememberParallaxSensorState(): State<ParallaxSensorState> {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val sensorState = remember {
        mutableStateOf(ParallaxSensorState(0f, 0f, false))
    }

    DisposableEffect(lifecycleOwner, context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        var rawTargetX = 0f
        var rawTargetY = 0f
        var currentX = 0f
        var currentY = 0f
        var hasReceivedEvents = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                hasReceivedEvents = true

                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        val rotationMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        // orientation[1] = pitch (-pi/2 to pi/2), orientation[2] = roll (-pi to pi)
                        rawTargetX = (orientation[1] / (Math.PI / 3.0)).toFloat().coerceIn(-1f, 1f)
                        rawTargetY = (orientation[2] / (Math.PI / 3.0)).toFloat().coerceIn(-1f, 1f)
                    }
                    Sensor.TYPE_GRAVITY, Sensor.TYPE_ACCELEROMETER -> {
                        // event.values[0] = x (roll), event.values[1] = y (pitch)
                        rawTargetY = (event.values[0] / 9.81f).coerceIn(-1f, 1f)
                        rawTargetX = ((event.values[1] - 5.0f) / 9.81f).coerceIn(-1f, 1f)
                    }
                }

                // Smooth dampening filter
                currentX += (rawTargetX - currentX) * 0.18f
                currentY += (rawTargetY - currentY) * 0.18f

                sensorState.value = ParallaxSensorState(
                    tiltX = currentX,
                    tiltY = currentY,
                    hasHardwareSensor = true
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        var isRegistered = false
        if (rotationSensor != null && sensorManager != null) {
            isRegistered = sensorManager.registerListener(
                listener,
                rotationSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (!isRegistered && rotationSensor != null && sensorManager != null) {
                    isRegistered = sensorManager.registerListener(
                        listener,
                        rotationSensor,
                        SensorManager.SENSOR_DELAY_GAME
                    )
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                if (isRegistered && sensorManager != null) {
                    sensorManager.unregisterListener(listener)
                    isRegistered = false
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            if (isRegistered && sensorManager != null) {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    return sensorState
}
