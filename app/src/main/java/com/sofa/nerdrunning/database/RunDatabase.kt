package com.sofa.nerdrunning.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sofa.nerdrunning.persistentrun.Coordinate
import com.sofa.nerdrunning.persistentrun.Run
import com.sofa.nerdrunning.persistentrun.RunDao
import com.sofa.nerdrunning.persistentrun.RunInterval
import com.sofa.nerdrunning.persistentrun.StaticMapsImage

@Database(entities = [Run::class, RunInterval::class, Coordinate::class, StaticMapsImage::class], version = 1)
@TypeConverters(Converters::class)
abstract class RunDatabase : RoomDatabase() {

    abstract fun runDao(): RunDao

}
