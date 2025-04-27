package com.example.summitdiary.database

import androidx.room.*

@Entity(tableName = "Hikes")
data class Hike(
    @PrimaryKey(autoGenerate = true) val hike_id: Long = 0,
    var title: String,
    var date: String,
    val distance: Int,
    var time: String,
    val place_id: Long,
    val gpx_path: String
)