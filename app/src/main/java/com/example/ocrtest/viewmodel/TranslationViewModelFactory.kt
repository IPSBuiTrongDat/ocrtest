package com.example.ocrtest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ocrtest.data.TranslationDao

class TranslationViewModelFactory(private val translationDao: TranslationDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TranslationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TranslationViewModel(translationDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
