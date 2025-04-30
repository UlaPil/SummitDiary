package com.example.summitdiary.database

import androidx.room.*

@Dao
interface HikePhotoDao {

    @Query("SELECT * FROM HikesPhotos")
    fun getAll(): List<HikePhoto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(link: HikePhoto)

    @Query("""
        SELECT Photos.* FROM Photos
        INNER JOIN HikesPhotos ON HikesPhotos.photo_id = Photos.photo_id
        WHERE HikesPhotos.hike_id = :hikeId
    """)
    suspend fun getPhotosForHike(hikeId: Long): List<Photo>

    @Query("DELETE FROM HikesPhotos WHERE hike_id = :hikeId")
    suspend fun deleteByHikeId(hikeId: Long)

    @Delete
    fun delete(link: HikePhoto)
}