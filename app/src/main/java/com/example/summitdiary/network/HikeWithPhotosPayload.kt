package com.example.summitdiary.network

data class HikeWithPhotosPayload(
    val userId: Int,
    val hike: HikePayload,
    val photos: List<PhotoPayload>
)
