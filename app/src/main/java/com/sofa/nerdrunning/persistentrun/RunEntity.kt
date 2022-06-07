package com.sofa.nerdrunning.persistentrun

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.google.android.gms.maps.model.LatLng
import java.time.Duration
import java.time.LocalDateTime

typealias RunId = Long

@Entity
data class Run(
    @PrimaryKey(autoGenerate = true)
    val id: RunId = 0,

    val startDateTime: LocalDateTime,
    val duration: Duration,
    val distance: Int,
    val meanPace: Duration,
    val meanHeartRate: Int,
    val altitudeUp: Int,
    val altitudeDown: Int,
    val numberOfIntervals: Int,
)

data class RunIdHolder(
    val id: RunId
)

data class RunDetail(
    @Embedded val run: Run,

    @Relation(
        parentColumn = "id",
        entityColumn = "runId"
    )
    val locations: List<Coordinate>,

    @Relation(
        parentColumn = "id",
        entityColumn = "runId"
    )
    val intervals: List<RunInterval>,
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Run::class,
            parentColumns = ["id"],
            childColumns = ["runId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index(value = ["runId"])]
)
data class RunInterval(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val runId: RunId,

    val startDistanceInRun: Int,
    val distance: Int,
    val duration: Duration,
    val kmPace: Duration,
    val meanHeartRate: Int,
    val altitudeUp: Int,
    val altitudeDown: Int,

    @Embedded(prefix = "start_") val start: LocationEntity,
    @Embedded(prefix = "end_") val end: LocationEntity,
)

data class LocationEntity(
    val timestamp: LocalDateTime,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double,
    val speed: Float,
    val speedAccuracy: Float,
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Run::class,
            parentColumns = ["id"],
            childColumns = ["runId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index(value = ["runId"])]
)
data class Coordinate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val runId: RunId,

    val latitude: Double,
    val longitude: Double,
)

fun LatLng.toCoordinate(runId: RunId): Coordinate =
    Coordinate(runId = runId, latitude = latitude, longitude = longitude)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Run::class,
            parentColumns = ["id"],
            childColumns = ["runId"],
            onDelete = CASCADE
        )
    ],
    indices = [Index(value = ["runId"])]
)
data class StaticMapsImage(
    @PrimaryKey
    val runId: RunId,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val image: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StaticMapsImage

        if (runId != other.runId) return false

        return true
    }

    override fun hashCode(): Int {
        return runId.hashCode()
    }
}
