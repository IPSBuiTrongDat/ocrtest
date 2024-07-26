package com.example.ocrtest.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ocrtest.data.TranslationEntity
import com.example.ocrtest.viewmodel.TranslationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseScreen(translations: List<TranslationEntity>, translationViewModel: TranslationViewModel) {
    var selectedTranslation by remember { mutableStateOf<TranslationEntity?>(null) }

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
                            item { HeaderText("No") }
                            item { HeaderText("語彙") }
                            item { HeaderText("意味") }
                            item { HeaderText("分類") }
                            item { HeaderText("メモ") }
                            item { HeaderText("日付") }
                        }
                    }

                    items(translations.size) { index ->
                        val translation = translations[index]
                        LazyRow(
                            modifier = Modifier.clickable { selectedTranslation = translation }
                        ) {
                            item { BodyText((index + 1).toString()) }
                            item { BodyText(shortenText(translation.word)) }
                            item { BodyText(shortenText(translation.meaning)) }
                            item { BodyText(translation.type) }
                            item { BodyText(translation.memo) }
                            item { BodyText(formatDate(translation.importDay)) }
                        }
                        Divider(color = Color.Gray, thickness = 1.dp)
                    }
                }
            }
        }
    )

    selectedTranslation?.let { translation ->
        TranslationDialog(translation) {
            selectedTranslation = null
        }
    }
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
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun TranslationDialog(translation: TranslationEntity, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                DialogText(title = "語彙", content = translation.word)
                DialogText(title = "意味", content = translation.meaning)
                DialogText(title = "分類", content = translation.type)
                DialogText(title = "メモ", content = translation.memo)
                DialogText(title = "日付", content = formatDate(translation.importDay))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("閉じる")
                }
            }
        }
    }
}

@Composable
fun DialogText(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "$title: ",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Red
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}

fun shortenText(text: String, maxLength: Int = 10): String {
    return if (text.length > maxLength) {
        text.take(maxLength-3) + "..."
    } else {
        text
    }
}

fun formatDate(date: Date?): String {
    return date?.let {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it)
    } ?: "N/A"
}
