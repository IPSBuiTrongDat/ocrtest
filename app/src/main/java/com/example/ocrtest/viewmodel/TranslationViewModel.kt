package com.example.ocrtest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ocrtest.data.TranslationDao
import com.example.ocrtest.data.TranslationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TranslationViewModel(private val translationDao: TranslationDao) : ViewModel() {
    val allTranslations: Flow<List<TranslationEntity>> = translationDao.getAllTranslations()

    fun insertTranslation(translation: TranslationEntity) {
        viewModelScope.launch {
            translationDao.insert(translation)
        }
    }

    fun clearAllTranslations() {
        viewModelScope.launch {
            translationDao.clearAll()
        }
    }
}
