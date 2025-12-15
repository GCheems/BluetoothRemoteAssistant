package com.example.bluetoothremoteassistant.data.model

import java.util.UUID

/**
 * BLE 服务数据类
 * @param uuid 服务 UUID
 * @param characteristics 该服务下的特征值列表
 */
data class BleService(
    val uuid: UUID,
    val characteristics: List<BleCharacteristic>
) {
    /**
     * UUID 短格式显示（仅显示前8位）
     */
    val shortUuid: String
        get() = uuid.toString().substring(0, 8).uppercase()

    /**
     * 是否为标准服务（根据 UUID 判断）
     */
    val isStandardService: Boolean
        get() = STANDARD_SERVICES.containsKey(uuid)

    /**
     * 服务名称（标准服务返回名称，自定义服务返回 UUID）
     */
    val serviceName: String
        get() = STANDARD_SERVICES[uuid] ?: "自定义服务"

    companion object {
        /**
         * 标准 BLE 服务 UUID 映射表
         */
        private val STANDARD_SERVICES = mapOf(
            UUID.fromString("00001800-0000-1000-8000-00805f9b34fb") to "通用访问",
            UUID.fromString("00001801-0000-1000-8000-00805f9b34fb") to "通用属性",
            UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb") to "设备信息",
            UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb") to "电池服务",
            UUID.fromString("00001805-0000-1000-8000-00805f9b34fb") to "当前时间",
            UUID.fromString("00001810-0000-1000-8000-00805f9b34fb") to "血压",
            UUID.fromString("00001811-0000-1000-8000-00805f9b34fb") to "提醒通知",
            UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb") to "心率",
            UUID.fromString("00001812-0000-1000-8000-00805f9b34fb") to "HID",
        )
    }
}
