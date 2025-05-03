package com.example.summitdiary.network

data class PhotoPayload(
    val localPhotoId: Long,
    val location: String,
    val path: String
)
