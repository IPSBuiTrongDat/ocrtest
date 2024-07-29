package com.example.ocrtest.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmScreen(navController: NavController, photoUri: String) {
    val decodedUri = Uri.parse(Uri.decode(photoUri))
    var showErrorDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("確認画面") }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val painter = rememberImagePainter(data = decodedUri)
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("再撮影")
                }
                Button(
                    onClick = {
                        scope.launch {
                            recognizeText(navController, decodedUri, onError = {
                                showErrorDialog = true
                                Log.e("ConfirmScreen", "Unable to detect text")
                            })
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("テキスト認識")
                }
            }
        }
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text(text = "認識失敗しました。再撮影しますか？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showErrorDialog = false
                            navController.popBackStack()
                        }
                    ) {
                        Text("はい")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showErrorDialog = false }
                    ) {
                        Text("いいえ")
                    }
                }
            )
        }
    }
}

private suspend fun recognizeText(navController: NavController, photoUri: Uri, onError: () -> Unit) {
    val image = InputImage.fromFilePath(navController.context, photoUri)
    val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())
    val japaneseRecognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

    var textRecognized = false

    val recognitionJob = CoroutineScope(Dispatchers.IO).launch {
        // Process Latin text
        latinRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isNotEmpty()) {
                    textRecognized = true
                    val text = visionText.text.take(100) // Lấy 100 ký tự đầu tiên
                    navController.navigate("translate/${Uri.encode(text)}")
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }

        // Process Japanese text
        japaneseRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isNotEmpty()) {
                    textRecognized = true
                    val text = visionText.text.take(100) // Lấy 100 ký tự đầu tiên
                    navController.navigate("translate/${Uri.encode(text)}")
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }

        delay(5000)

        // If text not recognized after 5 seconds, show error
        if (!textRecognized) {
            withContext(Dispatchers.Main) {
                onError()
            }
        }
    }

    delay(5000)
    if (recognitionJob.isActive) {
        recognitionJob.cancel()
        withContext(Dispatchers.Main) {
            onError()
        }
    }
}