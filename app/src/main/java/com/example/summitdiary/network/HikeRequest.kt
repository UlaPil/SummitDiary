package com.example.summitdiary.network

data class HikeRequest(
    val title: String,
    val date: String,
    val distance: Double,
    val time: String,
    val placeId: Int,
    val gpxPath: String,
    val userId: Int
)
