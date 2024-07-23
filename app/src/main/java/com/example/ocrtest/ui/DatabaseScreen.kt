package com.example.ocrtest.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ocrtest.data.TranslationEntity
import com.example.ocrtest.viewmodel.TranslationViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseScreen(viewModel: TranslationViewModel) {
    val translations by viewModel.translations.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Database") }
            )
        }
    ) {
        TranslationList(translations)
    }
}

@Composable
fun TranslationList(translations: List<TranslationEntity>) {
    LazyColumn {
        items(translations) { translationEntity ->
            Text(
                text = "Word: ${translationEntity.word}\nMeaning: ${translationEntity.meaning}\nDate: ${translationEntity.importDay}",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
