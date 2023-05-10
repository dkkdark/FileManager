package com.example.filemanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.filemanager.utils.Converters.convertTime
import com.example.filemanager.utils.Converters.determineFileSize
import com.example.filemanager.utils.Converters.findAppropriateDrawable
import java.io.File

@Composable
fun PermissionWasNotGrantedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        text = { Text("If you decide to provide permission, do it manually in the settings") },
        confirmButton = {
            Button(onClick = { onDismiss() }) {
                Text(
                    text = "OK",
                    fontSize = 14.sp
                )
            }
        }
    )
}

@Composable
fun AskForPermissionDialog(text: String, onCancel: () -> Unit, onProvide: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onCancel() },
        properties = DialogProperties(dismissOnClickOutside = false),
        title = { Text("Glad to see you in our file manager!") },
        text = { Text(text) },
        confirmButton = {
            Button(onClick = { onProvide() }) {
                Text(
                    text = "Provide",
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            Button(onClick = { onCancel() }) {
                Text(
                    text = "Cancel",
                    fontSize = 14.sp
                )
            }
        }
    )
}

@Composable
fun InfoFileDialog(file: File, dismiss: () -> Unit) {
    val rows = listOf(
        "Name" to file.name,
        "Size" to file.length().determineFileSize(),
        "Date" to file.lastModified().convertTime(),
    )

    AlertDialog(
        onDismissRequest = { dismiss() },
        icon = {
            Image(painter = painterResource(id = file.findAppropriateDrawable()), contentDescription = null)
        },
        title = {
            Text(
                text = "File info",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                for (row in rows) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(0.dp, 8.dp)
                    ) {
                        Text(
                            text = row.first,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(80.dp)
                        )
                        Text(
                            text = row.second,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { dismiss() }) {
                Text(text = "Close")
            }
        }
    )
}


@Composable
fun NotFoundScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Sorry we cannot find files here :(",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}