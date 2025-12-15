package com.example.bluetoothremoteassistant.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluetoothremoteassistant.data.model.BleDevice
import com.example.bluetoothremoteassistant.ui.theme.SignalStrong
import com.example.bluetoothremoteassistant.ui.theme.SignalMedium
import com.example.bluetoothremoteassistant.ui.theme.SignalWeak

/**
 * 设备列表项组件
 */
@Composable
fun DeviceListItem(
    device: BleDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 蓝牙图标
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = "蓝牙设备",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 设备信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = device.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // RSSI 信号强度
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${device.rssi} dBm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = getRssiColor(device.rssi)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getRssiLabel(device.rssi),
                    style = MaterialTheme.typography.bodySmall,
                    color = getRssiColor(device.rssi)
                )
            }
        }
    }
}

/**
 * 根据 RSSI 值获取颜色
 */
@Composable
private fun getRssiColor(rssi: Int) = when {
    rssi >= -60 -> SignalStrong
    rssi >= -80 -> SignalMedium
    else -> SignalWeak
}

/**
 * 根据 RSSI 值获取标签
 */
private fun getRssiLabel(rssi: Int) = when {
    rssi >= -60 -> "强"
    rssi >= -80 -> "中"
    else -> "弱"
}
