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
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ConfirmScreen(navController: NavController, photoUri: String) {
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
            val painter = rememberImagePainter(data = Uri.parse(photoUri))
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
                        recognizeText(navController, photoUri)
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Recognize")
                }
            }
        }
    }
}

private fun recognizeText(navController: NavController, photoUri: String) {
    val image = InputImage.fromFilePath(navController.context, Uri.parse(photoUri))
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            navController.navigate("translate/${visionText.text}")
        }
        .addOnFailureListener { e ->
            e.printStackTrace()
        }
}