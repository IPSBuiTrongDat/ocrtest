package com.example.ocrtest.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import java.util.Date

@Database(entities = [TranslationEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TranslationDatabase : RoomDatabase() {
    abstract fun translationDao(): TranslationDao
}

class Converters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}