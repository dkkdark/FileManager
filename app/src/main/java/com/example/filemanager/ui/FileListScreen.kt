package com.example.filemanager.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filemanager.utils.Converters.findAppropriateDrawable
import java.io.File

@Composable
fun FilesList(modifier: Modifier = Modifier,
              files: Array<out File>,
              editMode: File?,
              onClick: (path: String, file: File) -> Unit,
              onLongClick: (file: File) -> Unit) {
    LazyVerticalGrid(
        modifier = modifier.padding(8.dp),
        columns = GridCells.Adaptive(minSize = 75.dp)
    ) {
        items(files.size) { idx ->
            val file = files[idx]
            FileItem(file, editMode == file,
                onClick = {
                    onClick(it, file)
                },
                onLongClick = {
                    onLongClick(file)
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItem(file: File, isEditMode: Boolean, onClick: (path: String) -> Unit, onLongClick: () -> Unit) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .combinedClickable(
                onClick = { onClick(file.absolutePath) },
                onLongClick = {
                    if (!file.isDirectory) {
                        onLongClick()
                    }
                }
            )
            .background(if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = file.findAppropriateDrawable()),
            contentDescription = "file image"
        )
        Text(
            text = file.name,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}