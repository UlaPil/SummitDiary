package com.example.summitdiary.database

import androidx.room.*

@Entity(
    tableName = "HikesPhotos",
    primaryKeys = ["hike_id", "photo_id"]
)
data class HikePhoto(
    val hike_id: Long,
    val photo_id: Long,
    var isSynced: Boolean = false
)