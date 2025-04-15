package com.example.summitdiary.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HikeDao {

    @Query("SELECT * FROM Hikes")
    fun getAll(): Flow<List<Hike>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hike: Hike)

    @Update
    suspend fun update(hike: Hike)

    @Delete
    suspend fun delete(hike: Hike)
}