package com.example.summitdiary.database

import androidx.room.*

@Dao
interface PlaceDao {

    @Query("SELECT * FROM Places")
    fun getAll(): List<Place>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(place: Place) : Long

    @Query("SELECT name FROM Places WHERE place_id = :id")
    fun getById(id: Long): String?

    @Delete
    fun delete(place: Place)
}