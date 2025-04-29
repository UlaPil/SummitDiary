package com.example.summitdiary.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.summitdiary.SummitDiaryApplication
import com.example.summitdiary.database.Hike
import com.example.summitdiary.database.HikeRepository
import com.example.summitdiary.database.HikeWithPhotos
import com.example.summitdiary.database.Photo
import kotlinx.coroutines.launch
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HikeRepository

    val hikesWithPhotos: LiveData<List<HikeWithPhotos>>

    init {
        val app = application as SummitDiaryApplication
        repository = app.hikeRepository
        hikesWithPhotos = repository.allHikesWithPhotos.asLiveData()
    }
}
//    fun addPhoto(photo: Photo, hikeId: Long) {
//        viewModelScope.launch {
//            val photoId = repository.insertPhoto(photo)
//            repository.insertHikePhoto(hikeId, photoId)
//        }
//    }
//}

//fun addHike(hike: Hike) {
//    viewModelScope.launch {
//        repository.insertHike(hike)
//    }
//}