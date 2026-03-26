package com.uwange.camera_filter_engine.ui.theme.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirmClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirmClick) { Text(confirmText) }
        }
    )
}