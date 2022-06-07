package com.sofa.nerdrunning.database

import androidx.room.TypeConverter
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    @TypeConverter
    fun fromDbLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun toDbLocalDateTime(date: LocalDateTime?): String? {
        return date?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @TypeConverter
    fun fromDbDuration(value: String?): Duration? {
        return value?.let { Duration.parse(it) }
    }

    @TypeConverter
    fun toDbDuration(duration: Duration?): String? {
        return duration?.toString()
    }
}