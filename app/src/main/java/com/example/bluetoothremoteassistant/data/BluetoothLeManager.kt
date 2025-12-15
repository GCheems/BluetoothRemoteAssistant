package com.example.bluetoothremoteassistant.data

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.util.Log
import com.example.bluetoothremoteassistant.data.model.BleCharacteristic
import com.example.bluetoothremoteassistant.data.model.BleDevice
import com.example.bluetoothremoteassistant.data.model.BleService
import com.example.bluetoothremoteassistant.data.model.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * 蓝牙 LE 管理器 - 单例类
 * 负责蓝牙的扫描、连接、数据读写等核心功能
 */
class BluetoothLeManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothLeManager"
        private const val CLIENT_CHARACTERISTIC_CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb"
        
        // 自定义服务 UUID（ESP32常用）
        const val CUSTOM_SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        
        @Volatile
        private var instance: BluetoothLeManager? = null

        /**
         * 获取单例实例
         */
        fun getInstance(context: Context): BluetoothLeManager {
            return instance ?: synchronized(this) {
                instance ?: BluetoothLeManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // 蓝牙适配器
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    // 蓝牙扫描器
    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }

    // GATT 连接对象
    private var bluetoothGatt: BluetoothGatt? = null

    // 扫描到的设备列表
    private val _scannedDevices = MutableStateFlow<List<BleDevice>>(emptyList())
    val scannedDevices: StateFlow<List<BleDevice>> = _scannedDevices.asStateFlow()

    // 连接状态
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // 当前连接的设备
    private val _connectedDevice = MutableStateFlow<BleDevice?>(null)
    val connectedDevice: StateFlow<BleDevice?> = _connectedDevice.asStateFlow()

    // 服务列表
    private val _services = MutableStateFlow<List<BleService>>(emptyList())
    val services: StateFlow<List<BleService>> = _services.asStateFlow()

    // 接收到的数据日志
    private val _dataLogs = MutableStateFlow<List<String>>(emptyList())
    val dataLogs: StateFlow<List<String>> = _dataLogs.asStateFlow()

    // 是否正在扫描
    private var isScanning = false

    /**
     * 检查蓝牙是否可用
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * 扫描回调
     */
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            
            val device = BleDevice(
                name = result.device.name ?: "",
                address = result.device.address,
                rssi = result.rssi
            )

            // 更新设备列表（去重）
            val currentList = _scannedDevices.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.address == device.address }
            
            if (existingIndex >= 0) {
                // 更新已存在的设备（更新 RSSI）
                currentList[existingIndex] = device
            } else {
                // 添加新设备
                currentList.add(device)
            }
            
            _scannedDevices.value = currentList
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "扫描失败，错误码: $errorCode")
            addLog("扫描失败，错误码: $errorCode")
            isScanning = false
        }
    }

    /**
     * GATT 回调
     */
    private val gattCallback = object : BluetoothGattCallback() {
        
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "已连接到 GATT 服务器")
                    _connectionState.value = ConnectionState.CONNECTED
                    addLog("设备连接成功")
                    
                    // 发现服务
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "已从 GATT 服务器断开")
                    _connectionState.value = ConnectionState.DISCONNECTED
                    _services.value = emptyList()
                    addLog("设备已断开")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "服务发现成功")
                
                // 解析服务和特征值
                val servicesList = gatt.services.map { service ->
                    val characteristics = service.characteristics.map { char ->
                        BleCharacteristic(
                            uuid = char.uuid,
                            properties = char.properties
                        )
                    }
                    
                    BleService(
                        uuid = service.uuid,
                        characteristics = characteristics
                    )
                }
                
                _services.value = servicesList
                addLog("发现 ${servicesList.size} 个服务")
            } else {
                Log.e(TAG, "服务发现失败: $status")
                addLog("服务发现失败")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    characteristic.value
                } else {
                    @Suppress("DEPRECATION")
                    characteristic.value
                }
                
                val hexString = value?.joinToString(" ") { "%02X".format(it) } ?: "null"
                addLog("读取 ${characteristic.uuid}: $hexString")
            } else {
                addLog("读取失败: ${characteristic.uuid}")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            
            if (status == BluetoothGatt.GATT_SUCCESS) {
                addLog("写入成功: ${characteristic.uuid}")
            } else {
                addLog("写入失败: ${characteristic.uuid}")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            @Suppress("DEPRECATION")
            handleCharacteristicChange(characteristic, characteristic.value)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            handleCharacteristicChange(characteristic, value)
        }

        private fun handleCharacteristicChange(
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray?
        ) {
            val hexString = value?.joinToString(" ") { "%02X".format(it) } ?: "null"
            val asciiString = value?.let { String(it, Charsets.UTF_8) } ?: "null"
            addLog("通知 ${characteristic.uuid.toString().substring(0, 8)}: HEX[$hexString] ASCII[$asciiString]")
        }
    }

    /**
     * 检查权限
     */
    private fun checkPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /**
     * 开始扫描 BLE 设备
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        if (isScanning) {
            Log.w(TAG, "正在扫描中")
            return
        }
        
        if (!isBluetoothEnabled()) {
            Log.e(TAG, "蓝牙未开启")
            addLog("蓝牙未开启，请先开启蓝牙")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !checkPermission(android.Manifest.permission.BLUETOOTH_SCAN)) {
            Log.e(TAG, "无 BLUETOOTH_SCAN 权限")
            addLog("无扫描权限")
            return
        }

        // 清空设备列表
        _scannedDevices.value = emptyList()
        
        // 配置扫描设置
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)
        isScanning = true
        addLog("开始扫描 BLE 设备...")
        Log.d(TAG, "开始扫描")
    }

    /**
     * 停止扫描
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!isScanning) return
        
        bluetoothLeScanner?.stopScan(scanCallback)
        isScanning = false
        addLog("停止扫描")
        Log.d(TAG, "停止扫描")
    }

    /**
     * 连接设备
     */
    @SuppressLint("MissingPermission")
    fun connect(device: BleDevice) {
        // 先停止扫描
        if (isScanning) {
            stopScan()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !checkPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) {
            Log.e(TAG, "无 BLUETOOTH_CONNECT 权限")
            addLog("无连接权限")
            return
        }

        // 断开已有连接
        disconnect()

        _connectionState.value = ConnectionState.CONNECTING
        _connectedDevice.value = device
        addLog("正在连接 ${device.displayName}...")

        val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address)
        bluetoothGatt = bluetoothDevice?.connectGatt(context, false, gattCallback)
    }

    /**
     * 断开连接
     */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.let { gatt ->
            gatt.disconnect()
            gatt.close()
            bluetoothGatt = null
        }
        _connectionState.value = ConnectionState.DISCONNECTED
        _connectedDevice.value = null
        _services.value = emptyList()
    }

    /**
     * 读取特征值
     */
    @SuppressLint("MissingPermission")
    fun readCharacteristic(serviceUuid: UUID, characteristicUuid: UUID): Boolean {
        val characteristic = bluetoothGatt?.getService(serviceUuid)
            ?.getCharacteristic(characteristicUuid) ?: return false
        
        return bluetoothGatt?.readCharacteristic(characteristic) == true
    }

    /**
     * 写入特征值
     */
    @SuppressLint("MissingPermission")
    fun writeCharacteristic(serviceUuid: UUID, characteristicUuid: UUID, data: ByteArray): Boolean {
        val characteristic = bluetoothGatt?.getService(serviceUuid)
            ?.getCharacteristic(characteristicUuid) ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bluetoothGatt?.writeCharacteristic(
                characteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            ) == BluetoothGatt.GATT_SUCCESS
        } else {
            @Suppress("DEPRECATION")
            characteristic.value = data
            @Suppress("DEPRECATION")
            bluetoothGatt?.writeCharacteristic(characteristic) == true
        }
    }

    /**
     * 启用/禁用通知
     */
    @SuppressLint("MissingPermission")
    fun setCharacteristicNotification(
        serviceUuid: UUID,
        characteristicUuid: UUID,
        enable: Boolean
    ): Boolean {
        val characteristic = bluetoothGatt?.getService(serviceUuid)
            ?.getCharacteristic(characteristicUuid) ?: return false

        // 启用本地通知
        bluetoothGatt?.setCharacteristicNotification(characteristic, enable) ?: return false

        // 写入描述符以启用远程通知
        val descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG_UUID))
        if (descriptor != null) {
            val value = if (enable) {
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            } else {
                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            }

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bluetoothGatt?.writeDescriptor(descriptor, value) == BluetoothGatt.GATT_SUCCESS
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = value
                @Suppress("DEPRECATION")
                bluetoothGatt?.writeDescriptor(descriptor) == true
            }
        }

        return false
    }

    /**
     * 添加日志
     */
    private fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "[$timestamp] $message"
        _dataLogs.value = _dataLogs.value + logMessage
    }

    /**
     * 清空日志
     */
    fun clearLogs() {
        _dataLogs.value = emptyList()
    }

    /**
     * 清理资源
     */
    fun cleanup() {
        stopScan()
        disconnect()
    }
}
