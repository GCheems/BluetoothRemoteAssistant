package com.example.bluetoothremoteassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothremoteassistant.data.BluetoothLeManager
import com.example.bluetoothremoteassistant.data.model.BleDevice
import com.example.bluetoothremoteassistant.data.model.BleService
import com.example.bluetoothremoteassistant.data.model.ConnectionState
import com.example.bluetoothremoteassistant.data.model.ControlCommand
import com.example.bluetoothremoteassistant.data.model.ControlMode
import com.example.bluetoothremoteassistant.data.model.CustomButton
import com.example.bluetoothremoteassistant.data.repository.CustomButtonRepository
import com.example.bluetoothremoteassistant.data.repository.DeviceHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 主 ViewModel
 * 管理应用的整体状态和业务逻辑
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // 蓝牙管理器实例
    private val bleManager = BluetoothLeManager.getInstance(application)
    
    // 数据仓库实例
    private val customButtonRepository = CustomButtonRepository.getInstance(application)
    private val deviceHistoryRepository = DeviceHistoryRepository.getInstance(application)
    
    init {
        // 加载自定义按键
        loadCustomButtons()
        // 监听连接状态，当连接成功时保存设备到历史
        monitorConnectionState()
    }

    // 扫描到的设备列表
    val scannedDevices: StateFlow<List<BleDevice>> = bleManager.scannedDevices
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 连接状态
    val connectionState: StateFlow<ConnectionState> = bleManager.connectionState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ConnectionState.DISCONNECTED
        )

    // 当前连接的设备
    val connectedDevice: StateFlow<BleDevice?> = bleManager.connectedDevice
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // 服务列表
    val services: StateFlow<List<BleService>> = bleManager.services
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 数据日志
    val dataLogs: StateFlow<List<String>> = bleManager.dataLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 遥控模式状态
    private val _controlMode = MutableStateFlow(ControlMode.STANDARD)
    val controlMode: StateFlow<ControlMode> = _controlMode.asStateFlow()

    // 自定义按键列表（从持久化存储加载）
    val customButtons: StateFlow<List<CustomButton>> = customButtonRepository.customButtons
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 错误消息状态
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 自定义服务和特征值缓存（用于快速发送遥控指令）
    private var cachedCustomServiceUuid: UUID? = null
    private var cachedCustomCharacteristicUuid: UUID? = null

    /**
     * 检查蓝牙是否可用
     */
    fun isBluetoothEnabled(): Boolean {
        return bleManager.isBluetoothEnabled()
    }

    /**
     * 开始扫描
     */
    fun startScan() {
        viewModelScope.launch {
            bleManager.startScan()
        }
    }

    /**
     * 停止扫描
     */
    fun stopScan() {
        viewModelScope.launch {
            bleManager.stopScan()
        }
    }

    /**
     * 连接设备
     */
    fun connectDevice(device: BleDevice) {
        viewModelScope.launch {
            bleManager.connect(device)
        }
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        viewModelScope.launch {
            bleManager.disconnect()
        }
    }

    /**
     * 读取特征值
     */
    fun readCharacteristic(serviceUuid: UUID, characteristicUuid: UUID) {
        viewModelScope.launch {
            bleManager.readCharacteristic(serviceUuid, characteristicUuid)
        }
    }

    /**
     * 写入特征值
     */
    fun writeCharacteristic(serviceUuid: UUID, characteristicUuid: UUID, data: ByteArray) {
        viewModelScope.launch {
            bleManager.writeCharacteristic(serviceUuid, characteristicUuid, data)
        }
    }

    /**
     * 设置特征值通知
     */
    fun setCharacteristicNotification(serviceUuid: UUID, characteristicUuid: UUID, enable: Boolean) {
        viewModelScope.launch {
            bleManager.setCharacteristicNotification(serviceUuid, characteristicUuid, enable)
        }
    }

    /**
     * 清空日志
     */
    fun clearLogs() {
        viewModelScope.launch {
            bleManager.clearLogs()
        }
    }

    /**
     * 设置控制模式
     */
    fun setControlMode(mode: ControlMode) {
        _controlMode.value = mode
    }

    /**
     * 切换到自定义模式
     */
    fun switchToCustomMode() {
        _controlMode.value = ControlMode.CUSTOM
    }

    /**
     * 切换下一个控制模式
     */
    fun toggleNextControlMode() {
        _controlMode.value = when (_controlMode.value) {
            ControlMode.STANDARD -> ControlMode.GAMEPAD
            ControlMode.GAMEPAD -> ControlMode.CUSTOM
            ControlMode.CUSTOM -> ControlMode.STANDARD
        }
    }


    /**
     * 加载自定义按键
     */
    private fun loadCustomButtons() {
        // customButtons 现在直接从 repository 的 Flow 获取，不需要手动加载
    }
    
    /**
     * 监听连接状态，当连接成功时保存设备到历史
     */
    private fun monitorConnectionState() {
        viewModelScope.launch {
            connectionState.collect { state ->
                if (state == ConnectionState.CONNECTED) {
                    connectedDevice.value?.let { device ->
                        deviceHistoryRepository.addDevice(device)
                    }
                }
            }
        }
    }

    /**
     * 添加自定义按键
     */
    fun addCustomButton(button: CustomButton) {
        viewModelScope.launch {
            customButtonRepository.addButton(button)
        }
    }

    /**
     * 删除自定义按键
     */
    fun removeCustomButton(button: CustomButton) {
        viewModelScope.launch {
            customButtonRepository.removeButton(button)
        }
    }

    /**
     * 发送自定义按键数据
     */
    fun sendCustomButtonData(button: CustomButton) {
        viewModelScope.launch {
            val serviceUuid = cachedCustomServiceUuid
            val charUuid = cachedCustomCharacteristicUuid
            
            if (serviceUuid != null && charUuid != null) {
                val dataToSend = if (button.isHex) {
                    com.example.bluetoothremoteassistant.util.DataConverter.hexToBytes(button.data)
                } else {
                    button.data.toByteArray(Charsets.UTF_8)
                }
                
                if (dataToSend != null) {
                    bleManager.writeCharacteristic(serviceUuid, charUuid, dataToSend)
                } else {
                    // TODO: 提示数据格式错误
                }
            }
        }
    }

    /**
     * 更新自定义服务缓存
     * 当发现服务后，缓存自定义服务的UUID用于快速发送
     */
    fun updateCustomServiceCache() {
        viewModelScope.launch {
            val customServiceUuidStr = BluetoothLeManager.CUSTOM_SERVICE_UUID
            val customService = services.value.find { 
                it.uuid.toString().lowercase() == customServiceUuidStr.lowercase()
            }
            
            if (customService != null && customService.characteristics.isNotEmpty()) {
                // 使用第一个可写特征值
                val writableChar = customService.characteristics.find { 
                    it.isWritable
                }
                if (writableChar != null) {
                    cachedCustomServiceUuid = customService.uuid
                    cachedCustomCharacteristicUuid = writableChar.uuid
                }
            }
        }
    }

    /**
     * 发送遥控指令
     */
    fun sendControlCommand(command: ControlCommand) {
        viewModelScope.launch {
            val serviceUuid = cachedCustomServiceUuid
            val charUuid = cachedCustomCharacteristicUuid
            
            if (serviceUuid != null && charUuid != null) {
                bleManager.writeCharacteristic(serviceUuid, charUuid, command.toBytes())
            }
        }
    }

    /**
     * 发送停止指令
     */
    fun sendStopCommand() {
        sendControlCommand(ControlCommand.STOP)
    }

    /**
     * 清理资源
     */
    override fun onCleared() {
        super.onCleared()
        bleManager.cleanup()
    }
}
