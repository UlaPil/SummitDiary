package com.example.summitdiary.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HikeDao {

    @Query("SELECT * FROM Hikes ORDER BY date DESC")
    fun getAll(): Flow<List<Hike>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(hike: Hike) : Long?

    @Transaction
    @Query("SELECT * FROM Hikes ORDER BY substr(date, 7, 4) || substr(date, 4, 2) || substr(date, 1, 2) || substr(date, 13, 2) || substr(date, 16, 2) DESC")
    fun getHikesWithPhotos(): Flow<List<HikeWithPhotos>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHikePhotoCrossRef(crossRef: HikePhoto)

    @Query("SELECT * FROM Hikes WHERE isSynced = 0")
    suspend fun getUnsyncedHikes(): List<Hike>

    @Query("UPDATE Hikes SET isSynced = 0")
    suspend fun resetAllHikesSyncedFlag()

    @Update
    suspend fun update(hike: Hike)

    @Query("UPDATE Hikes SET isSynced = 1 WHERE hike_id = :hikeId")
    suspend fun markAsSynced(hikeId: Long)

    @Delete
    suspend fun delete(hike: Hike)

    @Query("SELECT * FROM hikes WHERE hike_id = :id")
    suspend fun getHikeById(id: Long): Hike?

    @Query("SELECT * FROM Hikes WHERE place_id = :placeId")
    fun getHikesForPlace(placeId: Long): List<Hike>

    @Query("UPDATE Hikes SET place_id = :placeId WHERE hike_id = :hikeId")
    suspend fun updatePlaceId(hikeId: Long, placeId: Long)

}