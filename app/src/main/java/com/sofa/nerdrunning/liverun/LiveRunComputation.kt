package com.sofa.nerdrunning.liverun

import android.location.Location
import com.sofa.nerdrunning.log.logDebug
import java.time.Duration
import java.time.LocalDateTime

fun LiveRun.addLocation(
    t: Int,
    location: Location,
    hr: Int,
    intervalTarget: IndexedValue<IntervalTarget>,
): LiveRun {
    if (tick == t) {
        return this
    }
    val now = LocalDateTime.now()
    val distanceToAdd =
        if (locations.isEmpty()) {
            0
        } else {
            locations.last().distanceTo(location)
        }
    val newDistance = distance + distanceToAdd
    val runLocation = location.toRunLocation(now)
    val newCurrentInterval =
        if (currentInterval.targetNumber != intervalTarget.index) {
            logDebug("LiveRunComputation", "start interval")
            val updatedCurrentInterval =
                currentInterval.addLocation(runLocation, distanceToAdd, now, hr, true)
            intervals.add(updatedCurrentInterval)
            LiveRunInterval(
                start = runLocation,
                target = intervalTarget.value,
                targetNumber = intervalTarget.index,
                heartRate = hr,
            )
        } else {
            val updatedCurrentInterval =
                if (locations.isEmpty()) {
                    LiveRunInterval(start = runLocation, heartRate = hr)
                } else {
                    currentInterval.addLocation(runLocation, distanceToAdd, now, hr, false)
                }
            if (updatedCurrentInterval.isStopped()) {
                intervals.add(updatedCurrentInterval)
                LiveRunInterval(start = runLocation, targetNumber = currentInterval.targetNumber)
            } else {
                updatedCurrentInterval
            }
        }
    val firstInterval = intervals.getOrElse(0) { currentInterval }
    val newDuration = Duration.between(firstInterval.start.timestamp, now)
    locations.add(runLocation)
    val newMeanPace =
        if (newDistance > 0) {
            newDuration.multipliedBy(1000).dividedBy(newDistance.toLong())
        } else {
            Duration.ZERO
        }
    val newLivePace = computeLivePace(locations.takeLast(20))
    val newMeanHeartRate = computeMeanHeartRate(meanHeartRate, hr, duration, newDuration)
    return LiveRun(
        tick = t,
        duration = newDuration.roundToNearestThousand(),
        distance = distance + distanceToAdd,
        lastAddedDistance = distanceToAdd,
        meanPace = newMeanPace,
        livePace = newLivePace,
        hasHeartRate = hr > 0,
        meanHeartRate = newMeanHeartRate,
        liveHeartRate = hr,
        currentInterval = newCurrentInterval,
        locations = locations,
        intervals = intervals,
    )
}

private fun Duration.roundToNearestThousand(): Duration {
    val millis = toMillis()
    val rest = millis % 1000
    val roundedMillis =
        if (rest > 500) {
            millis - rest + 1000
        } else {
            millis - rest
        }
    return Duration.ofMillis(roundedMillis)
}

fun computeMeanHeartRate(
    meanHeartRate: Int,
    hr: Int,
    oldDuration: Duration,
    newDuration: Duration
): Int {
    if (hr <= 0 || newDuration.isZero) {
        return meanHeartRate
    }
    val diff = newDuration.minus(oldDuration).seconds
    if (diff <= 0) {
        return meanHeartRate
    }
    return ((meanHeartRate * oldDuration.seconds.toInt()) + (hr * diff.toInt())) / newDuration.seconds.toInt()
}

fun LiveRunInterval.addLocation(
    runLocation: RunLocation,
    distanceToAdd: Int,
    timestamp: LocalDateTime,
    hr: Int,
    stopped: Boolean,
): LiveRunInterval {
    val newDuration = Duration.between(start.timestamp, timestamp)
    val newDistance = distance + distanceToAdd
    val newPace =
        if (newDistance > 0) {
            newDuration.multipliedBy(1000).dividedBy(newDistance.toLong())
        } else {
            Duration.ZERO
        }
    val newEnd =
        if (stopped || target.isReached(newDuration, newDistance)) {
            runLocation
        } else {
            null
        }
    val newHeartRate = computeMeanHeartRate(heartRate, hr, duration, newDuration)
    return LiveRunInterval(
        duration = newDuration.roundToNearestThousand(),
        distance = newDistance,
        pace = newPace,
        heartRate = newHeartRate,
        start = start,
        end = newEnd,
        target = target,
        targetNumber = targetNumber,
    )
}

private fun computeLivePace(locations: List<RunLocation>): Duration {
    if (locations.isEmpty() || locations.size == 1) {
        return Duration.ZERO
    }
    val distance = locations.foldIndexed(0) { index, acc, location ->
        if (index == 0) {
            0
        } else {
            acc + locations[index - 1].distanceTo(location.toLocation())
        }
    }
    val duration = Duration.between(locations.first().timestamp, locations.last().timestamp)
    return if (distance > 0) {
        duration.multipliedBy(1000).dividedBy(distance.toLong())
    } else {
        Duration.ZERO
    }
}
