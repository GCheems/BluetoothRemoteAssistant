package com.example.bluetoothremoteassistant.data.model

/**
 * 遥控指令枚举
 * 定义小车遥控的各种指令
 */
enum class ControlCommand(
    val label: String,
    val icon: String,
    val command: String
) {
    FORWARD("前进", "↑", "F"),
    BACKWARD("后退", "↓", "B"),
    TURN_LEFT("左转", "←", "L"),
    TURN_RIGHT("右转", "→", "R"),
    STOP("停止", "■", "S");

    /**
     * 获取指令的字节数组
     */
    fun toBytes(): ByteArray = command.toByteArray(Charsets.UTF_8)
}
