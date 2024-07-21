package com.example.ocrtest.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ocrtest.viewmodel.TranslationViewModel
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateScreen(navController: NavController, viewModel: TranslationViewModel, recognizedText: String) {
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
            var translatedText by remember { mutableStateOf("") }

            LaunchedEffect(recognizedText) {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(TranslateLanguage.VIETNAMESE)
                    .build()
                val translator: Translator = com.google.mlkit.nl.translate.Translation.getClient(options)

                translator.downloadModelIfNeeded()
                    .addOnSuccessListener {
                        translator.translate(recognizedText)
                            .addOnSuccessListener { translation ->
                                translatedText = translation
                            }
                            .addOnFailureListener { e ->
                                Log.e("TranslateScreen", "Translation failed", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("TranslateScreen", "Model download failed", e)
                    }
            }

            Text(
                text = recognizedText,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = translatedText,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.insertTranslation(recognizedText, translatedText)
                    navController.navigate("view_database") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Translation")
            }
        }
    }
}