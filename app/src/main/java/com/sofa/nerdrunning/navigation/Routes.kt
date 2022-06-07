package com.sofa.nerdrunning.navigation

import com.sofa.nerdrunning.persistentrun.RunId

object Routes {
    const val home = "home"
    const val liveRun = "liveRun"
    const val finishedRun = "finishedRun/{${Args.runId}}"
    const val listFinishedRuns = "listFinishedRuns"

    fun finishedRun(runId: RunId) = "finishedRun/$runId"

    object Args {
        const val runId = "runId"
    }
}
