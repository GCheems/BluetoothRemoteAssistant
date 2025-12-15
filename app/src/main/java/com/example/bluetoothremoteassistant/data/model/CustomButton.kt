package com.example.bluetoothremoteassistant.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 自定义按键模型
 */
@Serializable
data class CustomButton(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val data: String,
    val isHex: Boolean
)
