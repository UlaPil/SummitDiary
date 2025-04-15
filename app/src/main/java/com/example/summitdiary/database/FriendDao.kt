package com.example.summitdiary.database

import androidx.room.*

@Dao
interface FriendDao {

    @Query("SELECT * FROM Friends")
    fun getAll(): List<Friend>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(friend: Friend) : Long

    @Query("SELECT * FROM Friends WHERE friend_id = :id")
    fun getById(id: Long): Friend?

    @Delete
    fun delete(friend: Friend)
}