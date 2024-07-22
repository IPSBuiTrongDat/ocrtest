package com.example.ocrtest.ui

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmScreen(navController: NavController, photoUri: String) {
    val decodedUri = Uri.parse(Uri.decode(photoUri))
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Photo") }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
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
                    Text("Retake")
                }
                Button(
                    onClick = {
                        recognizeText(navController, decodedUri)
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Recognize")
                }
            }
        }
    }
}

//private fun recognizeText(navController: NavController, photoUri: Uri) {
//    val image = InputImage.fromFilePath(navController.context, photoUri)
//    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS.)
//
//    recognizer.process(image)
//        .addOnSuccessListener { visionText ->
//            navController.navigate("translate/${Uri.encode(visionText.text)}")
//        }
//        .addOnFailureListener { e ->
//            e.printStackTrace()
//        }
//}
private fun recognizeText(navController: NavController, photoUri: Uri) {
    val image = InputImage.fromFilePath(navController.context, photoUri)

    // Recognizer cho Latin
    val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())

    // Recognizer cho Nhật
    val japaneseRecognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

    // Process Latin text
    latinRecognizer.process(image)
        .addOnSuccessListener { visionText ->
            navController.navigate("translate/${Uri.encode(visionText.text)}")
        }
        .addOnFailureListener { e ->
            e.printStackTrace()
        }

    // Process Japanese text
    japaneseRecognizer.process(image)
        .addOnSuccessListener { visionText ->
            navController.navigate("translate/${Uri.encode(visionText.text)}")
        }
        .addOnFailureListener { e ->
            e.printStackTrace()
        }
}