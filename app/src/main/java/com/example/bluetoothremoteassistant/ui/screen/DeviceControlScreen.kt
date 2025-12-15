package com.example.bluetoothremoteassistant.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluetoothremoteassistant.data.BluetoothLeManager
import com.example.bluetoothremoteassistant.data.model.BleDevice
import com.example.bluetoothremoteassistant.data.model.BleService
import com.example.bluetoothremoteassistant.data.model.ConnectionState
import com.example.bluetoothremoteassistant.data.model.ControlCommand
import com.example.bluetoothremoteassistant.ui.component.CharacteristicItem
import com.example.bluetoothremoteassistant.ui.component.DataInputDialog
import com.example.bluetoothremoteassistant.ui.component.RemoteControlPanel
import com.example.bluetoothremoteassistant.ui.theme.ConnectedGreen
import com.example.bluetoothremoteassistant.ui.theme.DisconnectedGray
import java.util.UUID

/**
 * 设备控制页面
 * 显示连接状态、服务/特征值列表、数据日志
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceControlScreen(
    device: BleDevice?,
    connectionState: ConnectionState,
    services: List<BleService>,
    dataLogs: List<String>,
    controlMode: com.example.bluetoothremoteassistant.data.model.ControlMode,
    customButtons: List<com.example.bluetoothremoteassistant.data.model.CustomButton>,
    onDisconnect: () -> Unit,
    onBack: () -> Unit,
    onReadCharacteristic: (UUID, UUID) -> Unit,
    onWriteCharacteristic: (UUID, UUID, ByteArray) -> Unit,
    onNotifyCharacteristic: (UUID, UUID, Boolean) -> Unit,
    onClearLogs: () -> Unit,
    onSetControlMode: (com.example.bluetoothremoteassistant.data.model.ControlMode) -> Unit,
    onSwitchToCustomMode: () -> Unit,
    onSendControlCommand: (ControlCommand) -> Unit,
    onSendStopCommand: () -> Unit,
    onAddCustomButton: (com.example.bluetoothremoteassistant.data.model.CustomButton) -> Unit,
    onRemoveCustomButton: (com.example.bluetoothremoteassistant.data.model.CustomButton) -> Unit,
    onSendCustomButton: (com.example.bluetoothremoteassistant.data.model.CustomButton) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDataInputDialog by remember { mutableStateOf(false) }
    var showAddButtonDialog by remember { mutableStateOf(false) }
    var selectedServiceUuid by remember { mutableStateOf<UUID?>(null) }
    var selectedCharUuid by remember { mutableStateOf<UUID?>(null) }
    var expandedServiceIndex by remember { mutableStateOf<Int?>(null) }
    var isLogExpanded by remember { mutableStateOf(false) }
    var isOtherServicesExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(device?.displayName ?: "设备控制")
                        Text(
                            text = device?.address ?: "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 添加按键按钮（连接状态下始终显示）
                    if (connectionState == ConnectionState.CONNECTED) {
                        IconButton(onClick = {
                            onSwitchToCustomMode()
                            showAddButtonDialog = true
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "添加按键")
                        }
                    }

                    // 断开按钮
                    if (connectionState == ConnectionState.CONNECTED) {
                        IconButton(onClick = onDisconnect) {
                            Icon(Icons.Default.BluetoothDisabled, contentDescription = "断开")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 模式切换标签栏（仅连接时显示）
            if (connectionState == ConnectionState.CONNECTED) {
                TabRow(
                    selectedTabIndex = controlMode.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    com.example.bluetoothremoteassistant.data.model.ControlMode.values().forEach { mode ->
                        Tab(
                            selected = controlMode == mode,
                            onClick = { onSetControlMode(mode) },
                            text = {
                                Text(
                                    text = when (mode) {
                                        com.example.bluetoothremoteassistant.data.model.ControlMode.STANDARD -> "列表"
                                        com.example.bluetoothremoteassistant.data.model.ControlMode.GAMEPAD -> "遥控"
                                        com.example.bluetoothremoteassistant.data.model.ControlMode.CUSTOM -> "自定义"
                                    }
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = when (mode) {
                                        com.example.bluetoothremoteassistant.data.model.ControlMode.STANDARD -> Icons.Default.List
                                        com.example.bluetoothremoteassistant.data.model.ControlMode.GAMEPAD -> Icons.Default.Gamepad
                                        com.example.bluetoothremoteassistant.data.model.ControlMode.CUSTOM -> Icons.Default.GridView
                                    },
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }

            // 连接状态卡片
            ConnectionStatusCard(
                connectionState = connectionState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // 根据模式显示不同内容
            if (connectionState == ConnectionState.CONNECTED) {
                when (controlMode) {
                    com.example.bluetoothremoteassistant.data.model.ControlMode.GAMEPAD -> {
                        // 遥控模式：显示遥控面板
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            RemoteControlPanel(
                                onCommandPress = onSendControlCommand,
                                onCommandRelease = onSendStopCommand,
                                enabled = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    com.example.bluetoothremoteassistant.data.model.ControlMode.CUSTOM -> {
                        // 自定义模式：显示自定义按键面板
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            com.example.bluetoothremoteassistant.ui.component.CustomButtonPanel(
                                buttons = customButtons,
                                onButtonPress = onSendCustomButton,
                                onButtonRemove = onRemoveCustomButton,
                                onAddButtonClick = { showAddButtonDialog = true },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    com.example.bluetoothremoteassistant.data.model.ControlMode.STANDARD -> {
                        // 标准模式：显示服务列表
                        if (services.isNotEmpty()) {
                            // 分离自定义服务和其他服务
                            val customService = services.find {
                                it.uuid.toString().lowercase() == BluetoothLeManager.CUSTOM_SERVICE_UUID.lowercase()
                            }
                            val otherServices = services.filter {
                                it.uuid.toString().lowercase() != BluetoothLeManager.CUSTOM_SERVICE_UUID.lowercase()
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                // 自定义服务（突出显示）
                                if (customService != null) {
                                    item {
                                        Text(
                                            text = "⭐ 自定义服务（主功能）",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                    item {
                                        CustomServiceSection(
                                            service = customService,
                                            onReadCharacteristic = { charUuid ->
                                                onReadCharacteristic(customService.uuid, charUuid)
                                            },
                                            onWriteCharacteristic = { charUuid ->
                                                selectedServiceUuid = customService.uuid
                                                selectedCharUuid = charUuid
                                                showDataInputDialog = true
                                            },
                                            onNotifyCharacteristic = { charUuid, enable ->
                                                onNotifyCharacteristic(customService.uuid, charUuid, enable)
                                            }
                                        )
                                    }

                                    item {
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }

                                // 其他服务（可折叠）
                                if (otherServices.isNotEmpty()) {
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                        ) {
                                            Surface(
                                                onClick = { isOtherServicesExpanded = !isOtherServicesExpanded },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = "其他服务",
                                                            style = MaterialTheme.typography.titleSmall
                                                        )
                                                        Text(
                                                            text = "${otherServices.size} 个标准服务",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = if (isOtherServicesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                        contentDescription = if (isOtherServicesExpanded) "收起" else "展开"
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (isOtherServicesExpanded) {
                                        items(otherServices.size) { index ->
                                            val service = otherServices[index]
                                            ServiceSection(
                                                service = service,
                                                isExpanded = expandedServiceIndex == index,
                                                onExpandToggle = {
                                                    expandedServiceIndex = if (expandedServiceIndex == index) null else index
                                                },
                                                onReadCharacteristic = { charUuid ->
                                                    onReadCharacteristic(service.uuid, charUuid)
                                                },
                                                onWriteCharacteristic = { charUuid ->
                                                    selectedServiceUuid = service.uuid
                                                    selectedCharUuid = charUuid
                                                    showDataInputDialog = true
                                                },
                                                onNotifyCharacteristic = { charUuid, enable ->
                                                    onNotifyCharacteristic(service.uuid, charUuid, enable)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Divider()

                // 日志区域（所有模式都显示）
                LogSection(
                    logs = dataLogs,
                    isExpanded = isLogExpanded,
                    onToggleExpand = { isLogExpanded = !isLogExpanded },
                    onClearLogs = onClearLogs,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // 数据输入对话框
    if (showDataInputDialog && selectedServiceUuid != null && selectedCharUuid != null) {
        DataInputDialog(
            onDismiss = {
                showDataInputDialog = false
                selectedServiceUuid = null
                selectedCharUuid = null
            },
            onConfirm = { data ->
                onWriteCharacteristic(selectedServiceUuid!!, selectedCharUuid!!, data)
            }
        )
    }

    // 添加按键对话框
    if (showAddButtonDialog) {
        com.example.bluetoothremoteassistant.ui.component.AddButtonDialog(
            onDismiss = { showAddButtonDialog = false },
            onConfirm = { button ->
                onAddCustomButton(button)
                showAddButtonDialog = false
            }
        )
    }
}

/**
 * 连接状态卡片
 */
