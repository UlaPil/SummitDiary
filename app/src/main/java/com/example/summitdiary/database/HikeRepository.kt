package com.example.summitdiary.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class HikeRepository(private val hikeDao: HikeDao) {
    val allHikes: Flow<List<Hike>> = hikeDao.getAll()

    @WorkerThread
    suspend fun insertHike(hike: Hike) {
        hikeDao.insert(hike)
    }

    @WorkerThread
    suspend fun updateHike(hike: Hike) {
        hikeDao.update(hike)
    }
}