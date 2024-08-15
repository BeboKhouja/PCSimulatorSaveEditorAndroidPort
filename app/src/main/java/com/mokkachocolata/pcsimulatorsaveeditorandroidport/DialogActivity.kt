package com.mokkachocolata.pcsimulatorsaveeditorandroidport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mokkachocolata.pcsimulatorsaveeditorandroidport.ui.theme.PCSimulatorSaveEditorTheme

class DialogActivity : ComponentActivity() {
    val version = MainActivity2().version

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PCSimulatorSaveEditorTheme {

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialog(
    dialogTitle: String,
    dialogText: String,
) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.Info, contentDescription = "About")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
        },
        confirmButton = {

        },
        dismissButton = {
            TextButton(
                onClick = {
                }
            ) {
                Text("Ok")
            }
        }
    )
}