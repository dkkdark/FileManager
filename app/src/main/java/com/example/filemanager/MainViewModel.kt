package com.example.filemanager

import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filemanager.data.FileManagerDataStoreInterface
import com.example.filemanager.db.DatabaseRepository
import com.example.filemanager.db.models.FileHashModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userEntranceDataStore: FileManagerDataStoreInterface,
    private val databaseRepository: DatabaseRepository
): ViewModel() {

    private val _changedFilesList = MutableStateFlow<Array<out File>?>(null)
    val changedFilesList = _changedFilesList.asStateFlow()

    // every time sort type or path to files change, we get new files and cort them
    fun getFiles(path: String?, selectedType: Int, selectedOrder: Int): Array<out File>? {
        val root = path.getRootFile()

        val type = when (selectedType) {
            1 -> TypeFilter.SortByDate
            2 -> TypeFilter.SortByExtension
            3 -> TypeFilter.SortBySize
            else -> TypeFilter.SortByName
        }
        val order = when (selectedOrder) {
            1 -> OrderFilter.ASC
            else -> OrderFilter.DESC
        }

        val files = root.listFiles()
        files.sortFiles(type, order)

        // at the same time sort files from changed files tab
        _changedFilesList.value.sortFiles(type, order)

        return files
    }

    @OptIn(FlowPreview::class)
    fun compareHashes(root: String) {
        val newAndChangedFiles = arrayListOf<File>()
        viewModelScope.launch {
            Log.d("qqq", "compareHashes start")
            val files = getAllFileHashes(root.getRootFile())
            val collectedList = databaseRepository.getHashes().first()
            val hashes = collectedList.map { it.hash }

            // For bugs prevent set debounce
            files.debounce(500).collect {
                it.forEach { map ->
                    if (map.value !in hashes && !newAndChangedFiles.contains(map.key)) {
                        Log.d("qqq", "changes files")
                        newAndChangedFiles.add(map.key)
                        _changedFilesList.value = newAndChangedFiles.toTypedArray()
                    }

                }
            }
            Log.d("qqq", "compareHashes finished")
        }
    }

    fun saveHashes(directory: String) {
        viewModelScope.launch {
            val root = directory.getRootFile()
            insertHashes(root)
            Log.d("qqq", "saveHashes finished")
        }
    }

    // insert hashes to db step by step
    private suspend fun insertHashes(directory: File){
        withContext(Dispatchers.IO) {
            directory.walkTopDown()
                .filter { it.isFile }
                .forEach { file ->
                    val hash = getMD5Hash(file)
                    databaseRepository.insertHash(FileHashModel(hash = hash))
                }
        }
    }

    // get hashes step by step and emit its to flow
    private suspend fun getAllFileHashes(directory: File) = flow {
         val hashes = mutableMapOf<File, String>()
         directory.walkTopDown()
             .filter { it.isFile }
             .forEach { file ->
                 val hash = getMD5Hash(file)
                 hashes[file] = hash
                 emit(hashes.toMap())
             }
    }

    // just get hash
    private suspend fun getMD5Hash(file: File): String {
        return withContext(Dispatchers.IO) {
            val md = MessageDigest.getInstance("MD5")
            file.inputStream().use { input ->
                val buffer = ByteArray(4096)
                var bytesRead = input.read(buffer)
                while (bytesRead != -1) {
                    md.update(buffer, 0, bytesRead)
                    bytesRead = input.read(buffer)
                }
            }
            val bytes = md.digest()
            bytes.joinToString("") { "%02x".format(it) }
        }
    }

    private fun Array<out File>?.sortFiles(type: TypeFilter, order: OrderFilter) {
        when (type) {
            TypeFilter.SortByName -> {
                this?.sortBy {
                    it.name
                }
            }
            TypeFilter.SortByDate -> {
                this?.sortBy {
                    it.lastModified()
                }
            }
            TypeFilter.SortByExtension -> {
                this?.sortBy {
                    it.extension
                }
            }
            TypeFilter.SortBySize -> {
                this?.sortBy {
                    it.length()
                }
            }
        }
        if (order == OrderFilter.DESC) this?.reverse()
    }

    private fun String?.getRootFile() = if (this.isNullOrEmpty())
        File(Environment.getExternalStorageDirectory().absolutePath)
    else
        File(this)

    fun saveFirstEntry() {
        viewModelScope.launch {
            userEntranceDataStore.saveFirstEntranceVal(false)
        }
    }

}

enum class TypeFilter {
    SortByName,
    SortByDate,
    SortByExtension,
    SortBySize
}

enum class OrderFilter {
    DESC,
    ASC
}