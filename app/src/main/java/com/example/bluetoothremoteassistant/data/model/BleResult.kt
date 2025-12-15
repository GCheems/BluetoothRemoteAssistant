package com.example.bluetoothremoteassistant.data.model

/**
 * 蓝牙操作结果密封类
 * 统一封装所有蓝牙操作的返回结果
 */
sealed class BleResult<out T> {
    /**
     * 操作成功
     */
    data class Success<T>(val data: T) : BleResult<T>()
    
    /**
     * 操作失败
     */
    data class Error(
        val error: BleError,
        val details: String? = null
    ) : BleResult<Nothing>() {
        val message: String
            get() = details ?: error.message
    }
    
    /**
     * 操作进行中
     */
    object Loading : BleResult<Nothing>()
    
    /**
     * 判断是否成功
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * 判断是否失败
     */
    fun isError(): Boolean = this is Error
    
    /**
     * 获取成功的数据，如果失败则返回 null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
    
    /**
     * 获取错误信息，如果成功则返回 null
     */
    fun errorOrNull(): Error? = when (this) {
        is Error -> this
        else -> null
    }
    
    companion object {
        /**
         * 创建成功结果
         */
        fun <T> success(data: T): BleResult<T> = Success(data)
        
        /**
         * 创建失败结果
         */
        fun error(error: BleError, details: String? = null): BleResult<Nothing> = 
            Error(error, details)
        
        /**
         * 创建加载中状态
         */
        fun loading(): BleResult<Nothing> = Loading
    }
}
