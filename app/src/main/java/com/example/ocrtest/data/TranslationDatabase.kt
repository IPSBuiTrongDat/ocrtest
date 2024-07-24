package com.example.ocrtest.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ocrtest.utils.DateTypeConverter

@Database(entities = [TranslationEntity::class], version = 1, exportSchema = false)
@TypeConverters(DateTypeConverter::class)
abstract class TranslationDatabase : RoomDatabase() {
    abstract fun translationDao(): TranslationDao

    companion object {
        @Volatile
        private var INSTANCE: TranslationDatabase? = null

        fun getDatabase(context: Context): TranslationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TranslationDatabase::class.java,
                    "translation_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
