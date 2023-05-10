package com.example.filemanager.di

import android.content.Context
import androidx.room.Room
import com.example.filemanager.db.DatabaseRepository
import com.example.filemanager.db.FileManagerDatabase
import com.example.filemanager.db.FileManagerDatabase.Companion.DATABASE_NAME
import com.example.filemanager.db.dao.HashDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FileManagerDatabase =
        Room.databaseBuilder(
            context,
            FileManagerDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideDao(database: FileManagerDatabase): HashDao = database.hashDao()

    @Provides
    @Singleton
    fun provideDatabaseRepository(hashDao: HashDao): DatabaseRepository =
        DatabaseRepository(hashDao)

}