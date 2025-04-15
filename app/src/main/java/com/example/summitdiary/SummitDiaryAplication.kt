package com.example.summitdiary

import android.app.Application
import com.example.summitdiary.database.AppDatabase
import com.example.summitdiary.database.HikeRepository

class SummitDiaryAplication: Application() {
    private val database by lazy {
        AppDatabase.getDatabase(this)
    }
    val hikeRepository by lazy {
        HikeRepository(database.hikeDao())
    }
}