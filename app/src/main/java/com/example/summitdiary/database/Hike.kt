package com.example.summitdiary.database

import androidx.room.*

@Entity(tableName = "Hikes")
data class Hike(
    @PrimaryKey(autoGenerate = true) val hike_id: Long = 0,
    var title: String,
    var date: String,
    var distance: Int,
    var time: String,
    var place_id: Long,
    var gpx_path: String
)