package com.example.ocrtest.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ocrtest.data.TranslationDao
import com.example.ocrtest.data.TranslationEntity
import com.example.ocrtest.repository.TranslationRepository
import com.example.ocrtest.viewmodel.TranslationViewModel
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TranslateScreen(navController: NavController, viewModel: TranslationViewModel, recognizedText: String) {
    var inputText by remember { mutableStateOf(TextFieldValue(recognizedText)) }
    var translatedText by remember { mutableStateOf(TextFieldValue("")) }
    var memo by remember { mutableStateOf(TextFieldValue("")) }
    var category by remember { mutableStateOf("") }
    var sourceLanguage by remember { mutableStateOf("日本語") }
    var targetLanguage by remember { mutableStateOf("英語") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var sourceLanguageExpanded by remember { mutableStateOf(false) }
    var targetLanguageExpanded by remember { mutableStateOf(false) }

    val categories = listOf("単語", "文章")
    val languages = listOf("日本語", "英語")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Translate Text") }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Input TextField
            Row(modifier = Modifier.fillMaxWidth()) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .height(100.dp)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                )
                IconButton(onClick = { /* Copy to clipboard logic */ }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                }
            }

            // Output TextField
            Row(modifier = Modifier.fillMaxWidth()) {
                BasicTextField(
                    value = translatedText,
                    onValueChange = { translatedText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .height(100.dp)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                )
                IconButton(onClick = { /* Copy to clipboard logic */ }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                }
            }

            // Memo TextField
            Row(modifier = Modifier.fillMaxWidth()) {
                BasicTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .height(50.dp)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                )
            }

            // Category Selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("分類")
                Box {
                    Button(onClick = { categoryExpanded = true }) {
                        Text(category.ifEmpty { "" })
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Category")
                    }
//                    DropdownMenu(
//                        expanded = categoryExpanded,
//                        onDismissRequest = { categoryExpanded = false }
//                    ) {
//                        categories.forEach { option ->
//                            DropdownMenuItem(onClick = {
//                                category = option
//                                categoryExpanded = false
//                            }) {
//                                Text(option)
//                            }
//                        }
//                    }
                    DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach{ option ->
                            DropdownMenuItem(text = { /*TODO*/ }, onClick = { /*TODO*/ })
                        }                     
                    }
                }
            }

            // Language Selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box {
                    Button(onClick = { sourceLanguageExpanded = true }) {
                        Text(sourceLanguage)
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Source Language")
                    }
                    DropdownMenu(
                        expanded = sourceLanguageExpanded,
                        onDismissRequest = { sourceLanguageExpanded = false }
                    ) {
                        languages.forEach { language ->
                            DropdownMenuItem(onClick = {
                                sourceLanguage = language
                                sourceLanguageExpanded = false
                            }) {
                                Text(language)
                            }
                        }
                    }
                }
                Box {
                    Button(onClick = { targetLanguageExpanded = true }) {
                        Text(targetLanguage)
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Target Language")
                    }
                    DropdownMenu(
                        expanded = targetLanguageExpanded,
                        onDismissRequest = { targetLanguageExpanded = false }
                    ) {
                        languages.forEach { language ->
                            DropdownMenuItem(onClick = {
                                targetLanguage = language
                                targetLanguageExpanded = false
                            }) {
                                Text(language)
                            }
                        }
                    }
                }
            }

            // Translate Button
            Button(
                onClick = {
                    translateText(
                        inputText.text,
                        sourceLanguage,
                        targetLanguage
                    ) { translation ->
                        translatedText = TextFieldValue(translation)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("翻訳")
            }

            // Save Button
            Button(
                onClick = {
                    val translationEntity = TranslationEntity(
                        word = inputText.text,
                        meaning = translatedText.text,
                        type = category,
                        memo = memo.text,
                        importDay = Date()
                    )
                    viewModel.insertTranslation(translationEntity)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("端末に保存")
            }
        }
    }
}

private fun translateText(
    text: String,
    sourceLanguage: String,
    targetLanguage: String,
    onResult: (String) -> Unit
) {
    val sourceLangCode = when (sourceLanguage) {
        "日本語" -> TranslateLanguage.JAPANESE
        "英語" -> TranslateLanguage.ENGLISH
        else -> TranslateLanguage.JAPANESE
    }
    val targetLangCode = when (targetLanguage) {
        "日本語" -> TranslateLanguage.JAPANESE
        "英語" -> TranslateLanguage.ENGLISH
        else -> TranslateLanguage.ENGLISH
    }

    val options = TranslatorOptions.Builder()
        .setSourceLanguage(sourceLangCode)
        .setTargetLanguage(targetLangCode)
        .build()

    val translator: Translator = Translation.getClient(options)

    translator.downloadModelIfNeeded()
        .addOnSuccessListener {
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    onResult(translatedText)
                }
                .addOnFailureListener { e ->
                    Log.e("TranslateScreen", "Translation failed", e)
                }
        }
        .addOnFailureListener { e ->
            Log.e("TranslateScreen", "Model download failed", e)
        }
}

@Preview(showBackground = true)
@Composable
fun PreviewTranslateScreen() {
    val navController = rememberNavController()
    val viewModel = TranslationViewModel(TranslationRepository(translationDao = TranslationDao))
    TranslateScreen(
        navController = navController,
        viewModel = viewModel,
        recognizedText = "Sample text"
    )
}