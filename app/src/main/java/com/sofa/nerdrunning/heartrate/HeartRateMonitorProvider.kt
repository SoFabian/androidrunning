package com.sofa.nerdrunning.heartrate

import android.bluetooth.BluetoothManager
import com.sofa.nerdrunning.log.logDebug
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartRateMonitorProvider @Inject constructor(
    private val bluetoothManager: BluetoothManager
) {
    private val hrAddress = AtomicReference("")

    init {
        logDebug("HeartRateFlowProvider", "init")
    }

    fun setDevice(address: String) = hrAddress.set(address)

    private fun getDevice(): String = hrAddress.get()

    fun createMonitor(scope: CoroutineScope): HeartRateMonitor =
        if (hrAddress.get().isEmpty()) {
            ConstantHRMonitor(-1)
        } else {
            BluetoothHRMonitor(getDevice(), bluetoothManager, scope)
        }

}