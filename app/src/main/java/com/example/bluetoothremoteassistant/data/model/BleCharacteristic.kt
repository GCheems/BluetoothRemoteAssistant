package com.example.bluetoothremoteassistant.data.model

import java.util.UUID

/**
 * BLE 特征值数据类
 * @param uuid 特征值 UUID
 * @param properties 属性标志位
 * @param value 当前值
 */
data class BleCharacteristic(
    val uuid: UUID,
    val properties: Int,
    val value: ByteArray? = null
) {
    /**
     * UUID 短格式显示（仅显示前8位）
     */
    val shortUuid: String
        get() = uuid.toString().substring(0, 8).uppercase()

    /**
     * 是否可读
     */
    val isReadable: Boolean
        get() = (properties and 0x02) != 0

    /**
     * 是否可写
     */
    val isWritable: Boolean
        get() = (properties and 0x08) != 0 || (properties and 0x04) != 0

    /**
     * 是否支持通知
     */
    val isNotifiable: Boolean
        get() = (properties and 0x10) != 0

    /**
     * 是否支持指示
     */
    val isIndicatable: Boolean
        get() = (properties and 0x20) != 0

    /**
     * 属性描述文本
     */
    val propertiesText: String
        get() = buildList {
            if (isReadable) add("读")
            if (isWritable) add("写")
            if (isNotifiable) add("通知")
            if (isIndicatable) add("指示")
        }.joinToString(", ")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleCharacteristic

        if (uuid != other.uuid) return false
        if (properties != other.properties) return false
        if (value != null) {
            if (other.value == null) return false
            if (!value.contentEquals(other.value)) return false
        } else if (other.value != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + properties
        result = 31 * result + (value?.contentHashCode() ?: 0)
        return result
    }
}
