package com.example.filemanager.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.filemanager.db.dao.HashDao
import com.example.filemanager.db.models.FileHashModel

@Database(entities = [FileHashModel::class], version = 1)
abstract class FileManagerDatabase: RoomDatabase() {

    abstract fun hashDao(): HashDao

    companion object {
        const val DATABASE_NAME = "FileManagerDatabase"
    }
}