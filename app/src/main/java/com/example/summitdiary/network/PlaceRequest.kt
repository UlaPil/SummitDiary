package com.example.summitdiary.network

data class PlaceRequest(
    val name: String,
    val gps: String,
    val userId: Int
)
