package com.sofa.nerdrunning.liverun

import com.google.maps.android.PolyUtil
import com.sofa.nerdrunning.finishedrun.FinishedRun
import com.sofa.nerdrunning.finishedrun.FinishedRunInterval
import java.time.Duration

fun LiveRun.finish(): FinishedRun {
    var currentDistanceInRun = 0
    var currentLocationIndex = 0
    val finishedIntervals = MutableList(intervals.size + 1) { index ->
        val liveInterval =
            intervals.getOrElse(index) { currentInterval.copy(end = locations.lastOrNull()) }
        val locationsOfInterval = locationsOfInterval(currentLocationIndex, liveInterval)
        val altitudeUpDown = computeAltitudeUpDown(locationsOfInterval)
        val finishedInterval = liveInterval.finish(currentDistanceInRun, altitudeUpDown)
        currentDistanceInRun += finishedInterval.distance
        currentLocationIndex += locationsOfInterval.size
        finishedInterval
    }
    val altitudeUpDown = computeAltitudeUpDownFromIntervals(finishedIntervals)
    val firstInterval = intervals.getOrElse(0) { currentInterval }
    val latLngs = locations.map { l -> l.toLatLng() }
    val simplifiedLatLngs =
        if (latLngs.size > 50) {
            PolyUtil.simplify(latLngs, 5.0)
        } else {
            latLngs
        }
    return FinishedRun(
        startDateTime = firstInterval.start.timestamp,
        duration = duration,
        distance = distance,
        meanPace = meanPace,
        hasHeartRate = hasHeartRate,
        meanHeartRate = if (hasHeartRate) meanHeartRate else -1,
        altitudeUp = altitudeUpDown.metresUp.toInt(),
        altitudeDown = altitudeUpDown.metresDown.toInt(),
        intervals = finishedIntervals,
        locations = simplifiedLatLngs,
    )
}

private fun LiveRunInterval.finish(
    currentDistanceInRun: Int,
    altitudeUpDown: AltitudeUpDown,
): FinishedRunInterval =
    FinishedRunInterval(
        startDistanceInRun = currentDistanceInRun,
        distance = distance,
        duration = duration,
        kmPace = if (distance > 0) duration.dividedBy(distance.toLong())
            .multipliedBy(1000) else Duration.ZERO,
        meanHeartRate = heartRate,
        altitudeUp = altitudeUpDown.metresUp.toInt(),
        altitudeDown = altitudeUpDown.metresDown.toInt(),
        start = start,
        end = end ?: start,
    )

private fun LiveRun.locationsOfInterval(
    startLocationIndex: Int,
    liveInterval: LiveRunInterval,
): List<RunLocation> = locations.drop(startLocationIndex)
    .takeWhile { loc -> liveInterval.end == null || loc.timestamp < liveInterval.end.timestamp }

private fun computeAltitudeUpDown(locations: List<RunLocation>): AltitudeUpDown =
    locations.map(RunLocation::altitude)
        .chunked(10).map(Iterable<Double>::average)
        .foldIndexed(AltitudeUpDown()) { index, acc, altitude ->
            if (index == 0) {
                acc
            } else {
                val diff = altitude.minus(locations[index - 1].altitude)
                when {
                    diff > 0 -> acc.copy(metresUp = acc.metresUp + diff)
                    diff < 0 -> acc.copy(metresDown = acc.metresDown + diff)
                    else -> acc
                }
            }
        }

private fun computeAltitudeUpDownFromIntervals(intervals: List<FinishedRunInterval>): AltitudeUpDown =
    intervals.fold(AltitudeUpDown()) { acc, interval ->
        AltitudeUpDown(
            metresUp = acc.metresUp + interval.altitudeUp,
            metresDown = acc.metresDown + interval.altitudeDown,
        )
    }

private data class AltitudeUpDown(
    val metresUp: Double = 0.0,
    val metresDown: Double = 0.0,
)
