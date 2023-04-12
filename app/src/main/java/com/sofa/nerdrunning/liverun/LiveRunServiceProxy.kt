package com.sofa.nerdrunning.liverun

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.sofa.nerdrunning.finishedrun.FinishedRun
import com.sofa.nerdrunning.log.logDebug
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class LiveRunServiceProxy @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private lateinit var liveRunService: LiveRunService

    private val _boundFlow = MutableStateFlow(false)
    val boundFlow = _boundFlow.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            logDebug("LiveRunServiceProxy", "onServiceConnected")
            val liveRunBinder = binder as LiveRunService.LiveRunBinder
            liveRunService = liveRunBinder.getService()
            _boundFlow.value = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            logDebug("LiveRunServiceProxy", "onServiceDisconnected")
            _boundFlow.value = false
        }
    }

    fun start() {
        logDebug("LiveRunServiceProxy", "startLiveRunFlow")
        Intent(context, LiveRunService::class.java).also {
            it.putExtra(LiveRunService.INTENT_COMMAND, LiveRunService.INTENT_COMMAND_START)
            val name = context.startForegroundService(it)
            logDebug("LiveRunServiceProxy", "startForegroundService $name")
            val res = context.bindService(it, connection, Context.BIND_AUTO_CREATE)
            logDebug("LiveRunServiceProxy", "bind service $res")
        }
        logDebug("LiveRunServiceProxy", "startLiveRunFlow: bounded? ${boundFlow.value}")
    }

    fun intervalRequest(nextIntervalTarget: IntervalTarget) {
        if (_boundFlow.value) {
            liveRunService.intervalRequest(nextIntervalTarget)
        }
    }

    fun finish(finishedCallback: (FinishedRun) -> Unit) {
        if (!_boundFlow.value) {
            logDebug("LiveRunServiceProxy", "Cannot finish as service not bound.")
            return
        }
        liveRunService.finish(finishedCallback)
        _boundFlow.value = false
    }

    fun cancel() {
        if (!_boundFlow.value) {
            logDebug("LiveRunServiceProxy", "Cannot cancel as service not bound.")
            return
        }
        liveRunService.cancel()
        _boundFlow.value = false
    }

    fun unbind() {
        logDebug("LiveRunServiceProxy", "unbind service")
        context.unbindService(connection)
    }

    fun liveRunFlow() =
        if (boundFlow.value) {
            liveRunService.liveRunFlow.flow
        } else {
            flowOf(LiveRun())
        }

}