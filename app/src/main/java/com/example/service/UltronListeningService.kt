package com.example.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.audio.UltronWakeWordDetector
import com.example.state.UltronAssistantManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UltronListeningService : Service() {

    private var wakeWordDetector: UltronWakeWordDetector? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        activeInstance = this
        _isServiceRunning.value = true

        createNotificationChannel()
        acquireWakeLock()

        wakeWordDetector = UltronWakeWordDetector(this) { detectedPhrase ->
            Log.i(TAG, "Speech matched: $detectedPhrase")
            wakeWordDetector?.pauseForSpeech()
            if (UltronAssistantManager.isAwaitingCommandAfterGreeting.value) {
                // Ultron previously greeted Tony with "Yes, Tony." and is now receiving the actual directive
                UltronAssistantManager.sendUserQuery(detectedPhrase)
            } else {
                UltronAssistantManager.onWakeWordDetected("VOICE")
            }
        }

        Log.i(TAG, "UltronListeningService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_LISTENING
        Log.i(TAG, "onStartCommand action: $action")

        when (action) {
            ACTION_START_LISTENING -> {
                startForegroundWithNotification()
                wakeWordDetector?.start()
            }
            ACTION_STOP_LISTENING -> {
                wakeWordDetector?.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_SIMULATE_WAKE -> {
                wakeWordDetector?.pauseForSpeech()
                UltronAssistantManager.onWakeWordDetected("MANUAL_TEST")
            }
        }

        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val hasMicPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val notification = buildForegroundNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // In Android 14+ (targetSdk 34+), foregroundServiceType="microphone" strictly requires
                // RECORD_AUDIO runtime permission and eligible app state.
                // If RECORD_AUDIO is not granted yet, we fallback to general foreground type (0)
                // so the service does not crash with SecurityException.
                val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasMicPermission) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                } else {
                    0
                }
                startForeground(NOTIFICATION_ID, notification, serviceType)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (se: SecurityException) {
            Log.e(TAG, "SecurityException starting foreground service: ${se.message}. Falling back to standard foreground.")
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start foreground service: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in startForeground: ${e.message}")
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            101,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, UltronListeningService::class.java).apply {
            action = ACTION_STOP_LISTENING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            102,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val testIntent = Intent(this, UltronListeningService::class.java).apply {
            action = ACTION_SIMULATE_WAKE
        }
        val testPendingIntent = PendingIntent.getService(
            this,
            103,
            testIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(android.R.drawable.ic_media_play, getString(R.string.action_test), testPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.action_stop), stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "UltronAssistant::ListeningWakeLock"
            )?.apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L) // 10 min safe timeout, renewed if needed
            }
        } catch (e: Exception) {
            Log.w(TAG, "WakeLock could not be acquired: ${e.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing wake lock: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activeInstance = null
        _isServiceRunning.value = false
        wakeWordDetector?.destroy()
        wakeWordDetector = null
        releaseWakeLock()
        Log.i(TAG, "UltronListeningService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "UltronListeningService"
        const val CHANNEL_ID = "ultron_assistant_voice_channel"
        const val NOTIFICATION_ID = 4401

        const val ACTION_START_LISTENING = "com.example.action.START_LISTENING"
        const val ACTION_STOP_LISTENING = "com.example.action.STOP_LISTENING"
        const val ACTION_SIMULATE_WAKE = "com.example.action.SIMULATE_WAKE"

        @Volatile
        private var activeInstance: UltronListeningService? = null

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        fun resumeDetectorAfterSpeech() {
            activeInstance?.wakeWordDetector?.resumeAfterSpeech()
        }
    }
}
