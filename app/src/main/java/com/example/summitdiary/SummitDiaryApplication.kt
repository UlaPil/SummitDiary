package com.example.summitdiary

import android.app.Application
import com.example.summitdiary.database.AppDatabase
import com.example.summitdiary.database.HikeRepository

class SummitDiaryApplication : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context = this)
    }

    val hikeRepository by lazy {
        HikeRepository(database.hikeDao())
    }
}