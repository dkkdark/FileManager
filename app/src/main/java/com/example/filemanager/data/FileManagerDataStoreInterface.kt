package com.example.filemanager.data

import kotlinx.coroutines.flow.Flow

interface FileManagerDataStoreInterface {
    val readFirstEntranceVal: Flow<Boolean>
    suspend fun saveFirstEntranceVal(value: Boolean)
}