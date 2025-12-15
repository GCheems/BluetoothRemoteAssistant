package com.example.bluetoothremoteassistant.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bluetoothremoteassistant.data.model.CustomButton
import com.example.bluetoothremoteassistant.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * 自定义按键数据仓库
 * 使用 DataStore 持久化存储自定义按键配置
 */
class CustomButtonRepository(private val context: Context) {
    
    companion object {
        private val Context.customButtonsDataStore: DataStore<Preferences> by preferencesDataStore(
            name = Constants.DATASTORE_CUSTOM_BUTTONS
        )
        
        private val BUTTONS_KEY = stringPreferencesKey("custom_buttons")
        
        @Volatile
        private var instance: CustomButtonRepository? = null
        
        fun getInstance(context: Context): CustomButtonRepository {
            return instance ?: synchronized(this) {
                instance ?: CustomButtonRepository(context.applicationContext).also { 
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
     * 获取自定义按键列表的 Flow
     */
    val customButtons: Flow<List<CustomButton>> = context.customButtonsDataStore.data
        .map { preferences ->
            val jsonString = preferences[BUTTONS_KEY] ?: return@map emptyList()
            try {
                json.decodeFromString<List<CustomButton>>(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    
    /**
     * 保存自定义按键列表
     */
    suspend fun saveButtons(buttons: List<CustomButton>) {
        try {
            val jsonString = json.encodeToString(buttons)
            context.customButtonsDataStore.edit { preferences ->
                preferences[BUTTONS_KEY] = jsonString
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 添加自定义按键
     */
    suspend fun addButton(button: CustomButton) {
        context.customButtonsDataStore.edit { preferences ->
            val currentJson = preferences[BUTTONS_KEY] ?: "[]"
            val currentButtons = try {
                json.decodeFromString<List<CustomButton>>(currentJson)
            } catch (e: Exception) {
                emptyList()
            }
            
            val newButtons = currentButtons + button
            preferences[BUTTONS_KEY] = json.encodeToString(newButtons)
        }
    }
    
    /**
     * 删除自定义按键
     */
    suspend fun removeButton(button: CustomButton) {
        context.customButtonsDataStore.edit { preferences ->
            val currentJson = preferences[BUTTONS_KEY] ?: "[]"
            val currentButtons = try {
                json.decodeFromString<List<CustomButton>>(currentJson)
            } catch (e: Exception) {
                emptyList()
            }
            
            val newButtons = currentButtons.filter { it.id != button.id }
            preferences[BUTTONS_KEY] = json.encodeToString(newButtons)
        }
    }
    
    /**
     * 清空所有自定义按键
     */
    suspend fun clearButtons() {
        context.customButtonsDataStore.edit { preferences ->
            preferences.remove(BUTTONS_KEY)
        }
    }
}
