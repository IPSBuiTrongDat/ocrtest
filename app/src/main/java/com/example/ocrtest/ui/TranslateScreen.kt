package com.example.ocrtest.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ocrtest.data.TranslationEntity
import com.example.ocrtest.viewmodel.TranslationViewModel
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
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

    val languagesNotDownloaded = remember { mutableStateOf(listOf<String>()) }
    val downloadProgress = remember { mutableStateOf(0) }
    val totalLanguagesToDownload = remember { mutableStateOf(0) }
    val isDownloading = remember { mutableStateOf(false) }
    val showErrorPopup = remember { mutableStateOf(false) }
    val showTranslationTimeoutDialog = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val clipboardManager = LocalClipboardManager.current

    // Hàm để kiểm tra và tải các gói ngôn ngữ
    fun checkAndDownloadLanguages() {
        val languagesNeeded = listOf(
            when (sourceLanguage) {
                "日本語" -> "ja"
                "英語" -> "en"
                "Tiếng Việt" -> "vi"
                else -> "en"
            },
            when (targetLanguage) {
                "日本語" -> "ja"
                "英語" -> "en"
                "Tiếng Việt" -> "vi"
                else -> "en"
            }
        ).distinct()

        val languagesToDownload = mutableListOf<String>()

        languagesNeeded.forEach { language ->
            isLanguageDownloaded(language) { downloaded ->
                if (!downloaded) {
                    languagesToDownload.add(language)
                }
            }
        }

        if (languagesToDownload.isNotEmpty()) {
            languagesNotDownloaded.value = languagesToDownload
            totalLanguagesToDownload.value = languagesToDownload.size
            downloadProgress.value = 0
            isDownloading.value = true

            languagesToDownload.forEach { language ->
                scope.launch {
                    downloadLanguage(language, onSuccess = {
                        downloadProgress.value++
                        if (downloadProgress.value == totalLanguagesToDownload.value) {
                            isDownloading.value = false
                        }
                    }, onFailure = {
                        isDownloading.value = false
                        showErrorPopup.value = true
                    })
                }
            }
        }
    }

    // Hàm kiểm tra xem ngôn ngữ đã được tải chưa
    fun isLanguageDownloaded(language: String, onResult: (Boolean) -> Unit) {
        val languageCode = when (language) {
            "ja" -> TranslateLanguage.JAPANESE
            "en" -> TranslateLanguage.ENGLISH
            "vi" -> TranslateLanguage.VIETNAMESE
            else -> TranslateLanguage.ENGLISH
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(languageCode)
            .setTargetLanguage(languageCode)
            .build()

        val translator = Translation.getClient(options)

        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    // Hàm để tải ngôn ngữ
    suspend fun downloadLanguage(language: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val languageCode = when (language) {
            "ja" -> TranslateLanguage.JAPANESE
            "en" -> TranslateLanguage.ENGLISH
            "vi" -> TranslateLanguage.VIETNAMESE
            else -> TranslateLanguage.ENGLISH
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(languageCode)
            .setTargetLanguage(languageCode)
            .build()

        val translator = Translation.getClient(options)

        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Translate Text") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Input TextField
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .padding(8.dp)
                            .height(150.dp)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                    )
                    if (inputText.text.isEmpty()) {
                        Text(
                            text = "Enter text here...",
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                IconButton(onClick = {
                    val clip = ClipData.newPlainText("Copied Text", inputText.text)
                    clipboardManager.setText(clip)
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                }
            }

            // Output TextField
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = translatedText,
                        onValueChange = { translatedText = it },
                        modifier = Modifier
                            .padding(8.dp)
                            .height(150.dp)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                    )
                    if (translatedText.text.isEmpty()) {
                        Text(
                            text = "Translated text will appear here...",
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                IconButton(onClick = {
                    val clip = ClipData.newPlainText("Copied Text", translatedText.text)
                    clipboardManager.setText(clip)
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                }
            }

            // Memo TextField
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = memo,
                        onValueChange = { memo = it },
                        modifier = Modifier
                            .padding(8.dp)
                            .height(100.dp)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                    )
                    if (memo.text.isEmpty()) {
                        Text(
                            text = "Enter memo here...",
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // Category Selector
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("分類", modifier = Modifier.alignByBaseline())
                Box {
                    Button(onClick = { categoryExpanded = true }) {
                        Text(category.ifEmpty { "選択" })
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Category")
                    }
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        val categories = listOf("単語", "文章")
                        categories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    category = option
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Language Selector
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box {
                    Button(onClick = { sourceLanguageExpanded = true }) {
                        Text(sourceLanguage)
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Source Language")
                    }
                    DropdownMenu(
                        expanded = sourceLanguageExpanded,
                        onDismissRequest = { sourceLanguageExpanded = false }
                    ) {
                        val languages = listOf("日本語", "英語", "Tiếng Việt")
                        languages.forEach { language ->
                            DropdownMenuItem(
                                text = { Text(language) },
                                onClick = {
                                    sourceLanguage = language
                                    sourceLanguageExpanded = false
                                    checkAndDownloadLanguages()
                                }
                            )
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
                        val languages = listOf("日本語", "英語", "Tiếng Việt")
                        languages.forEach { language ->
                            DropdownMenuItem(
                                text = { Text(language) },
                                onClick = {
                                    targetLanguage = language
                                    targetLanguageExpanded = false
                                    checkAndDownloadLanguages()
                                }
                            )
                        }
                    }
                }
            }

            // Error message if not all packages are downloaded
            if (languagesNotDownloaded.value.isNotEmpty()) {
                Text(
                    text = "必要な言語パッケージをダウンロードしてください。",
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Translate or Download Button
            Button(
                onClick = {
                    if (isDownloading.value) {
                        checkAndDownloadLanguages()
                    } else {
                        scope.launch {
                            val translationResult = withTimeoutOrNull(60000) {
                                translateText(
                                    inputText.text,
                                    sourceLanguage,
                                    targetLanguage
                                ) { translation ->
                                    translatedText = TextFieldValue(translation)
                                }
                            }
                            if (translationResult == null) {
                                showTranslationTimeoutDialog.value = true
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(if (isDownloading.value) "ダウンロード (${downloadProgress.value}/${totalLanguagesToDownload.value})" else "翻訳")
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

        // Error Dialog
        if (showErrorPopup.value) {
            Dialog(
                onDismissRequest = { showErrorPopup.value = false },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "インターネット接続を確認し、もう一度お試しください。", color = Color.Red)
                        Button(onClick = { showErrorPopup.value = false }) {
                            Text("閉じる")
                        }
                    }
                }
            }
        }

        // Translation Timeout Dialog
        if (showTranslationTimeoutDialog.value) {
            Dialog(
                onDismissRequest = { showTranslationTimeoutDialog.value = false },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "翻訳に失敗しました", color = Color.Red)
                        Button(onClick = { showTranslationTimeoutDialog.value = false }) {
                            Text("閉じる")
                        }
                    }
                }
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
        "Tiếng Việt" -> TranslateLanguage.VIETNAMESE
        else -> TranslateLanguage.JAPANESE
    }
    val targetLangCode = when (targetLanguage) {
        "日本語" -> TranslateLanguage.JAPANESE
        "英語" -> TranslateLanguage.ENGLISH
        "Tiếng Việt" -> TranslateLanguage.VIETNAMESE
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
