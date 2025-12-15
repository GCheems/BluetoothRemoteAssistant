package com.example.bluetoothremoteassistant.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluetoothremoteassistant.data.model.ControlCommand

/**
 * 小车遥控面板
 * 提供方向控制和停止按钮
 */
@Composable
fun RemoteControlPanel(
    onCommandPress: (ControlCommand) -> Unit,
    onCommandRelease: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🚗 小车遥控",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 提示文字
            if (enabled) {
                Text(
                    text = "单击发送一次 • 长按持续控制",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "请先连接设备",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 方向控制布局
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 第一行：前进
                ControlButton(
                    label = ControlCommand.FORWARD.label,
                    icon = ControlCommand.FORWARD.icon,
                    onPress = { onCommandPress(ControlCommand.FORWARD) },
                    onRelease = onCommandRelease,
                    enabled = enabled
                )
                
                // 第二行：左转、停止、右转
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControlButton(
                        label = ControlCommand.TURN_LEFT.label,
                        icon = ControlCommand.TURN_LEFT.icon,
                        onPress = { onCommandPress(ControlCommand.TURN_LEFT) },
                        onRelease = onCommandRelease,
                        enabled = enabled
                    )
                    
                    ControlButton(
                        label = ControlCommand.STOP.label,
                        icon = ControlCommand.STOP.icon,
                        onPress = { onCommandPress(ControlCommand.STOP) },
                        onRelease = onCommandRelease,
                        enabled = enabled,
                        modifier = Modifier.size(100.dp)
                    )
                    
                    ControlButton(
                        label = ControlCommand.TURN_RIGHT.label,
                        icon = ControlCommand.TURN_RIGHT.icon,
                        onPress = { onCommandPress(ControlCommand.TURN_RIGHT) },
                        onRelease = onCommandRelease,
                        enabled = enabled
                    )
                }
                
                // 第三行：后退
                ControlButton(
                    label = ControlCommand.BACKWARD.label,
                    icon = ControlCommand.BACKWARD.icon,
                    onPress = { onCommandPress(ControlCommand.BACKWARD) },
                    onRelease = onCommandRelease,
                    enabled = enabled
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 说明文字
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💡 提示：松开按钮会自动发送停止指令",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
