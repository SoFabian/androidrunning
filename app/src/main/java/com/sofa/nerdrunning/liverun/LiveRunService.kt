package com.sofa.nerdrunning.liverun

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import com.sofa.nerdrunning.R
import com.sofa.nerdrunning.elevation.Elevations
import com.sofa.nerdrunning.finishedrun.FinishedRun
import com.sofa.nerdrunning.images.StaticMapsImageRetriever
import com.sofa.nerdrunning.log.logDebug
import com.sofa.nerdrunning.persistentrun.RunRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@AndroidEntryPoint
class LiveRunService : Service() {

    @Inject
    lateinit var liveRunFlowProvider: LiveRunFlowProvider

    @Inject
    lateinit var runRepository: RunRepository

    @Inject
    lateinit var staticMapsImageRetriever: StaticMapsImageRetriever

    @Inject
    lateinit var elevations: Elevations

    private val binder = LiveRunBinder()
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val started = AtomicBoolean()

    private val vibrator by lazy {
//        should work but does not
//        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
//        vibratorManager.defaultVibrator
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    private val intervalTargetReachedFlow by lazy {
        MutableSharedFlow<Int>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }

    override fun onCreate() {
        super.onCreate()
        logDebug("LiveRunService", "onCreate")
    }

    override fun onBind(intent: Intent?): IBinder {
        logDebug("LiveRunService", "onBind")
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val command = intent?.getStringExtra(INTENT_COMMAND)
        logDebug("LiveRunService", "onStartCommand $command")
        if (command == INTENT_COMMAND_FINISH) {
            stopService()
            return START_NOT_STICKY
        }
        if (command == INTENT_COMMAND_START && started.compareAndSet(false, true)) {
            logDebug("LiveRunService", "create flow")
            collectIntervalTargetReachedFlow()
            liveRunFlow = liveRunFlowProvider.createFlow(
                intervalRequestFlow.asSharedFlow(),
                intervalTargetReachedFlow,
                serviceScope
            )
        }
        showNotification()
        return START_STICKY
    }

    private fun collectIntervalTargetReachedFlow() {
        serviceScope.launch {
            intervalTargetReachedFlow.collectLatest { vibrate() }
        }
    }

    private fun vibrate() {
        logDebug("LiveRunService", "Vibrate")
        vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun stopService() {
        logDebug("LiveRunService", "stop")
        serviceJob.cancel()
        vibrator.cancel()
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        logDebug("LiveRunService", "onDestroy")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logDebug("LiveRunService", "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        logDebug("LiveRunService", "onRebind")
        super.onRebind(intent)
    }

    private fun showNotification() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Live run channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("You are running.")
            .setContentText("Keep up!")
            .setSmallIcon(R.drawable.ic_stat_runner)
            .build()

        startForeground(CODE_FOREGROUND_SERVICE, notification)
    }

    /** method for clients  */

    lateinit var liveRunFlow: LiveRunFlow
    private val intervalRequestFlow by lazy {
        val flow = MutableSharedFlow<IntervalTarget>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        flow.tryEmit(IntervalTarget())
        flow
    }

    fun intervalRequest(nextIntervalTarget: IntervalTarget) {
        intervalRequestFlow.tryEmit(nextIntervalTarget)
    }

    fun finish(finishedCallback: (FinishedRun) -> Unit) {
        logDebug("LiveRunService", "finish")
        val liveRun = liveRunFlow.stop()
        serviceScope.launch {
            val latLngs = liveRun.locations.map { it.toLatLng() }
            val elevationsMap = elevations.getElevationsForLocations(latLngs)
            val finishedRun = liveRun.finish(elevationsMap)
            finishedCallback(finishedRun)
            val runId = runRepository.insert(finishedRun)
            logDebug("LiveRunService", "persist run ID $runId")
            staticMapsImageRetriever.loadAndPersist(runId, finishedRun.locations)
            stopService()
        }
    }

    fun cancel() {
        logDebug("LiveRunService", "cancel")
        stopService()
    }

    companion object {
        const val INTENT_COMMAND = "Command"
        const val INTENT_COMMAND_START = "Start"
        const val INTENT_COMMAND_FINISH = "Finish"

        private const val CHANNEL_ID = "Live run notification channel"
        private const val CODE_FOREGROUND_SERVICE = 1
    }

    inner class LiveRunBinder : Binder() {
        fun getService(): LiveRunService = this@LiveRunService
    }
}
