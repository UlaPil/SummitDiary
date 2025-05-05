package com.example.summitdiary.network

data class HikeDetailDto(
    val id: Int,
    val title: String,
    val date: String,
    val distance: Double,
    val time: String,
    val placeName: String,
    val placeGps: String,
    val gpxPath: String,
    val photos: List<PhotoDto>
)

data class PhotoDto(
    val id: Int,
    val path: String,
    val location: String?
)
