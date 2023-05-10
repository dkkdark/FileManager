package com.example.filemanager.db

import com.example.filemanager.db.models.FileHashModel
import kotlinx.coroutines.flow.Flow


interface DatabaseRepositoryInterface {
    suspend fun insertHashes(items: List<FileHashModel>)
    suspend fun insertHash(item: FileHashModel)
    fun updateHash(item: FileHashModel)
    fun getHashes(): Flow<List<FileHashModel>>
    fun getHash(id: Int): Flow<FileHashModel>
}