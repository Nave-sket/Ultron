package com.example.model

enum class DeviceType {
    PHONE,
    BLUETOOTH_AUDIO,
    SMART_WATCH,
    SMART_DEVICE,
    SATELLITE_NODE
}

enum class DeviceStatus {
    ONLINE,
    CONNECTED,
    STANDBY,
    DISCONNECTED
}

data class ConnectedDevice(
    val id: String,
    val name: String,
    val type: DeviceType,
    val status: DeviceStatus,
    val signalStrength: Int = 85, // percentage
    val batteryLevel: Int = 92, // percentage
    val isAuthorized: Boolean = true,
    val address: String? = null,
    val lastPing: String = "NOW"
)
