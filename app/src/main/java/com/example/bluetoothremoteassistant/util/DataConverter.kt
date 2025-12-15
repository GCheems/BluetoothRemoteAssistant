package com.example.bluetoothremoteassistant.util

/**
 * 数据转换工具类
 * 提供字符串、Hex 和 ByteArray 之间的转换
 */
object DataConverter {

    /**
     * 字符串转 ByteArray
     */
    fun stringToBytes(str: String): ByteArray {
        return str.toByteArray(Charsets.UTF_8)
    }

    /**
     * ByteArray 转字符串
     */
    fun bytesToString(bytes: ByteArray): String {
        return String(bytes, Charsets.UTF_8)
    }

    /**
     * Hex 字符串转 ByteArray
     * 输入格式: "01 02 03" 或 "010203"
     */
    fun hexToBytes(hex: String): ByteArray? {
        return try {
            // 移除空格和其他分隔符
            val cleanHex = hex.replace(Regex("[^0-9A-Fa-f]"), "")
            
            if (cleanHex.length % 2 != 0) {
                return null // Hex 字符串长度必须是偶数
            }

            ByteArray(cleanHex.length / 2) { i ->
                cleanHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * ByteArray 转 Hex 字符串
     * 输出格式: "01 02 03"
     */
    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString(" ") { "%02X".format(it) }
    }

    /**
     * 验证 Hex 字符串格式
     */
    fun isValidHex(hex: String): Boolean {
        val cleanHex = hex.replace(Regex("[^0-9A-Fa-f]"), "")
        return cleanHex.length % 2 == 0 && cleanHex.matches(Regex("[0-9A-Fa-f]+"))
    }
}
