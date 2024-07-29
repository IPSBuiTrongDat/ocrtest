package com.example.ocrtest.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.ocrtest.data.TranslationEntity
import com.example.ocrtest.viewmodel.TranslationViewModel
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TranslateScreen(navController: NavController, viewModel: TranslationViewModel, recognizedText: String) {
    var inputText by remember { mutableStateOf(TextFieldValue(recognizedText.take(100))) }
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
    val showDownloadPopup = remember { mutableStateOf(false) }
    val showTranslationTimeoutDialog = remember { mutableStateOf(false) }
    val showSaveErrorDialog = remember { mutableStateOf(false) }
    val showSaveSuccessButton = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val textFieldWidth = screenWidth * 0.8f

    val clipboardManager = LocalClipboardManager.current

    fun isLanguageDownloaded(language: String, onResult: (Boolean) -> Unit) {
        val languageCode = when (language) {
            "日本語" -> TranslateLanguage.JAPANESE
            "英語" -> TranslateLanguage.ENGLISH
            "ベトナム語" -> TranslateLanguage.VIETNAMESE
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

    fun downloadLanguage(language: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val languageCode = when (language) {
            "日本語" -> TranslateLanguage.JAPANESE
            "英語" -> TranslateLanguage.ENGLISH
            "ベトナム語" -> TranslateLanguage.VIETNAMESE
            else -> TranslateLanguage.ENGLISH
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(languageCode)
            .setTargetLanguage(languageCode)
            .build()

        val translator = Translation.getClient(options)

        scope.launch {
            val downloadResult = withTimeoutOrNull(30000) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    translator.downloadModelIfNeeded()
                        .addOnSuccessListener {
                            continuation.resume(Unit) {}
                        }
                        .addOnFailureListener {
                            continuation.cancel()
                        }
                }
            }
            if (downloadResult == null) {
                onFailure()
            } else {
                onSuccess()
            }
        }
    }

    fun checkAndDownloadLanguages(onComplete: () -> Unit) {
        val languagesNeeded = listOf(
            when (sourceLanguage) {
                "日本語" -> "ja"
                "英語" -> "en"
                "ベトナム語" -> "vi"
                else -> "en"
            },
            when (targetLanguage) {
                "日本語" -> "ja"
                "英語" -> "en"
                "ベトナム語" -> "vi"
                else -> "en"
            }
        ).distinct()

        val languagesToDownload = mutableListOf<String>()

        var languagesChecked = 0
        languagesNeeded.forEach { language ->
            isLanguageDownloaded(language) { downloaded ->
                languagesChecked++
                if (!downloaded) {
                    languagesToDownload.add(language)
                }
                if (languagesChecked == languagesNeeded.size) {
                    if (languagesToDownload.isNotEmpty()) {
                        languagesNotDownloaded.value = languagesToDownload
                        totalLanguagesToDownload.value = languagesToDownload.size
                        downloadProgress.value = 0
                        isDownloading.value = false
                        showDownloadPopup.value = true
                    } else {
                        onComplete()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("翻訳画面") }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(textFieldWidth)
                        .verticalScroll(rememberScrollState())
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("認識された内容") },
                        modifier = Modifier
                            .padding(8.dp)
                            .height(150.dp)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                    )
                }
                IconButton(onClick = {
                    clipboardManager.setText(AnnotatedString(inputText.text))
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                }
            }

            // Output TextField
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(textFieldWidth)
                        .verticalScroll(rememberScrollState())
                ) {
                    TextField(
                        value = translatedText,
                        onValueChange = { translatedText = it },
                        placeholder = { Text("ここで翻訳される") },
                        modifier = Modifier
                            .padding(8.dp)
                            .height(150.dp)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                    )
                }
                IconButton(onClick = {
                    clipboardManager.setText(AnnotatedString(translatedText.text))
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                }
            }

            // Memo TextField
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(textFieldWidth)
                        .verticalScroll(rememberScrollState())
                ) {
                    TextField(
                        value = memo,
                        onValueChange = { memo = it },
                        placeholder = { Text("メモ") },
                        modifier = Modifier
                            .padding(8.dp)
                            .height(100.dp)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                    )
                }
            }

            // Category Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "分類",
                    fontSize = 20.sp,
                    modifier = Modifier.alignByBaseline()
                )
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
            Row(
                modifier = Modifier
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
                        val languages = listOf("日本語", "英語", "ベトナム語")
                        languages.forEach { language ->
                            DropdownMenuItem(
                                text = { Text(language) },
                                onClick = {
                                    sourceLanguage = language
                                    sourceLanguageExpanded = false
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
                        val languages = listOf("日本語", "英語", "ベトナム語")
                        languages.forEach { language ->
                            DropdownMenuItem(
                                text = { Text(language) },
                                onClick = {
                                    targetLanguage = language
                                    targetLanguageExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Translate Button
            Button(
                onClick = {
                    checkAndDownloadLanguages {
                        scope.launch {
                            val translationResult = withTimeoutOrNull(30000) {
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
                Text("翻訳")
            }

            // Save or Back to Home Button
            if (showSaveSuccessButton.value) {
                Button(
                    onClick = { navController.navigate("home") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("保存のみ。ホーム戻る")
                }
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val translationEntity = TranslationEntity(
                                    word = inputText.text,
                                    meaning = translatedText.text,
                                    type = category,
                                    memo = memo.text,
                                    importDay = Date()
                                )
                                viewModel.insertTranslation(translationEntity)
                                showSaveSuccessButton.value = true
                            } catch (e: Exception) {
                                showSaveErrorDialog.value = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("端末に保存")
                }
            }
        }

        // Download Popup
        if (showDownloadPopup.value) {
            Dialog(
                onDismissRequest = { showDownloadPopup.value = false },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (isDownloading.value) {
                            Text(
                                text = "ダウンロード中 (${downloadProgress.value}/${totalLanguagesToDownload.value})",
                                color = Color.Blue
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                showDownloadPopup.value = false
                                isDownloading.value = false
                            }) {
                                Text("キャンセル")
                            }
                        } else {
                            Text(text = "ダウンロードが必要です", color = Color.Red)
                            Button(onClick = {
                                isDownloading.value = true
                                languagesNotDownloaded.value.forEach { language ->
                                    downloadLanguage(language, onSuccess = {
                                        downloadProgress.value++
                                        if (downloadProgress.value == totalLanguagesToDownload.value) {
                                            isDownloading.value = false
                                            showDownloadPopup.value = false
                                        }
                                    }, onFailure = {
                                        isDownloading.value = false
                                        showErrorPopup.value = true
                                    })
                                }
                            }) {
                                Text("ダウンロード")
                            }
                            Button(onClick = {
                                showDownloadPopup.value = false
                            }) {
                                Text("キャンセル")
                            }
                        }
                    }
                }
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
                        Text(text = "ダウンロードに失敗しました。インターネット接続を確認してください。", color = Color.Red)
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

        // Save Error Dialog
        if (showSaveErrorDialog.value) {
            Dialog(
                onDismissRequest = { showSaveErrorDialog.value = false },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "保存に失敗しました", color = Color.Red)
                        Button(onClick = { showSaveErrorDialog.value = false }) {
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
        "ベトナム語" -> TranslateLanguage.VIETNAMESE
        else -> TranslateLanguage.JAPANESE
    }
    val targetLangCode = when (targetLanguage) {
        "日本語" -> TranslateLanguage.JAPANESE
        "英語" -> TranslateLanguage.ENGLISH
        "ベトナム語" -> TranslateLanguage.VIETNAMESE
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
