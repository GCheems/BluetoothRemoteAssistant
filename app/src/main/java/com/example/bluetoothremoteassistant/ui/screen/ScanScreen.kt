package com.example.bluetoothremoteassistant.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluetoothremoteassistant.data.model.BleDevice
import com.example.bluetoothremoteassistant.ui.component.DeviceListItem

/**
 * 扫描页面
 * 显示扫描到的 BLE 设备列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    devices: List<BleDevice>,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (BleDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("蓝牙遥控助手") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (isScanning) {
                        onStopScan()
                    } else {
                        onStartScan()
                    }
                },
                icon = { Icon(Icons.Default.Search, contentDescription = "扫描") },
                text = { Text(if (isScanning) "停止扫描" else "开始扫描") }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 扫描状态提示
            if (isScanning) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 设备列表
            if (devices.isEmpty()) {
                // 空状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isScanning) "正在扫描..." else "点击按钮开始扫描设备",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 设备列表头部
                Text(
                    text = "发现 ${devices.size} 个设备",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(devices, key = { it.address }) { device ->
                        DeviceListItem(
                            device = device,
                            onClick = {
                                if (isScanning) {
                                    onStopScan()
                                }
                                onDeviceClick(device)
                            }
                        )
                    }
                }
            }
        }
    }
}
