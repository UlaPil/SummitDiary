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

    @Query("SELECT * FROM Places WHERE place_id = :id LIMIT 1")
    suspend fun getPlaceById(id: Long): Place

    @Query("SELECT * FROM Places WHERE isSynced = 0")
    suspend fun getUnsyncedPlaces(): List<Place>

    @Update
    suspend fun update(place: Place)

    @Delete
    fun delete(place: Place)
}