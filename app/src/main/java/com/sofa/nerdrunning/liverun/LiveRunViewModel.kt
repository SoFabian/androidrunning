package com.sofa.nerdrunning.liverun

import android.util.Log
import androidx.lifecycle.ViewModel
import com.sofa.nerdrunning.finishedrun.FinishedRun
import com.sofa.nerdrunning.log.logDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

@HiltViewModel
class LiveRunViewModel @Inject constructor(
    private val liveRunService: LiveRunServiceProxy,
) : ViewModel() {

    fun startLiveRun() = liveRunService.start()
    fun finishLiveRun() = liveRunService.finish { _finishedRunFlow.value = it}
    fun cancelLiveRun() = liveRunService.cancel()
    fun unbindLiveRun() = liveRunService.unbind()

    private var liveRunFlow: AtomicReference<Flow<LiveRun>> = AtomicReference(null)
    val liveRunServiceBoundFlow = liveRunService.boundFlow

    private val _finishedRunFlow = MutableStateFlow<FinishedRun?>(null)
    val finishedRunFlow = _finishedRunFlow.asStateFlow()

    fun liveRunFlow(): Flow<LiveRun> {
        if (liveRunFlow.get() == null) {
            if (liveRunServiceBoundFlow.value) {
                liveRunFlow.compareAndSet(null, liveRunService.liveRunFlow())
            } else {
                Log.w("LiveRunViewModel", "call to liveRunFlow() although not bound")
                return flowOf(LiveRun())
            }
        }
        return liveRunFlow.get()
    }

    init {
        logDebug("LiveRunViewModel", "init")
    }

    override fun onCleared() {
        super.onCleared()
        logDebug("LiveRunViewModel", "onCleared")
    }

    fun intervalRequest(nextIntervalTarget: IntervalTarget) {
        liveRunService.intervalRequest(nextIntervalTarget)
    }

}
