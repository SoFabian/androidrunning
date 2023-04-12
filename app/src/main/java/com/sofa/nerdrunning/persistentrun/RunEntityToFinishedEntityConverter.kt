package com.sofa.nerdrunning.persistentrun

import com.google.android.gms.maps.model.LatLng
import com.sofa.nerdrunning.finishedrun.FinishedRun
import com.sofa.nerdrunning.finishedrun.FinishedRunInterval
import com.sofa.nerdrunning.liverun.RunLocation

fun Coordinate.toLatLng(): LatLng =
    LatLng(latitude, longitude)

fun RunDetail.toFinishedRun() =
    FinishedRun(
        startDateTime = run.startDateTime,
        duration = run.duration,
        distance = run.distance,
        meanPace = run.meanPace,
        hasHeartRate = run.meanHeartRate > 0,
        meanHeartRate = run.meanHeartRate,
        altitudeUp = run.altitudeUp,
        altitudeDown = run.altitudeDown,
        intervals = intervals.map(RunInterval::toFinishedRunInterval),
        locations = locations.map(Coordinate::toLatLng),
    )

fun RunInterval.toFinishedRunInterval() =
    FinishedRunInterval(
        startDistanceInRun = startDistanceInRun,
        distance = distance,
        duration = duration,
        kmPace = kmPace,
        meanHeartRate = meanHeartRate,
        altitudeUp = altitudeUp,
        altitudeDown = altitudeDown,
        start = start.toRunLocation(),
        end = end.toRunLocation(),
    )


fun LocationEntity.toRunLocation() =
    RunLocation(
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        speed = speed,
        speedAccuracy = speedAccuracy,
    )
