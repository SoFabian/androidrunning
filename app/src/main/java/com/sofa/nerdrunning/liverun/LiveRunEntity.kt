package com.sofa.nerdrunning.liverun

import android.location.Location
import android.os.Parcelable
import androidx.compose.runtime.Stable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Stable
@Parcelize
data class LiveRun(
    val duration: Duration = Duration.ZERO,
    val distance: Int = 0,
    val lastAddedDistance: Int = 0,

    val hasHeartRate: Boolean = false,

    val meanPace: Duration = Duration.ZERO,
    val meanHeartRate: Int = 75,
    val livePace: Duration = Duration.ZERO,
    val liveHeartRate: Int = 75,

    val currentInterval: LiveRunInterval = LiveRunInterval(),

    val intervals: MutableList<LiveRunInterval> = mutableListOf(),
    val locations: MutableList<RunLocation> = mutableListOf(),

    val tick: Int = 0,
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LiveRun

        if (tick != other.tick) return false

        return true
    }

    override fun hashCode(): Int {
        return tick
    }
}

@Parcelize
data class IntervalTarget(
    val targetValue: Int = 1000,
    val targetUnit: TargetUnit = TargetUnit.METER
) : Parcelable

enum class TargetUnit {
    METER, MINUTE, SECOND
}

@Parcelize
data class LiveRunInterval(
    val duration: Duration = Duration.ZERO,
    val distance: Int = 0,
    val pace: Duration = Duration.ZERO,
    val heartRate: Int = 75,

    val target: IntervalTarget = IntervalTarget(),
    val targetNumber: Int = 0,

    val start: RunLocation = RunLocation(),
    val end: RunLocation? = null,
) : Parcelable

fun LiveRunInterval.isStopped(): Boolean =
    end != null || target.isReached(duration, distance)

fun IntervalTarget.isReached(duration: Duration, distance: Int): Boolean =
    when (targetUnit) {
        TargetUnit.METER -> {
            distance >= targetValue
        }
        TargetUnit.MINUTE -> {
            duration >= Duration.ofMinutes(targetValue.toLong())
        }
        TargetUnit.SECOND -> {
            duration >= Duration.ofSeconds(targetValue.toLong())
        }
    }

@Parcelize
data class RunLocation(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val latitude: Double = 48.23,
    val longitude: Double = 22.56,
    val accuracy: Float = 20f,
    val altitude: Double = 202.5,
    val speed: Float = 20.9f,
    val speedAccuracy: Float = 1.2f,
) : Parcelable

fun Location.toRunLocation(timestamp: LocalDateTime = LocalDateTime.now()): RunLocation =
    RunLocation(
        timestamp,
        latitude,
        longitude,
        accuracy,
        altitude,
        speed,
        speedAccuracyMetersPerSecond,
    )

fun RunLocation.distanceTo(location: Location): Int {
    val distanceToLastLocation = toLocation().distanceTo(location).roundToInt()
    return if (distanceToLastLocation < location.accuracy && location.speed < 1) {
        0
    } else {
        distanceToLastLocation
    }
}

fun RunLocation.toLocation(): Location =
    Location("").apply {
        latitude = this@toLocation.latitude
        longitude = this@toLocation.longitude
        altitude = this@toLocation.altitude
        speed = this@toLocation.speed
        accuracy = this@toLocation.accuracy
        speedAccuracyMetersPerSecond = this@toLocation.speedAccuracy
    }

fun RunLocation.toLatLng(): LatLng =
    LatLng(latitude, longitude)
