package com.example.filemanager.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.filemanager.db.models.FileHashModel
import kotlinx.coroutines.flow.Flow

@Dao
interface HashDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHashes(items: List<FileHashModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHash(item: FileHashModel)

    @Update
    suspend fun updateHashes(item: FileHashModel)

    @Query("SELECT * FROM FileHashModel")
    fun selectHashes(): Flow<List<FileHashModel>>

    @Query("SELECT * FROM FileHashModel WHERE id = :id")
    fun selectHashById(id: Int): Flow<FileHashModel>

    @Query("SELECT * FROM FileHashModel WHERE hash = :hash")
    fun selectHashByHash(hash: String): Flow<FileHashModel?>
}