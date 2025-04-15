package com.example.summitdiary.database

import androidx.room.*

@Dao
interface HikePhotoDao {

    @Query("SELECT * FROM HikesPhotos")
    fun getAll(): List<HikePhoto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(link: HikePhoto)

    @Query("SELECT p.path FROM HikesPhotos hp INNER JOIN Photos p on p.photo_id = hp.photo_id WHERE hp.hike_id = :hikeId")
    fun getPhotosForHike(hikeId: Long): List<String>

    @Delete
    fun delete(link: HikePhoto)
}