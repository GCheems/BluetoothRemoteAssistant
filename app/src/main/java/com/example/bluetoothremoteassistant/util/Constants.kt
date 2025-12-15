package com.example.bluetoothremoteassistant.util

import java.util.UUID

/**
 * 应用常量定义
 */
object Constants {
    
    // ==================== 蓝牙相关常量 ====================
    
    /**
     * 连接超时时间（毫秒）
     */
    const val CONNECTION_TIMEOUT_MS = 10_000L
    
    /**
     * GATT 操作超时时间（毫秒）
     */
    const val GATT_OPERATION_TIMEOUT_MS = 5_000L
    
    /**
     * 扫描超时时间（毫秒）
     */
    const val SCAN_TIMEOUT_MS = 30_000L
    
    // ==================== 常用蓝牙 UUID ====================
    
    /**
     * 客户端特征配置描述符 UUID
     * 用于启用/禁用通知和指示
     */
    val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID = 
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    
    /**
     * 通用串口服务 UUID
     */
    val UART_SERVICE_UUID: UUID = 
        UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    
    /**
     * UART TX 特征值 UUID（从设备发送到手机）
     */
    val UART_TX_CHARACTERISTIC_UUID: UUID = 
        UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
    
    /**
     * UART RX 特征值 UUID（从手机发送到设备）
     */
    val UART_RX_CHARACTERISTIC_UUID: UUID = 
        UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
    
    // ==================== 日志相关常量 ====================
    
    /**
     * 最大日志条数
     * 超过此数量时，将删除最旧的日志
     */
    const val MAX_LOG_COUNT = 500
    
    /**
     * 日志时间戳格式
     */
    const val LOG_TIMESTAMP_FORMAT = "HH:mm:ss.SSS"
    
    // ==================== 数据持久化相关常量 ====================
    
    /**
     * 最大历史设备数量
     */
    const val MAX_DEVICE_HISTORY = 20
    
    /**
     * DataStore 文件名 - 自定义按键
     */
    const val DATASTORE_CUSTOM_BUTTONS = "custom_buttons"
    
    /**
     * DataStore 文件名 - 设备历史
     */
    const val DATASTORE_DEVICE_HISTORY = "device_history"
    
    // ==================== UI 相关常量 ====================
    
    /**
     * 设备信号强度过滤阈值（dBm）
     */
    const val RSSI_FILTER_THRESHOLD = -90
    
    /**
     * 错误提示显示时间（毫秒）
     */
    const val ERROR_MESSAGE_DURATION_MS = 3_000L
}
