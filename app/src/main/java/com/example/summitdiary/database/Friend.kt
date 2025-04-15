package com.example.summitdiary.database

import androidx.room.*

@Entity(tableName = "Friends")
data class Friend(
    @PrimaryKey(autoGenerate = true) val friend_id: Long = 0,
    val name: String,
    val surname: String
)