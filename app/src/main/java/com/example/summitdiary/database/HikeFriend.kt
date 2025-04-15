package com.example.summitdiary.database

import androidx.room.*

@Entity(
    tableName = "HikesFriend",
    primaryKeys = ["hike_id", "friend_id"]
)
data class HikeFriend(
    val hike_id: Long,
    val friend_id: Long
)