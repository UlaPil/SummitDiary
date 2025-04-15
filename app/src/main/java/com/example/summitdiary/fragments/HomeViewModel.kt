package com.example.summitdiary.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.summitdiary.database.Hike
import com.example.summitdiary.database.HikeRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val hikeRepository: HikeRepository) : ViewModel() {
    var hikes: LiveData<List<Hike>> = hikeRepository.allHikes.asLiveData()

    fun addHike(newHike: Hike) = viewModelScope.launch {
        hikeRepository.insertHike(newHike)
    }

    fun updateHike(hike: Hike) = viewModelScope.launch {
        hikeRepository.updateHike(hike)
    }
}

class HikeModelFactory(private val repository: HikeRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java))
            return HomeViewModel(repository) as T

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}