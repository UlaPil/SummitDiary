package com.example.summitdiary.database

import androidx.room.*

@Dao
interface PhotoDao {

    @Query("SELECT * FROM Photos")
    fun getAll(): List<Photo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(photo: Photo) : Long?

    @Query("SELECT * FROM Photos WHERE photo_id = :id")
    fun getById(id: Long): Photo?

    @Query("SELECT * FROM Photos WHERE isSynced = 0")
    suspend fun getUnsyncedPhotos(): List<Photo>

    @Update
    suspend fun update(photo: Photo)

    @Query("UPDATE Photos SET isSynced = 0")
    suspend fun resetAllPhotosSyncedFlag()

    @Delete
    fun delete(photo: Photo)

}