@Composable
private fun ConnectionStatusCard(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = when (connectionState) {
                ConnectionState.CONNECTED -> ConnectedGreen.copy(alpha = 0.1f)
                else -> DisconnectedGray.copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (connectionState) {
                    ConnectionState.CONNECTED -> Icons.Default.CheckCircle
                    ConnectionState.CONNECTING -> Icons.Default.HourglassEmpty
                    else -> Icons.Default.Cancel
                },
                contentDescription = null,
                tint = when (connectionState) {
                    ConnectionState.CONNECTED -> ConnectedGreen
                    else -> DisconnectedGray
                },
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = when (connectionState) {
                        ConnectionState.CONNECTED -> "已连接"
                        ConnectionState.CONNECTING -> "连接中..."
                        ConnectionState.DISCONNECTING -> "断开中..."
                        ConnectionState.DISCONNECTED -> "已断开"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                if (connectionState == ConnectionState.CONNECTED) {
                    Text(
                        text = "可以开始操作特征值",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 服务区块（可展开）
 */
@Composable
private fun ServiceSection(
    service: BleService,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onReadCharacteristic: (UUID) -> Unit,
    onWriteCharacteristic: (UUID) -> Unit,
    onNotifyCharacteristic: (UUID, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column {
            // 服务头部
            Surface(
                onClick = onExpandToggle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = service.serviceName,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "UUID: ${service.shortUuid}...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${service.characteristics.size} 个特征值",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收起" else "展开"
                    )
                }
            }

            // 特征值列表
            if (isExpanded) {
                Divider()
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    service.characteristics.forEach { characteristic ->
                        CharacteristicItem(
                            serviceUuid = service.uuid,
                            characteristic = characteristic,
                            onRead = { onReadCharacteristic(characteristic.uuid) },
                            onWrite = { onWriteCharacteristic(characteristic.uuid) },
                            onNotify = { enable -> onNotifyCharacteristic(characteristic.uuid, enable) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 日志区域
 */
@Composable
private fun LogSection(
    logs: List<String>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayLogs = if (isExpanded) logs else logs.takeLast(3)
    val logHeight = if (isExpanded) 400.dp else 180.dp

    Card(
        modifier = modifier
            .height(logHeight)
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 日志头部
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "数据日志 (${logs.size})",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "收起" else "展开",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                TextButton(onClick = onClearLogs) {
                    Text("清空")
                }
            }

            Divider()

            // 日志内容
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                reverseLayout = true
            ) {
                items(displayLogs.reversed()) { log ->
                    Text(
                        text = log,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * 自定义服务区块（突出显示）
 */
@Composable
private fun CustomServiceSection(
    service: BleService,
    onReadCharacteristic: (UUID) -> Unit,
    onWriteCharacteristic: (UUID) -> Unit,
    onNotifyCharacteristic: (UUID, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 服务头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🎯 ${service.serviceName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "UUID: ${service.shortUuid}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${service.characteristics.size} 个特征值",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // 特征值列表（始终展开）
            service.characteristics.forEach { characteristic ->
                CharacteristicItem(
                    serviceUuid = service.uuid,
                    characteristic = characteristic,
                    onRead = { onReadCharacteristic(characteristic.uuid) },
                    onWrite = { onWriteCharacteristic(characteristic.uuid) },
                    onNotify = { enable -> onNotifyCharacteristic(characteristic.uuid, enable) }
                )
            }
        }
    }
}
