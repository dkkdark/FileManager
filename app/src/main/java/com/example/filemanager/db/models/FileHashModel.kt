package com.example.filemanager.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FileHashModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hash: String
)
