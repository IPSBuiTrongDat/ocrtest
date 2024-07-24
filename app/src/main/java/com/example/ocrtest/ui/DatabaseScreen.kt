package com.example.ocrtest.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ocrtest.viewmodel.TranslationViewModel
import kotlinx.coroutines.launch

@Composable
fun DatabaseScreen(viewModel: TranslationViewModel = viewModel()) {
    val translations = viewModel.allTranslations.collectAsState(initial = emptyList())

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(modifier = Modifier.horizontalScroll(scrollState)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface)
            ) {
                Text("ID", modifier = Modifier.padding(8.dp))
                translations.value.forEach { translation ->
                    Text(translation.id.toString(), modifier = Modifier.padding(8.dp))
                }
            }
            Column(
                modifier = Modifier
                    .weight(2f)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface)
            ) {
                Text("語彙", modifier = Modifier.padding(8.dp))
                translations.value.forEach { translation ->
                    Text(translation.word, modifier = Modifier.padding(8.dp))
                }
            }
            Column(
                modifier = Modifier
                    .weight(2f)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface)
            ) {
                Text("意味", modifier = Modifier.padding(8.dp))
                translations.value.forEach { translation ->
                    Text(translation.meaning, modifier = Modifier.padding(8.dp))
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface)
            ) {
                Text("分類", modifier = Modifier.padding(8.dp))
                translations.value.forEach { translation ->
                    Text(translation.type, modifier = Modifier.padding(8.dp))
                }
            }
            Column(
                modifier = Modifier
                    .weight(2f)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface)
            ) {
                Text("メモ", modifier = Modifier.padding(8.dp))
                translations.value.forEach { translation ->
                    Text(translation.memo, modifier = Modifier.padding(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                coroutineScope.launch {
                    viewModel.clearAllTranslations()
                }
            }) {
                Text("クリア")
            }
        }
    }
}
