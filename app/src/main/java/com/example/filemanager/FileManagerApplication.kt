package com.example.filemanager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FileManagerApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }
}