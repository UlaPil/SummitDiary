package com.example.summitdiary.database

import androidx.room.*

@Entity(tableName = "Photos")
data class Photo(
    @PrimaryKey(autoGenerate = true) val photo_id: Long = 0,
    val location: String?,
    val path: String,
    var isSynced: Boolean = false
)