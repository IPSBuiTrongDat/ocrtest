package com.example.ocrtest.repository

import com.example.ocrtest.data.TranslationDao
import com.example.ocrtest.data.TranslationEntity
import kotlinx.coroutines.flow.Flow

class TranslationRepository(private val translationDao: TranslationDao) {
    suspend fun insert(translationEntity: TranslationEntity) {
        translationDao.insert(translationEntity)
    }

    fun getAllTranslations(): Flow<List<TranslationEntity>> {
        return translationDao.getAllTranslations()
    }
}