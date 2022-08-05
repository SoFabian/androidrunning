package com.sofa.nerdrunning.liverun

import android.location.Location
import com.sofa.nerdrunning.heartrate.HeartRateMonitor
import com.sofa.nerdrunning.log.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch

class LiveRunFlow(
    locationFlow: Flow<Location>,
    private val heartRateMonitor: HeartRateMonitor,
    intervalRequestFlow: Flow<IntervalTarget>,
    intervalTargetReachedFlow: MutableSharedFlow<Int>,
    scope: CoroutineScope,
) {
    companion object {
        const val START_COUNTDOWN = 10
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private val tickerChannel = ticker(1000, 0)
    private val tickerFlow = tickerChannel.receiveAsFlow().withIndex().map { iv -> iv.index }

    private val _liveRun = MutableStateFlow(LiveRun())
    val flow = _liveRun.asStateFlow()

    private val liveRunComputationFlow = combineTransform(
        tickerFlow,
        locationFlow,
        heartRateMonitor.createFlow(),
        intervalRequestFlow.withIndex()
    ) { t, location, hr, intervalTarget ->
        val currentRun =
            if (t == START_COUNTDOWN && _liveRun.value.tick < START_COUNTDOWN) {
                LiveRun()
            } else {
                _liveRun.value
            }
        val currentIntervalsSize = currentRun.intervals.size
        val nextRun =
            if (t < START_COUNTDOWN) {
                LiveRun(tick = t)
            } else {
                currentRun.addLocation(t, location, hr, intervalTarget)
            }
        if (currentIntervalsSize != nextRun.intervals.size) {
            intervalTargetReachedFlow.tryEmit(nextRun.intervals.size)
        }
        if (currentRun.tick != nextRun.tick) {
            emit(nextRun)
        }
    }

    init {
        logDebug("LiveRunFlow", "init")
        scope.launch {
            liveRunComputationFlow.collect {
                _liveRun.value = it
            }
        }
    }

    fun stop(): LiveRun {
        tickerChannel.cancel()
        heartRateMonitor.cancel()
        return _liveRun.value
    }

}