package com.sofa.nerdrunning.finishedrun

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.sofa.nerdrunning.liverun.RunLocation
import kotlinx.parcelize.Parcelize
import java.time.Duration
import java.time.LocalDateTime

@Parcelize
data class FinishedRun(
    val startDateTime: LocalDateTime = LocalDateTime.now(),
    val duration: Duration = Duration.ZERO,
    val distance: Int = 0,
    val meanPace: Duration = Duration.ZERO,
    val hasHeartRate: Boolean = true,
    val meanHeartRate: Int = 75,
    val altitudeUp: Int = 0,
    val altitudeDown: Int = 0,

    val intervals: List<FinishedRunInterval> = emptyList(),
    val locations: List<LatLng> = emptyList(),
) : Parcelable

@Parcelize
data class FinishedRunInterval(
    val startDistanceInRun: Int = 0,
    val distance: Int = 0,
    val duration: Duration = Duration.ZERO,
    val kmPace: Duration = Duration.ZERO,
    val meanHeartRate: Int = 75,
    val altitudeUp: Int = 0,
    val altitudeDown: Int = 0,

    val start: RunLocation = RunLocation(),
    val end: RunLocation = RunLocation(),
) : Parcelable
