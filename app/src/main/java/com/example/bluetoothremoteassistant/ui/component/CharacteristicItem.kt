package com.example.bluetoothremoteassistant.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluetoothremoteassistant.data.model.BleCharacteristic
import java.util.UUID

/**
 * 特征值列表项组件
 */
@Composable
fun CharacteristicItem(
    serviceUuid: UUID,
    characteristic: BleCharacteristic,
    onRead: () -> Unit,
    onWrite: () -> Unit,
    onNotify: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var notifyEnabled by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // UUID 和属性
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "UUID: ${characteristic.shortUuid}...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "属性: ${characteristic.propertiesText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 读取按钮
                if (characteristic.isReadable) {
                    FilledTonalButton(
                        onClick = onRead,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "读取",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("读取")
                    }
                }

                // 写入按钮
                if (characteristic.isWritable) {
                    FilledTonalButton(
                        onClick = onWrite,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "写入",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("写入")
                    }
                }

                // 通知开关
                if (characteristic.isNotifiable || characteristic.isIndicatable) {
                    FilledTonalButton(
                        onClick = {
                            notifyEnabled = !notifyEnabled
                            onNotify(notifyEnabled)
                        },
                        modifier = Modifier.weight(1f),
                        colors = if (notifyEnabled) {
                            ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            ButtonDefaults.filledTonalButtonColors()
                        }
                    ) {
                        Icon(
                            imageVector = if (notifyEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = "通知",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (notifyEnabled) "已启用" else "通知")
                    }
                }
            }
        }
    }
}
