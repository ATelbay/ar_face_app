package com.arystan.arface.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun PromptDialog(vm: ARViewModel) {
    val visible by vm.promptDialogVisible.collectAsState()
    val generating by vm.aiGenerating.collectAsState()
    val error by vm.aiError.collectAsState()

    if (!visible) return

    var text by remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = { if (!generating) vm.dismissPromptDialog() },
        title = { Text("AI-фильтр") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Опиши, как должна выглядеть маска")
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("например: космический кот") },
                    enabled = !generating,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 3,
                )
                if (generating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        Text("Генерирую…")
                    }
                }
                error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { vm.submitPrompt(text.text.trim()) },
                enabled = !generating && text.text.isNotBlank(),
            ) {
                Text("Сгенерировать")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { vm.dismissPromptDialog() },
                enabled = !generating,
            ) {
                Text("Отмена")
            }
        },
    )
}
