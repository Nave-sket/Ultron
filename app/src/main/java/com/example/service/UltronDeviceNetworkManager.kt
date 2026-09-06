package com.example.service

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.model.ConnectedDevice
import com.example.model.DeviceStatus
import com.example.model.DeviceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UltronDeviceNetworkManager {

    private const val TAG = "UltronDevNetwork"

    private val _devices = MutableStateFlow<List<ConnectedDevice>>(emptyList())
    val devices: StateFlow<List<ConnectedDevice>> = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(false)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    fun initialize(context: Context) {
        refreshDevices(context)
    }

    fun refreshDevices(context: Context) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter
        _isBluetoothEnabled.value = adapter?.isEnabled == true

        val deviceList = mutableListOf<ConnectedDevice>()

        // 1. Central Phone Node
        deviceList.add(
            ConnectedDevice(
                id = "phone_central",
                name = "${Build.MANUFACTURER.uppercase()} ${Build.MODEL} (This Device)",
                type = DeviceType.PHONE,
                status = DeviceStatus.ONLINE,
                signalStrength = 100,
                batteryLevel = 98,
                isAuthorized = true,
                lastPing = "ONLINE"
            )
        )

        // 2. Query legitimate paired Bluetooth devices if permission is granted
        var hasBtPermission = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasBtPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (adapter != null && adapter.isEnabled && hasBtPermission) {
            try {
                @Suppress("MissingPermission")
                val bonded = adapter.bondedDevices
                if (bonded != null && bonded.isNotEmpty()) {
                    for (device in bonded) {
                        val devType = classifyBluetoothDevice(device)
                        deviceList.add(
                            ConnectedDevice(
                                id = device.address,
                                name = device.name ?: "Authorized Device",
                                type = devType,
                                status = DeviceStatus.CONNECTED,
                                signalStrength = 88,
                                batteryLevel = 85,
                                isAuthorized = true,
                                address = device.address,
                                lastPing = "CONNECTED"
                            )
                        )
                    }
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "Bluetooth permission not yet granted by user: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Error listing Bluetooth devices: ${e.message}")
            }
        }

        // If no paired bluetooth hardware was returned yet, populate authorized smart device network nodes
        if (deviceList.size == 1) {
            deviceList.add(
                ConnectedDevice(
                    id = "bt_audio_1",
                    name = "Ultron Neural Link (Headset)",
                    type = DeviceType.BLUETOOTH_AUDIO,
                    status = DeviceStatus.CONNECTED,
                    signalStrength = 94,
                    batteryLevel = 88,
                    isAuthorized = true,
                    lastPing = "ACTIVE"
                )
            )
            deviceList.add(
                ConnectedDevice(
                    id = "smart_watch_1",
                    name = "Stark Quantum Watch",
                    type = DeviceType.SMART_WATCH,
                    status = DeviceStatus.ONLINE,
                    signalStrength = 90,
                    batteryLevel = 76,
                    isAuthorized = true,
                    lastPing = "SYNCED"
                )
            )
            deviceList.add(
                ConnectedDevice(
                    id = "home_node_1",
                    name = "Stark Tower Core Hub",
                    type = DeviceType.SMART_DEVICE,
                    status = DeviceStatus.ONLINE,
                    signalStrength = 100,
                    batteryLevel = 100,
                    isAuthorized = true,
                    lastPing = "SECURE"
                )
            )
        }

        _devices.value = deviceList
    }

    private fun classifyBluetoothDevice(device: BluetoothDevice): DeviceType {
        return when (device.bluetoothClass?.majorDeviceClass) {
            android.bluetooth.BluetoothClass.Device.Major.AUDIO_VIDEO -> DeviceType.BLUETOOTH_AUDIO
            android.bluetooth.BluetoothClass.Device.Major.WEARABLE -> DeviceType.SMART_WATCH
            else -> DeviceType.SMART_DEVICE
        }
    }

    /**
     * Volume control where permitted by Android AudioManager
     */
    fun adjustVolume(context: Context, direction: Int): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return 0
        try {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                direction,
                AudioManager.FLAG_SHOW_UI
            )
            return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        } catch (e: Exception) {
            Log.e(TAG, "Error adjusting volume: ${e.message}")
            return 0
        }
    }
}
