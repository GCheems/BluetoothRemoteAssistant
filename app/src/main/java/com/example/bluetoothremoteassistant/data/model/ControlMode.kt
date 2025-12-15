package com.example.bluetoothremoteassistant.data.model

/**
 * 控制模式枚举
 */
enum class ControlMode {
    STANDARD,   // 标准模式：显示服务和特征值列表
    GAMEPAD,    // 手柄模式：显示方向键和 AB 键
    CUSTOM      // 自定义模式：显示用户自定义按键
}
