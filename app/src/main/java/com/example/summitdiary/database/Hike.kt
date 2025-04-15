package com.example.summitdiary.database

import androidx.room.*

@Entity(tableName = "Hikes")
data class Hike(
    @PrimaryKey(autoGenerate = true) val hike_id: Long = 0,
    val title: String,
    val date: String,
    val distance: Int,
    val time: String,
    val place_id: Long,
    val gpx_path: String
)
