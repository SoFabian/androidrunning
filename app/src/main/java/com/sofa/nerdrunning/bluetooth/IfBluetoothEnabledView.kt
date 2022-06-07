package com.sofa.nerdrunning.bluetooth

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

@Composable
fun IfBluetoothSupported(
    bluetoothAdapterStatus: BluetoothAdapterStatus,
    text: String,
    textStyle: TextStyle,
    content: @Composable () -> Unit,
) {
    when (bluetoothAdapterStatus) {
        BluetoothAdapterStatus.NotSupported -> Text(text, style = textStyle)
        else -> content()
    }
}
