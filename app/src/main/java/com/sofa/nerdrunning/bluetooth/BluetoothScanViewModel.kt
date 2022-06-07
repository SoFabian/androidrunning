package com.sofa.nerdrunning.bluetooth

import android.bluetooth.BluetoothManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.Uuid
import com.juul.kable.Advertisement
import com.juul.kable.Scanner
import com.sofa.nerdrunning.bluetooth.ScanStatus.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val SCAN_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(10)

sealed class ScanStatus {
    object Stopped : ScanStatus()
    object Started : ScanStatus()
    data class Failed(val message: CharSequence) : ScanStatus()
}

@HiltViewModel
class BluetoothScanViewModel @Inject constructor(
    private val bluetoothManager: BluetoothManager,
) : ViewModel() {

    private val scanner by lazy { Scanner() }
    private val scanScope = viewModelScope.childScope()
    private val found = hashMapOf<String, Advertisement>()

    private val _scanStatus = MutableStateFlow<ScanStatus>(Stopped)

    private val _advertisements = MutableStateFlow<List<Advertisement>>(emptyList())
    val advertisements = _advertisements.asStateFlow()

    fun bluetoothAdapterStatus(): BluetoothAdapterStatus = bluetoothManager.adapterStatus()

    fun startScan(uuid: Uuid) {
        if (_scanStatus.value == Started) return
        _scanStatus.value = Started

        scanScope.launch {
            withTimeoutOrNull(SCAN_DURATION_MILLIS) {
                scanner
                    .advertisements
                    .catch { cause ->
                        _scanStatus.value = Failed(cause.message ?: "Unknown error")
                    }
                    .onCompletion { cause ->
                        if (cause == null) _scanStatus.value = Stopped
                    }
                    .filter { ad -> ad.uuids.any { uuid == it } }
                    .collect { advertisement ->
                        found[advertisement.address] = advertisement
                        _advertisements.value = found.values.toList()
                    }
            }
        }
    }

    private fun stopScan() {
        scanScope.cancelChildren()
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }
}

private fun CoroutineScope.childScope() =
    CoroutineScope(coroutineContext + Job(coroutineContext[Job]))

private fun CoroutineScope.cancelChildren(
    cause: CancellationException? = null
) = coroutineContext[Job]?.cancelChildren(cause)
