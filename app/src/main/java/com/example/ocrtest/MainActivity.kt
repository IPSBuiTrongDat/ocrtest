package com.example.ocrtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.ocrtest.data.TranslationDatabase
import com.example.ocrtest.ui.CaptureScreen
import com.example.ocrtest.ui.ConfirmScreen
import com.example.ocrtest.ui.DatabaseScreen
import com.example.ocrtest.ui.HomeScreen
import com.example.ocrtest.ui.TranslateScreen
import com.example.ocrtest.viewmodel.TranslationViewModel
import com.example.ocrtest.viewmodel.TranslationViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = Room.databaseBuilder(
            applicationContext,
            TranslationDatabase::class.java,
            "translation_database"
        ).build()
        val dao = database.translationDao()
        val viewModel: TranslationViewModel by viewModels { TranslationViewModelFactory(dao) }

        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "home") {
                composable("home") { HomeScreen(navController) }
                composable("capture") { CaptureScreen(navController) }
                composable("confirm/{photoUri}") { backStackEntry ->
                    val photoUri = backStackEntry.arguments?.getString("photoUri") ?: ""
                    ConfirmScreen(navController, photoUri)
                }
                composable("translate/{recognizedText}") { backStackEntry ->
                    val recognizedText = backStackEntry.arguments?.getString("recognizedText") ?: ""
                    TranslateScreen(navController, viewModel, recognizedText)
                }
                composable("view_database") { DatabaseScreen(viewModel) }
            }
        }
    }
}
