package com.example.summitdiary.network

data class HikeDto(
    val id: Int,
    val title: String,
    val date: String,
    val distance: Double,
    val time: String,
    val place: String,
    val gpxPath: String,
    val userId: Int
)