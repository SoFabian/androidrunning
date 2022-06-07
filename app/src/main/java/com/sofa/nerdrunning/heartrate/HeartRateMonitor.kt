package com.sofa.nerdrunning.heartrate

import android.bluetooth.BluetoothManager
import android.util.Log
import com.juul.kable.ConnectionLostException
import com.juul.kable.State
import com.juul.kable.characteristicOf
import com.juul.kable.peripheral
import com.sofa.nerdrunning.log.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.experimental.and
import kotlin.math.pow

const val HEART_RATE_SERVICE = "0000180d-0000-1000-8000-00805f9b34fb"
const val HEART_RATE_MEASUREMENT_CHARACTERISTIC = "00002a37-0000-1000-8000-00805f9b34fb"

val heartRateCharacteristic = characteristicOf(
    service = HEART_RATE_SERVICE,
    characteristic = HEART_RATE_MEASUREMENT_CHARACTERISTIC,
)

sealed interface HeartRateMonitor {
    fun createFlow(): Flow<Int>
    fun cancel() {}
}

class ConstantHRMonitor(private val value: Int = 75) : HeartRateMonitor {
    override fun createFlow() = flowOf(value)
}

class BluetoothHRMonitor(
    macAddress: String,
    bluetoothManager: BluetoothManager,
    private val scope: CoroutineScope,
) : HeartRateMonitor {

    private val peripheral by lazy {
        scope.peripheral(
            bluetoothManager.adapter.getRemoteDevice(macAddress)
        )
    }

    private val connectionAttempt = AtomicInteger()

    override fun createFlow(): Flow<Int> {
        if (connectionAttempt.get() <= 0) {
            scope.enableAutoReconnect()
            scope.connect()
        }
        return peripheral.observe(heartRateCharacteristic)
            .catch { cause -> Log.e("BluetoothHRMonitor", "Error observing HR", cause) }
            .map { data -> extractHR(data) }
            .onEach { logDebug("HeartRateMonitor", "on heart rate $it") }
    }

    private fun CoroutineScope.enableAutoReconnect() {
        peripheral.state
            .filter { it is State.Disconnected }
            .onEach {
                val timeMillis = backoff(retry = connectionAttempt.getAndIncrement())
                Log.i("HRViewModel", "Waiting $timeMillis ms to reconnect...")
                delay(timeMillis)
                connect()
            }
            .launchIn(this)
    }

    private fun CoroutineScope.connect() {
        connectionAttempt.incrementAndGet()
        launch {
            logDebug("HRViewModel", "connect")
            try {
                peripheral.connect()
                connectionAttempt.set(0)
            } catch (e: ConnectionLostException) {
                Log.w("HRViewModel", "Connection attempt failed", e)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun cancel() {
        GlobalScope.launch {
            withTimeoutOrNull(disconnectTimeout) {
                peripheral.disconnect()
            }
        }
    }

    companion object {
        private val disconnectTimeout = TimeUnit.SECONDS.toMillis(5)
    }
}

private fun extractHR(data: ByteArray): Int =
    when {
        data.size <= 2 -> {
            0
        }
        isUInt8(data) -> {
            data[1].toInt()
        }
        else -> {
            convertToInt(data[2], data[1])
        }
    }

private fun isUInt8(hr: ByteArray): Boolean = hr[0].and(1).toInt() == 0

private fun convertToInt(msb: Byte, lsb: Byte): Int =
    msb.toInt().and(0xff).shl(8).or(lsb.toInt().and(0xff))

/**
 * Exponential backoff using the following formula:
 *
 * ```
 * delay = base * multiplier ^ retry
 * ```
 *
 * For example (using `base = 100` and `multiplier = 2`):
 *
 * | retry | delay |
 * |-------|-------|
 * |   1   |   100 |
 * |   2   |   200 |
 * |   3   |   400 |
 * |   4   |   800 |
 * |   5   |  1600 |
 * |  ...  |   ... |
 *
 * Inspired by:
 * [Exponential Backoff And Jitter](https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/)
 *
 * @return Backoff delay (in units matching [base] units, e.g. if [base] units are milliseconds then returned delay will be milliseconds).
 */
private fun backoff(
    base: Long = 100,
    multiplier: Float = 2f,
    retry: Int,
): Long = (base * multiplier.pow(retry - 1)).toLong()
