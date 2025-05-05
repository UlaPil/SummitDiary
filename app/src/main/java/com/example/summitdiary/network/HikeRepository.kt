package com.example.summitdiary.network

import androidx.room.Transaction
import com.example.summitdiary.database.Hike
import com.example.summitdiary.database.HikeDao
import com.example.summitdiary.database.HikePhoto
import com.example.summitdiary.database.HikePhotoDao
import com.example.summitdiary.database.Photo
import com.example.summitdiary.database.PhotoDao

class HikeRepository(
    private val hikeDao: HikeDao,
    private val photoDao: PhotoDao,
    private val hikePhotoDao: HikePhotoDao
) {
    @Transaction
    suspend fun insertHikeWithPhotos(hike: Hike, photos: List<Photo>) {
        val hikeId = hikeDao.insert(hike)
        photos.forEach { photo ->
            val photoId = photoDao.insert(photo)
            hikePhotoDao.insert(HikePhoto(hike_id = hikeId!!, photo_id = photoId!!))
        }
    }
}
