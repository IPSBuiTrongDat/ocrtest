package com.example.ocrtest.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ocrtest.data.TranslationDao
import com.example.ocrtest.data.TranslationEntity
import kotlinx.coroutines.launch

class TranslationViewModel(private val translationDao: TranslationDao) : ViewModel() {
    private val _allTranslations = MutableLiveData<List<TranslationEntity>>()
    val allTranslations: LiveData<List<TranslationEntity>> = _allTranslations

    init {
        Log.e("Open datatable", "loadAllTranslations123")
        loadAllTranslations()
    }


    fun loadAllTranslations() {
        viewModelScope.launch {
            _allTranslations.postValue(translationDao.getAllTranslationsList())
        }
    }

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
