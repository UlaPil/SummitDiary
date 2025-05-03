package com.example.summitdiary.network

data class HikePayload(
    val title: String,
    val date: String,
    val distance: Double,
    val time: String,
    val placeName: String,
    val placeGps: String,
    val gpxPath: String
)
