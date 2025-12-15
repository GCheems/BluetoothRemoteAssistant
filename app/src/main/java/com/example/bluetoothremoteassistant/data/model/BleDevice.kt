package com.example.bluetoothremoteassistant.data.model

/**
 * BLE 设备数据类
 * @param name 设备名称
 * @param address MAC 地址
 * @param rssi 信号强度
 */
data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int
) {
    /**
     * 显示名称，如果设备名为空则显示 "未知设备"
     */
    val displayName: String
        get() = name.ifEmpty { "未知设备" }
}

/**
 * 连接状态枚举
 */
enum class ConnectionState {
    DISCONNECTED,   // 断开连接
    CONNECTING,     // 连接中
    CONNECTED,      // 已连接
    DISCONNECTING   // 断开中
}
