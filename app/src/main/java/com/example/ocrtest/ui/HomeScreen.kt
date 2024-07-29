package com.example.ocrtest.ui

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ホーム画面") }
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { navController.navigate("capture") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            Text("カメラで撮影")
                        }
//                        Button(
//                            onClick = { navController.navigate("database") },
//                            modifier = Modifier
//                                .weight(1f)
//                                .padding(8.dp)
//                        ) {
//                            Text("データ表示")
//                        }
                        Button(
                            onClick = { showExitDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            Text("終了")
                        }
                    }
                }
            }
        }
    )

    if (showExitDialog) {
        Dialog(
            onDismissRequest = { showExitDialog = false },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "本当に終了しますか？")
                    Row(
                        modifier = Modifier.padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                showExitDialog = false
                            },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("いいえ")
                        }
                        Button(
                            onClick = {
                                val activity = context as? Activity
                                activity?.finish()
                            },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("はい")
                        }
                    }
                }
            }
        }
    }
}
