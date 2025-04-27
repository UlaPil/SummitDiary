package com.example.summitdiary.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.summitdiary.SummitDiaryApplication
import com.example.summitdiary.database.Hike
import com.example.summitdiary.database.HikeRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HikeRepository

    val hikes: LiveData<List<Hike>>

    init {
        val app = application as SummitDiaryApplication
        repository = app.hikeRepository
        hikes = repository.allHikes.asLiveData()
    }

    fun addHike(hike: Hike) {
        viewModelScope.launch {
            repository.insertHike(hike)
        }
    }
}

//class HomeViewModel(private val hikeRepository: HikeRepository) : ViewModel() {
//    var hikes: LiveData<List<Hike>> = hikeRepository.allHikes.asLiveData()
//
//    fun addHike(newHike: Hike) = viewModelScope.launch {
//        hikeRepository.insertHike(newHike)
//    }
//
//    fun updateHike(hike: Hike) = viewModelScope.launch {
//        hikeRepository.updateHike(hike)
//    }
//}
//
//class HikeModelFactory(private val repository: HikeRepository): ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(HomeViewModel::class.java))
//            return HomeViewModel(repository) as T
//
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}