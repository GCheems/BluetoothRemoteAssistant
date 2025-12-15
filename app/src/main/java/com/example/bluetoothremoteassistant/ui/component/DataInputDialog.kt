package com.example.bluetoothremoteassistant.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bluetoothremoteassistant.util.DataConverter

/**
 * 数据输入对话框
 * 支持字符串和 Hex 两种输入模式
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (ByteArray) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var isHexMode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发送数据") },
        text = {
            Column {
                // 输入模式切换
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isHexMode,
                        onClick = { 
                            isHexMode = false
                            errorMessage = null
                        },
                        label = { Text("字符串") }
                    )
                    FilterChip(
                        selected = isHexMode,
                        onClick = { 
                            isHexMode = true
                            errorMessage = null
                        },
                        label = { Text("Hex") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 输入框
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { 
                        inputText = it
                        errorMessage = null
                    },
                    label = { Text(if (isHexMode) "Hex 数据" else "文本内容") },
                    placeholder = { 
                        Text(if (isHexMode) "例: 01 02 03 或 010203" else "输入要发送的文本")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isHexMode) KeyboardType.Text else KeyboardType.Text
                    ),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (isHexMode) {
                            Text("支持格式: 01 02 03 或 010203")
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (inputText.isEmpty()) {
                        errorMessage = "请输入数据"
                        return@TextButton
                    }

                    val bytes = if (isHexMode) {
                        val hexBytes = DataConverter.hexToBytes(inputText)
                        if (hexBytes == null) {
                            errorMessage = "无效的 Hex 格式"
                            return@TextButton
                        }
                        hexBytes
                    } else {
                        DataConverter.stringToBytes(inputText)
                    }

                    onConfirm(bytes)
                    onDismiss()
                }
            ) {
                Text("发送")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
