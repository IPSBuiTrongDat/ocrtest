package com.example.ocrtest.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ocrtest.data.TranslationEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseScreen(translations: List<TranslationEntity>) {
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
                val horizontalScrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    // Header Row
                    Row {
                        HeaderText("ID", Modifier.weight(1f))
                        HeaderText("語彙", Modifier.weight(2f))
                        HeaderText("意味", Modifier.weight(2f))
                        HeaderText("分類", Modifier.weight(1f))
                        HeaderText("メモ", Modifier.weight(2f))
                    }
                    Divider(color = Color.Gray, thickness = 1.dp)

                    // Data Rows
                    translations.forEach { translation ->
                        Row {
                            BodyText(translation.id.toString(), Modifier.weight(1f))
                            BodyText(translation.word, Modifier.weight(2f))
                            BodyText(translation.meaning, Modifier.weight(2f))
                            BodyText(translation.type, Modifier.weight(1f))
                            BodyText(translation.memo, Modifier.weight(2f))
                        }
                        Divider(color = Color.Gray, thickness = 1.dp)
                    }
                }
            }
        }
    )
}

@Composable
fun HeaderText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
            .padding(8.dp)
            .height(40.dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onPrimary,
        maxLines = 1
    )
}

@Composable
fun BodyText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .padding(8.dp)
            .height(40.dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1
    )
}
