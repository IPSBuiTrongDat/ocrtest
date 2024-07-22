package com.example.ocrtest

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.ocrtest.data.TranslationDatabase
import com.example.ocrtest.repository.TranslationRepository
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

        // Tạo database và repository
        val database = Room.databaseBuilder(applicationContext, TranslationDatabase::class.java, "translation_database").build()
        val repository = TranslationRepository(database.translationDao())
        val viewModel: TranslationViewModel by viewModels { TranslationViewModelFactory(repository) }

        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "home") {
                composable("home") { HomeScreen(navController) }
                composable("capture") { CaptureScreen(navController) }
                composable("confirm/{photoUri}") { backStackEntry ->
                    ConfirmScreen(navController, backStackEntry.arguments?.getString("photoUri")!!)
                }
                composable("translate/{recognizedText}") { backStackEntry ->
                    TranslateScreen(navController, viewModel, backStackEntry.arguments?.getString("recognizedText")!!)
                }
                composable("view_database") { DatabaseScreen(viewModel) }
            }
        }
    }
}