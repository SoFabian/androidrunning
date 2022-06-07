package com.sofa.nerdrunning.confirmation

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Text(text)
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Yes", color = MaterialTheme.colors.onPrimary)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("No", color = MaterialTheme.colors.onPrimary)
            }
        }
    )
}