package com.example.bluetoothremoteassistant.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.bluetoothremoteassistant.data.model.CustomButton
import com.example.bluetoothremoteassistant.util.DataConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddButtonDialog(
    onDismiss: () -> Unit,
    onConfirm: (CustomButton) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var data by remember { mutableStateOf("") }
    var isHex by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "添加自定义按键",
                    style = MaterialTheme.typography.titleLarge
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("按键名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("数据格式:")
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(
                        selected = !isHex,
                        onClick = { isHex = false },
                        label = { Text("文本") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = isHex,
                        onClick = { isHex = true },
                        label = { Text("Hex") }
                    )
                }

                OutlinedTextField(
                    value = data,
                    onValueChange = { 
                        data = it
                        errorText = null
                    },
                    label = { Text(if (isHex) "Hex 数据 (例如: FF 01 AA)" else "发送内容") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = if (isHex) {
                        KeyboardOptions(keyboardType = KeyboardType.Ascii)
                    } else {
                        KeyboardOptions.Default
                    },
                    isError = errorText != null,
                    supportingText = {
                        if (errorText != null) {
                            Text(errorText!!)
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorText = "请输入按键名称"
                                return@Button
                            }
                            if (data.isBlank()) {
                                errorText = "请输入发送数据"
                                return@Button
                            }
                            if (isHex && !DataConverter.isValidHex(data)) {
                                errorText = "Hex 格式无效"
                                return@Button
                            }

                            onConfirm(CustomButton(
                                name = name,
                                data = data,
                                isHex = isHex
                            ))
                        }
                    ) {
                        Text("添加")
                    }
                }
            }
        }
    }
}
