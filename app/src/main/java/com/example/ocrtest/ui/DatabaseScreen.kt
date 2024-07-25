package com.example.ocrtest.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ocrtest.data.TranslationEntity
import com.example.ocrtest.viewmodel.TranslationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseScreen(translations: List<TranslationEntity>,translationViewModel: TranslationViewModel) {
    LaunchedEffect(key1 = "view_database") {
        translationViewModel.loadAllTranslations()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Database") },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        LazyRow {
                            item { HeaderText("ID") }
                            item { HeaderText("語彙") }
                            item { HeaderText("意味") }
                            item { HeaderText("分類") }
                            item { HeaderText("メモ") }
                        }
                    }

                    items(translations.size) { index ->
                        val translation = translations[index]
                        LazyRow {
                            item { BodyText(translation.id.toString()) }
                            item { BodyText(translation.word) }
                            item { BodyText(translation.meaning) }
                            item { BodyText(translation.type) }
                            item { BodyText(translation.memo) }
                        }
                        Divider(color = Color.Gray, thickness = 1.dp)
                    }
                }
            }
        }
    )
}

@Composable
fun HeaderText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .padding(8.dp)
            .width(100.dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onPrimary,
        maxLines = 1
    )
}

@Composable
fun BodyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .padding(8.dp)
            .width(100.dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1
    )
}
