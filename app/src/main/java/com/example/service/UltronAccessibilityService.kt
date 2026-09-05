package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UltronAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceConnected.value = true
        Log.i(TAG, "UltronAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No event capture needed for pure navigation actions
    }

    override fun onInterrupt() {
        Log.w(TAG, "UltronAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) {
            instance = null
            _isServiceConnected.value = false
        }
        Log.i(TAG, "UltronAccessibilityService destroyed")
    }

    companion object {
        private const val TAG = "UltronAccessService"

        @Volatile
        private var instance: UltronAccessibilityService? = null

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        /**
         * Executes the Android Global Back action.
         * Returns true if successfully dispatched.
         */
        fun performBackAction(): Boolean {
            val service = instance
            return if (service != null) {
                val result = service.performGlobalAction(GLOBAL_ACTION_BACK)
                Log.i(TAG, "performGlobalAction(GLOBAL_ACTION_BACK) result: $result")
                result
            } else {
                Log.w(TAG, "Cannot perform back action: service is not running or enabled")
                false
            }
        }

        /**
         * Checks whether the UltronAccessibilityService is enabled in Android system settings.
         */
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            if (instance != null) return true

            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val expectedFull = ComponentName(context, UltronAccessibilityService::class.java).flattenToString()
            val expectedShort = ComponentName(context, UltronAccessibilityService::class.java).flattenToShortString()

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)

            while (colonSplitter.hasNext()) {
                val component = colonSplitter.next()
                if (component.equals(expectedFull, ignoreCase = true) ||
                    component.equals(expectedShort, ignoreCase = true)
                ) {
                    return true
                }
            }
            return false
        }
    }
}
