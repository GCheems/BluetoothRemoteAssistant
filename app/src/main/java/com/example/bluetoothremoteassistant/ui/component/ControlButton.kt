package com.example.bluetoothremoteassistant.ui.component

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 自定义遥控按钮组件
 * 支持单击发送一次和长按持续发送
 */
@Composable
fun ControlButton(
    label: String,
    icon: String,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var repeatJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    Card(
        modifier = modifier
            .size(100.dp)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            // 按下时
                            isPressed = true
                            onPress() // 立即发送一次
                            
                            // 延迟后开始重复发送
                            repeatJob = scope.launch {
                                delay(500) // 延迟500ms后开始重复
                                while (isPressed) {
                                    onPress()
                                    delay(150) // 每150ms发送一次
                                }
                            }
                            
                            // 等待释放
                            tryAwaitRelease()
                            
                            // 释放时
                            isPressed = false
                            repeatJob?.cancel()
                            onRelease()
                        }
                    )
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                isPressed -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 1.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPressed) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isPressed) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
        }
    }
}
