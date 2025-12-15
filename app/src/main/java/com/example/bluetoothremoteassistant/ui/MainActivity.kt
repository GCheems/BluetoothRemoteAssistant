package com.example.bluetoothremoteassistant.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bluetoothremoteassistant.data.model.BleDevice
import com.example.bluetoothremoteassistant.data.model.ConnectionState
import com.example.bluetoothremoteassistant.data.model.ControlCommand
import com.example.bluetoothremoteassistant.ui.screen.DeviceControlScreen
import com.example.bluetoothremoteassistant.ui.screen.ScanScreen
import com.example.bluetoothremoteassistant.ui.theme.BluetoothRemoteAssistantTheme
import com.example.bluetoothremoteassistant.viewmodel.MainViewModel
import com.google.accompanist.permissions.*

/**
 * 主 Activity
 */
class MainActivity : ComponentActivity() {

    // 蓝牙启用请求
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Toast.makeText(this, "需要开启蓝牙才能使用此功能", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            BluetoothRemoteAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BluetoothRemoteApp(
                        onRequestEnableBluetooth = {
                            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            enableBluetoothLauncher.launch(enableBtIntent)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 应用主组件
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BluetoothRemoteApp(
    onRequestEnableBluetooth: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()

    // 权限列表
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    // 权限状态
    val permissionsState = rememberMultiplePermissionsState(permissions)

    // 检查权限
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    // 显示权限请求或主界面
    when {
        permissionsState.allPermissionsGranted -> {
            MainNavigation(
                navController = navController,
                viewModel = viewModel,
                onRequestEnableBluetooth = onRequestEnableBluetooth
            )
        }
        else -> {
            PermissionRequestScreen(
                permissionsState = permissionsState
            )
        }
    }
}

/**
 * 权限请求页面
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(
    permissionsState: MultiplePermissionsState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "需要权限",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "此应用需要蓝牙和位置权限才能扫描和连接 BLE 设备",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(
                onClick = { permissionsState.launchMultiplePermissionRequest() }
            ) {
                Text("授予权限")
            }

            // 显示被拒绝的权限
            if (permissionsState.shouldShowRationale) {
                Text(
                    text = "蓝牙扫描需要位置权限。请在权限请求中允许所有权限。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 主导航
 */
@Composable
fun MainNavigation(
    navController: NavHostController,
    viewModel: MainViewModel,
    onRequestEnableBluetooth: () -> Unit
) {
    // 收集状态
    val devices by viewModel.scannedDevices.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val connectedDevice by viewModel.connectedDevice.collectAsState()
    val services by viewModel.services.collectAsState()
    val dataLogs by viewModel.dataLogs.collectAsState()

    var isScanning by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<BleDevice?>(null) }

    NavHost(
        navController = navController,
        startDestination = "scan"
    ) {
        // 扫描页面
        composable("scan") {
            ScanScreen(
                devices = devices,
                isScanning = isScanning,
                onStartScan = {
                    if (!viewModel.isBluetoothEnabled()) {
                        onRequestEnableBluetooth()
                    } else {
                        viewModel.startScan()
                        isScanning = true
                    }
                },
                onStopScan = {
                    viewModel.stopScan()
                    isScanning = false
                },
                onDeviceClick = { device ->
                    selectedDevice = device
                    viewModel.connectDevice(device)
                    navController.navigate("control")
                }
            )
        }

        // 控制页面
        composable("control") {
            val controlMode by viewModel.controlMode.collectAsState()
            val customButtons by viewModel.customButtons.collectAsState()
            
            // 当发现服务后，更新自定义服务缓存
            LaunchedEffect(services) {
                if (services.isNotEmpty()) {
                    viewModel.updateCustomServiceCache()
                }
            }
            
            DeviceControlScreen(
                device = selectedDevice ?: connectedDevice,
                connectionState = connectionState,
                services = services,
                dataLogs = dataLogs,
                controlMode = controlMode,
                customButtons = customButtons,
                onDisconnect = {
                    viewModel.disconnect()
                },
                onBack = {
                    if (connectionState == ConnectionState.CONNECTED) {
                        viewModel.disconnect()
                    }
                    navController.popBackStack()
                },
                onReadCharacteristic = { serviceUuid, charUuid ->
                    viewModel.readCharacteristic(serviceUuid, charUuid)
                },
                onWriteCharacteristic = { serviceUuid, charUuid, data ->
                    viewModel.writeCharacteristic(serviceUuid, charUuid, data)
                },
                onNotifyCharacteristic = { serviceUuid, charUuid, enable ->
                    viewModel.setCharacteristicNotification(serviceUuid, charUuid, enable)
                },
                onClearLogs = {
                    viewModel.clearLogs()
                },
                onSetControlMode = { mode ->
                    viewModel.setControlMode(mode)
                },
                onSwitchToCustomMode = {
                    viewModel.switchToCustomMode()
                },
                onSendControlCommand = { command: ControlCommand ->
                    viewModel.sendControlCommand(command)
                },
                onSendStopCommand = {
                    viewModel.sendStopCommand()
                },
                onAddCustomButton = { button ->
                    viewModel.addCustomButton(button)
                },
                onRemoveCustomButton = { button ->
                    viewModel.removeCustomButton(button)
                },
                onSendCustomButton = { button ->
                    viewModel.sendCustomButtonData(button)
                }
            )
        }
    }

    // 监听扫描状态
    LaunchedEffect(devices) {
        // 如果扫描中且有设备，保持扫描状态
    }
}
