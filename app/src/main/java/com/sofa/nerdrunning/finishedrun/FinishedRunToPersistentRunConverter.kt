package com.sofa.nerdrunning.finishedrun

import com.sofa.nerdrunning.liverun.RunLocation
import com.sofa.nerdrunning.persistentrun.LocationEntity
import com.sofa.nerdrunning.persistentrun.Run
import com.sofa.nerdrunning.persistentrun.RunId
import com.sofa.nerdrunning.persistentrun.RunInterval

fun FinishedRun.toRun() =
    Run(
        startDateTime = startDateTime,
        duration = duration,
        distance = distance,
        meanPace = meanPace,
        meanHeartRate = if (hasHeartRate) meanHeartRate else -1,
        altitudeUp = altitudeUp,
        altitudeDown = altitudeDown,
        numberOfIntervals = intervals.size,
    )

fun FinishedRunInterval.toRunInterval(runId: RunId) =
    RunInterval(
        startDistanceInRun = startDistanceInRun,
        runId = runId,
        distance = distance,
        duration = duration,
        kmPace = kmPace,
        meanHeartRate = meanHeartRate,
        altitudeUp = altitudeUp,
        altitudeDown = altitudeDown,
        start = start.toLocationEntity(),
        end = end.toLocationEntity(),
    )

fun RunLocation.toLocationEntity() =
    LocationEntity(
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        speed = speed,
        speedAccuracy = speedAccuracy,
    )
