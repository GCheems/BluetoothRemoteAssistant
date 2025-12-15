package com.example.bluetoothremoteassistant.data.model

/**
 * 蓝牙错误枚举
 * 定义所有可能的蓝牙操作错误类型
 */
enum class BleError(val code: Int, val message: String) {
    // 设备相关错误
    DEVICE_NOT_FOUND(1001, "未找到设备"),
    DEVICE_DISCONNECTED(1002, "设备已断开连接"),
    
    // 连接相关错误
    CONNECTION_FAILED(2001, "连接失败"),
    CONNECTION_TIMEOUT(2002, "连接超时"),
    ALREADY_CONNECTED(2003, "设备已连接"),
    
    // 服务和特征值相关错误
    SERVICE_NOT_FOUND(3001, "未找到服务"),
    CHARACTERISTIC_NOT_FOUND(3002, "未找到特征值"),
    CHARACTERISTIC_NOT_READABLE(3003, "特征值不支持读取"),
    CHARACTERISTIC_NOT_WRITABLE(3004, "特征值不支持写入"),
    CHARACTERISTIC_NOT_NOTIFIABLE(3005, "特征值不支持通知"),
    
    // 操作相关错误
    READ_FAILED(4001, "读取失败"),
    WRITE_FAILED(4002, "写入失败"),
    NOTIFICATION_FAILED(4003, "设置通知失败"),
    
    // 权限相关错误
    PERMISSION_DENIED(5001, "权限被拒绝"),
    BLUETOOTH_DISABLED(5002, "蓝牙未开启"),
    
    // 扫描相关错误
    SCAN_FAILED(6001, "扫描失败"),
    SCAN_ALREADY_STARTED(6002, "扫描已在进行中"),
    
    // 其他错误
    UNKNOWN_ERROR(9999, "未知错误"),
    OPERATION_IN_PROGRESS(9998, "操作正在进行中"),
    INVALID_DATA(9997, "数据格式无效");
    
    companion object {
        /**
         * 根据错误码获取错误类型
         */
        fun fromCode(code: Int): BleError {
            return values().find { it.code == code } ?: UNKNOWN_ERROR
        }
    }
}
