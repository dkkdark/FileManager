package com.example.filemanager.di

import com.example.filemanager.data.FileManagerDataStore
import com.example.filemanager.data.FileManagerDataStoreInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSaveModule {

    @Singleton
    @Binds
    abstract fun bindFileManagerDataStoreState(state: FileManagerDataStore): FileManagerDataStoreInterface
}