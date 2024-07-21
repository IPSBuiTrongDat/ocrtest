package com.example.ocrtest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ocrtest.data.TranslationEntity
import com.example.ocrtest.repository.TranslationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Date

class TranslationViewModel(private val repository: TranslationRepository) : ViewModel() {
    private val _translations = MutableStateFlow<List<TranslationEntity>>(emptyList())
    val translations: StateFlow<List<TranslationEntity>> = _translations.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTranslations().collect { translationList ->
                _translations.value = translationList
            }
        }
    }

    fun insertTranslation(word: String, meaning: String) {
        val translationEntity = TranslationEntity(
            word = word,
            meaning = meaning,
            writtenDate = Date()
        )
        viewModelScope.launch {
            repository.insert(translationEntity)
        }
    }
}