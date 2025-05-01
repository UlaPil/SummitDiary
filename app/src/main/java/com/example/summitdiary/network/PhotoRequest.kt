package com.example.summitdiary.network

data class PhotoRequest(
    val location: String?,
    val path: String,
    val userId: Int
)
