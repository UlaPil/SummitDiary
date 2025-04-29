package com.example.summitdiary.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HikeDao {

    @Query("SELECT * FROM Hikes")
    fun getAll(): Flow<List<Hike>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(hike: Hike) : Long?

    @Transaction
    @Query("SELECT * FROM Hikes")
    fun getHikesWithPhotos(): Flow<List<HikeWithPhotos>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHikePhotoCrossRef(crossRef: HikePhoto)

    @Update
    suspend fun update(hike: Hike)

    @Delete
    suspend fun delete(hike: Hike)

    @Query("SELECT * FROM hikes WHERE hike_id = :id")
    suspend fun getHikeById(id: Long): Hike?
}