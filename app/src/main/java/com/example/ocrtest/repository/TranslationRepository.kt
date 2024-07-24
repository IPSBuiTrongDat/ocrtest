package com.example.ocrtest.repository

import com.example.ocrtest.data.TranslationDao
import com.example.ocrtest.data.TranslationEntity
import kotlinx.coroutines.flow.Flow

class TranslationRepository(private val translationDao: TranslationDao) {

    fun getAllTranslations(): Flow<List<TranslationEntity>> {
        return translationDao.getAllTranslations()
    }

    suspend fun insert(translation: TranslationEntity) {
        translationDao.insert(translation)
    }

    suspend fun clearAll() {
        translationDao.clearAll()
    }
}
