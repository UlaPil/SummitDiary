package com.example.summitdiary.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.summitdiary.SummitDiaryApplication
import com.example.summitdiary.database.HikeRepository
import com.example.summitdiary.database.HikeWithPhotos
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HikeRepository

    val hikesWithPhotos: LiveData<List<HikeWithPhotos>>

    init {
        val app = application as SummitDiaryApplication
        repository = app.hikeRepository
        hikesWithPhotos = repository.allHikesWithPhotos.asLiveData()
    }
}
