package com.example.summitdiary.database

import androidx.room.*

@Dao
interface HikeFriendDao {

    @Query("SELECT * FROM HikesFriend")
    fun getAll(): List<HikeFriend>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(link: HikeFriend)

    @Query("SELECT f.* FROM HikesFriend hf INNER JOIN Friends f ON f.friend_id = hf.friend_id WHERE hike_id = :hikeId")
    fun getFriendsForHike(hikeId: Long): List<Friend>

    @Delete
    fun delete(link: HikeFriend)
}