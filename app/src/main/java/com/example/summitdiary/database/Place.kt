package com.example.summitdiary.database

import androidx.room.*

@Entity(tableName = "Places")
data class Place(
    @PrimaryKey(autoGenerate = true) val place_id: Long = 0,
    val name: String,
    val gps: String
)