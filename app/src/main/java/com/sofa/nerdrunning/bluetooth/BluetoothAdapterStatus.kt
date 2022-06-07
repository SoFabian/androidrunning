package com.sofa.nerdrunning.bluetooth

import android.bluetooth.BluetoothManager

enum class BluetoothAdapterStatus {
    NotSupported,
    NotEnabled,
    Ready,
}

fun BluetoothManager.adapterStatus() =
    if (adapter == null) {
        BluetoothAdapterStatus.NotSupported
    } else if (!adapter.isEnabled) {
        BluetoothAdapterStatus.NotEnabled
    } else {
        BluetoothAdapterStatus.Ready
    }
