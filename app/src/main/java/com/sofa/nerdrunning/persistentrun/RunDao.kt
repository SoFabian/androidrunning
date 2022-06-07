package com.sofa.nerdrunning.persistentrun

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sofa.nerdrunning.finishedrun.FinishedRun
import com.sofa.nerdrunning.finishedrun.toRun
import com.sofa.nerdrunning.finishedrun.toRunInterval

@Dao
interface RunDao {

    @Query("SELECT * FROM run ORDER BY id DESC")
    fun readAll(): PagingSource<Int, Run>

    @Transaction
    @Query("SELECT * FROM run WHERE id = :runId")
    suspend fun readRunDetail(runId: RunId): RunDetail?

    @Insert
    fun insert(run: Run): Long

    @Insert
    fun insert(runIntervals: List<RunInterval>)

    @Insert
    fun insertLocations(runLocations: List<Coordinate>)

    @Transaction
    suspend fun insert(finishedRun: FinishedRun): Long {
        val run = finishedRun.toRun()
        val runId = insert(run)
        val runIntervals = finishedRun.intervals.map { i -> i.toRunInterval(runId) }
        insert(runIntervals)
        val runLocations = finishedRun.locations.map { l -> l.toCoordinate(runId) }
        insertLocations(runLocations)
        return runId
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertImage(staticMapsImage: StaticMapsImage)

    @Query("SELECT image FROM staticMapsImage WHERE runId = :runId")
    fun readImage(runId: RunId): ByteArray?

    @Delete(entity = Run::class)
    fun deleteRun(runIdHolder: RunIdHolder)

}