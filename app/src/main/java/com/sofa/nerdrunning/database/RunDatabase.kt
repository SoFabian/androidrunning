package com.sofa.nerdrunning.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.sofa.nerdrunning.persistentrun.Coordinate
import com.sofa.nerdrunning.persistentrun.Run
import com.sofa.nerdrunning.persistentrun.RunDao
import com.sofa.nerdrunning.persistentrun.RunInterval
import com.sofa.nerdrunning.persistentrun.StaticMapsImage

@Database(
    entities = [Run::class, RunInterval::class, Coordinate::class, StaticMapsImage::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2, spec = RunDatabase.SpecFrom1To2::class)]
)
@TypeConverters(Converters::class)
abstract class RunDatabase : RoomDatabase() {

    abstract fun runDao(): RunDao

    @DeleteColumn(tableName = "RunInterval", columnName = "start_altitude")
    @DeleteColumn(tableName = "RunInterval", columnName = "end_altitude")
    class SpecFrom1To2 : AutoMigrationSpec

}
