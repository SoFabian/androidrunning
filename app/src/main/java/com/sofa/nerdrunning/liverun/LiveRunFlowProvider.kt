package com.sofa.nerdrunning.liverun

import com.sofa.nerdrunning.heartrate.HeartRateMonitorProvider
import com.sofa.nerdrunning.location.LocationFlowProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LiveRunFlowProvider @Inject constructor(
    private val locationFlowProvider: LocationFlowProvider,
    private val heartRateFlowProvider: HeartRateMonitorProvider,
) {

    fun createFlow(
        intervalRequestFlow: Flow<IntervalTarget>,
        scope: CoroutineScope
    ): LiveRunFlow {
        val locationFlow = locationFlowProvider.createFlow()
        val heartRateMonitor = heartRateFlowProvider.createMonitor(scope)
        return LiveRunFlow(locationFlow, heartRateMonitor, intervalRequestFlow, scope)
    }

}
