package com.example.ocrtest.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
    var targetLanguage by remember { mutableStateOf("英語") }
    var targetLanguageExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var downloadStatus by remember { mutableStateOf<String?>(null) }
    var isDownloadComplete by remember { mutableStateOf(false) }
    var languagesToDownload by remember { mutableStateOf(emptyList<String>()) }
    var showExitDialog by remember { mutableStateOf(false) }

    val languages = listOf("英語", "ベトナム語")
    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val textFieldWidth = screenWidth * 0.8f

    // Register a BroadcastReceiver to monitor network changes
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkReceiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                val isConnected = networkCapabilities != null &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                if (!isConnected && isLoading) {
                    downloadStatus = "インターネットに接続していません"
                    isLoading = false
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(networkReceiver, filter)
        onDispose {
            context.unregisterReceiver(networkReceiver)
        }
    }

    Scaffold { paddingValues ->
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
            }

            // Language Selector
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
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
                    checkAndDownloadLanguages(context, "日本語", targetLanguage) { neededLanguages ->
                        if (neededLanguages.isEmpty()) {
                            translateText(
                                inputText.text,
                                "日本語",
                                targetLanguage
                            ) { translation ->
                                translatedText = TextFieldValue(translation)
                            }
                        } else {
                            languagesToDownload = neededLanguages
                            downloadStatus = "この言語をダウンロードしますか: ${neededLanguages.joinToString(", ")}"
                        }
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
                        Text(text = status, color = Color.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        if (status.contains("ダウンロードしますか")) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(onClick = {
                                    if (isNetworkAvailable(context)) {
                                        isLoading = true
                                        downloadStatus = "ダウンロード中"
                                        downloadLanguages(context, languagesToDownload) { success ->
                                            isLoading = false
                                            isDownloadComplete = success
                                            if (success) {
                                                saveDownloadedLanguages(context, languagesToDownload)
                                                downloadStatus = "ダウンロードに成功しました"
                                            } else {
                                                downloadStatus = "ダウンロードに失敗しました"
                                            }
                                        }
                                    } else {
                                        downloadStatus = "インターネットに接続していません"
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
                        } else {
                            Button(onClick = {
                                downloadStatus = null
                            }) {
                                Text("閉じる")
                            }
                        }
                    }
                }
            }
        }

        // Downloading Popup
        if (isLoading) {
            Dialog(
                onDismissRequest = { isLoading = false },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "ダウンロード中", color = Color.Blue)
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            isLoading = false
                            downloadStatus = null
                        }) {
                            Text("キャンセル")
                        }
                    }
                }
            }
        }

        // Download Complete Popup
        if (isDownloadComplete) {
            Dialog(
                onDismissRequest = { isDownloadComplete = false },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "ダウンロードに成功しました", color = Color.Black)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            isDownloadComplete = false
                            translateText(
                                inputText.text,
                                "日本語",
                                targetLanguage
                            ) { translation ->
                                translatedText = TextFieldValue(translation)
                            }
                        }) {
                            Text("翻訳")
                        }
                    }
                }
            }
        }

        // Download Failed Popup
        if (downloadStatus == "ダウンロードに失敗しました" || downloadStatus == "インターネットに接続していません") {
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
                        Text(text = downloadStatus!!, color = Color.Red)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            downloadStatus = null
                        }) {
                            Text("閉じる")
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
}

private fun checkAndDownloadLanguages(
    context: Context,
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

    val downloadedLanguages = loadDownloadedLanguages(context)
    val languagesToDownload = neededLanguages.filterNot { it in downloadedLanguages }

    onResult(languagesToDownload)
}

private fun downloadLanguages(
    context: Context,
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
                withTimeout(180000) {
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

private fun loadDownloadedLanguages(context: Context): List<String> {
    val sharedPreferences = context.getSharedPreferences("DownloadedLanguages", Context.MODE_PRIVATE)
    val languages = sharedPreferences.getStringSet("languages", setOf()) ?: setOf()
    return languages.toList()
}

private fun saveDownloadedLanguages(context: Context, languages: List<String>) {
    val sharedPreferences = context.getSharedPreferences("DownloadedLanguages", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val existingLanguages = sharedPreferences.getStringSet("languages", setOf()) ?: setOf()
    val updatedLanguages = existingLanguages.toMutableSet().apply { addAll(languages) }
    editor.putStringSet("languages", updatedLanguages)
    editor.apply()
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

private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    return networkCapabilities != null &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

