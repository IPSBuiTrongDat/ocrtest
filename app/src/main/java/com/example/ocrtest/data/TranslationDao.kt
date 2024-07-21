package com.example.ocrtest.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(translationEntity: TranslationEntity)

    @Query("SELECT * FROM translations")
    fun getAllTranslations(): Flow<List<TranslationEntity>>
}