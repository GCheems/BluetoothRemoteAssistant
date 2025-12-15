package com.example.bluetoothremoteassistant.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bluetoothremoteassistant.data.model.BleDevice
import com.example.bluetoothremoteassistant.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * 设备历史数据模型
 */
@Serializable
data class DeviceHistory(
    val address: String,
    val name: String?,
    val lastConnectedTime: Long
)

/**
 * 设备历史记录仓库
 * 使用 DataStore 持久化存储最近连接的设备
 */
class DeviceHistoryRepository(private val context: Context) {
    
    companion object {
        private val Context.deviceHistoryDataStore: DataStore<Preferences> by preferencesDataStore(
            name = Constants.DATASTORE_DEVICE_HISTORY
        )
        
        private val HISTORY_KEY = stringPreferencesKey("device_history")
        
        @Volatile
        private var instance: DeviceHistoryRepository? = null
        
        fun getInstance(context: Context): DeviceHistoryRepository {
            return instance ?: synchronized(this) {
                instance ?: DeviceHistoryRepository(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }
    
    /**
     * JSON 序列化器
     */
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    /**
     * 获取设备历史列表的 Flow
     */
    val deviceHistory: Flow<List<DeviceHistory>> = context.deviceHistoryDataStore.data
        .map { preferences ->
            val jsonString = preferences[HISTORY_KEY] ?: return@map emptyList()
            try {
                json.decodeFromString<List<DeviceHistory>>(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    
    /**
     * 添加设备到历史记录
     */
    suspend fun addDevice(device: BleDevice) {
        context.deviceHistoryDataStore.edit { preferences ->
            val currentJson = preferences[HISTORY_KEY] ?: "[]"
            val currentHistory = try {
                json.decodeFromString<List<DeviceHistory>>(currentJson)
            } catch (e: Exception) {
                emptyList()
            }
            
            // 移除已存在的相同设备
            val filteredHistory = currentHistory.filter { it.address != device.address }
            
            // 添加新设备到列表开头
            val newHistory = listOf(
                DeviceHistory(
                    address = device.address,
                    name = device.name,
                    lastConnectedTime = System.currentTimeMillis()
                )
            ) + filteredHistory
            
            // 限制最大历史记录数量
            val limitedHistory = newHistory.take(Constants.MAX_DEVICE_HISTORY)
            
            preferences[HISTORY_KEY] = json.encodeToString(limitedHistory)
        }
    }
    
    /**
     * 清空设备历史记录
     */
    suspend fun clearHistory() {
        context.deviceHistoryDataStore.edit { preferences ->
            preferences.remove(HISTORY_KEY)
        }
    }
    
    /**
     * 删除指定设备
     */
    suspend fun removeDevice(address: String) {
        context.deviceHistoryDataStore.edit { preferences ->
            val currentJson = preferences[HISTORY_KEY] ?: "[]"
            val currentHistory = try {
                json.decodeFromString<List<DeviceHistory>>(currentJson)
            } catch (e: Exception) {
                emptyList()
            }
            
            val newHistory = currentHistory.filter { it.address != address }
            preferences[HISTORY_KEY] = json.encodeToString(newHistory)
        }
    }
}
