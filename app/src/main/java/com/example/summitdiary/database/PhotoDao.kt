package com.example.summitdiary.database

import androidx.room.*

@Dao
interface PhotoDao {

    @Query("SELECT * FROM Photos")
    fun getAll(): List<Photo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: Photo)

    @Query("SELECT * FROM Photos WHERE photo_id = :id")
    fun getById(id: Long): Photo?

    @Delete
    fun delete(photo: Photo)

}