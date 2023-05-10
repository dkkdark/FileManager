package com.example.filemanager.db

import android.util.Log
import com.example.filemanager.db.dao.HashDao
import com.example.filemanager.db.models.FileHashModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val hashDao: HashDao
): DatabaseRepositoryInterface {

    override suspend fun insertHashes(items: List<FileHashModel>) {
        hashDao.insertHashes(items)
    }

    override suspend fun insertHash(item: FileHashModel) {
        val fileModel = hashDao.selectHashByHash(item.hash).first()
        if (fileModel != null) {
            hashDao.updateHashes(fileModel)
        }
        else {
            hashDao.insertHash(item)
        }
    }

    override fun updateHash(item: FileHashModel) {
        CoroutineScope(Dispatchers.IO).launch {
            hashDao.updateHashes(item)
        }
    }

    override fun getHashes() = hashDao.selectHashes()

    override fun getHash(id: Int) = hashDao.selectHashById(id)

}