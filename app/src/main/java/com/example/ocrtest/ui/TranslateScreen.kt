package com.example.ocrtest.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TranslateScreen(navController: NavController, viewModel: TranslationViewModel, recognizedText: String) {
    var inputText by remember { mutableStateOf(TextFieldValue(recognizedText)) }
    var translatedText by remember { mutableStateOf(TextFieldValue("")) }
    var sourceLanguage by remember { mutableStateOf("日本語") }
    var targetLanguage by remember { mutableStateOf("英語") }
    var sourceLanguageExpanded by remember { mutableStateOf(false) }
    var targetLanguageExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var downloadStatus by remember { mutableStateOf<String?>(null) }
    var isDownloadComplete by remember { mutableStateOf(false) }
    var languagesToDownload by remember { mutableStateOf(emptyList<String>()) }
    var showExitDialog by remember { mutableStateOf(false) }

    val languages = listOf("日本語", "英語", "ベトナム語")
    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val textFieldWidth = screenWidth * 0.8f

    val clipboardManager = LocalClipboardManager.current

    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Translate Text") }
//            )
//        }
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

            // Language Selector
//            Row(modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 8.dp),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Box {
//                    Button(onClick = { sourceLanguageExpanded = true }) {
//                        Text(sourceLanguage)
//                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Source Language")
//                    }
//                    DropdownMenu(
//                        expanded = sourceLanguageExpanded,
//                        onDismissRequest = { sourceLanguageExpanded = false }
//                    ) {
//                        languages.forEach { language ->
//                            DropdownMenuItem(
//                                text = { Text(language) },
//                                onClick = {
//                                    sourceLanguage = language
//                                    sourceLanguageExpanded = false
//                                }
//                            )
//                        }
//                    }
//                }
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
                    checkAndDownloadLanguages(context, sourceLanguage, targetLanguage) { neededLanguages ->
                        languagesToDownload = neededLanguages
                        downloadStatus = "この言語をダウンロードしますか。"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("翻訳")
            }

            // Re-capture and Complete Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { navController.navigate("capture") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("再撮影")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { showExitDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("終了")
                }
            }
        }

        // Download Popup
        downloadStatus?.let { status ->
            Dialog(
                onDismissRequest = { downloadStatus = null },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (isLoading) {
                            Text(text = "ダウンロード中: ${languagesToDownload.joinToString(", ")}", color = Color.Blue)
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                downloadStatus = null
                                isLoading = false
                            }) {
                                Text("キャンセル")
                            }
                        } else if (isDownloadComplete) {
                            Text(text = "ダウンロードしました", color = Color.Green)
                            Button(onClick = {
                                downloadStatus = null
                                translateText(
                                    inputText.text,
                                    sourceLanguage,
                                    targetLanguage
                                ) { translation ->
                                    translatedText = TextFieldValue(translation)
                                }
                            }) {
                                Text("翻訳")
                            }
                        } else {
                            Text(text = status, color = Color.Red)
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(onClick = {
                                    isLoading = true
                                    downloadLanguages(context, languagesToDownload) { success ->
                                        isLoading = false
                                        isDownloadComplete = success
                                        if (success) {
                                            saveDownloadedLanguages(context, languagesToDownload)
                                        } else {
                                            downloadStatus = "ダウンロードに失敗しました"
                                        }
                                    }
                                }) {
                                    Text("はい")
                                }
                                Button(onClick = {
                                    downloadStatus = null
                                }) {
                                    Text("いいえ")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Exit Confirmation Dialog
        if (showExitDialog) {
            Dialog(
                onDismissRequest = { showExitDialog = false },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "アプリを終了しますか？", color = Color.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(onClick = {
                                showExitDialog = false
                                // Code to exit the app
                                (context as? Activity)?.finish()
                            }) {
                                Text("はい")
                            }
                            Button(onClick = { showExitDialog = false }) {
                                Text("いいえ")
                            }
                        }
                    }
                }
            }
        }
    }
//}

private fun checkAndDownloadLanguages(
    context: android.content.Context,
    sourceLanguage: String,
    targetLanguage: String,
    onResult: (List<String>) -> Unit
) {
    val neededLanguages = listOf(
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

    onResult(neededLanguages)
}

private fun downloadLanguages(
    context: android.content.Context,
    languages: List<String>,
    onComplete: (Boolean) -> Unit
) {
    var successCount = 0
    val scope = CoroutineScope(Dispatchers.IO)

    for (language in languages) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.fromLanguageTag(language)!!)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()

        val translator: Translator = Translation.getClient(options)

        scope.launch {
            try {
                withTimeout(60000) {
                    translator.downloadModelIfNeeded().await()
                }
                successCount++
                if (successCount == languages.size) {
                    withContext(Dispatchers.Main) {
                        onComplete(true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }
}

private fun saveDownloadedLanguages(context: android.content.Context, languages: List<String>) {
    val sharedPreferences = context.getSharedPreferences("DownloadedLanguages", android.content.Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    languages.forEach { language ->
        editor.putBoolean(language, true)
    }
    editor.apply()
}

private fun translat
eText(
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
