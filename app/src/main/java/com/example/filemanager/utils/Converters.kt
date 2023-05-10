package com.example.filemanager.utils

import com.example.filemanager.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Converters {

    fun Long.determineFileSize() = when {
        this > 1024L * 1024L * 1024L -> "%.2f GB".format(this.toDouble() / (1024L * 1024L * 1024L))
        this > 1024 * 1024 -> "%.2f MB".format(this.toDouble() / (1024 * 1024))
        this > 1024 -> "%.2f KB".format(this.toDouble() / 1024)
        else -> "$this B"
    }

    fun Long.convertTime(): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(this))
    }

    fun File.findAppropriateDrawable() =
        if (this.isDirectory) R.drawable.folder
        else if (this.path.endsWith(".jpeg")) R.drawable.jpeg
        else if (this.path.endsWith(".jpg")) R.drawable.jpg
        else if (this.path.endsWith(".png")) R.drawable.png
        else if (this.path.endsWith(".txt")) R.drawable.txt
        else if (this.path.endsWith(".pdf")) R.drawable.pdf
        else if (this.path.endsWith(".docx")) R.drawable.docx
        else R.drawable.file

}