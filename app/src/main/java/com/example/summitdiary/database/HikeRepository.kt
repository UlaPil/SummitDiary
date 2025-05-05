package com.example.summitdiary.database

import kotlinx.coroutines.flow.Flow

class HikeRepository(private val hikeDao: HikeDao) {

    val allHikesWithPhotos: Flow<List<HikeWithPhotos>> = hikeDao.getHikesWithPhotos()

}