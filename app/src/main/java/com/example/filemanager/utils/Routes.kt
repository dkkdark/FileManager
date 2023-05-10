package com.example.filemanager.utils

sealed class Routes(val route: String) {
    object MainScreen : Routes("main_screen")
    object Welcome : Routes("welcome")
}