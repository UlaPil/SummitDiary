package com.example.summitdiary.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST("api/hikes")
    suspend fun postHike(
        @Part metadata: MultipartBody.Part,
        @Part photos: List<MultipartBody.Part>,
        @Part gpx: MultipartBody.Part?
    ): Response<Map<String, Any>>

    @POST("/api/users")
    suspend fun postUser(@Body user: UserRequest): Response<Map<String, Int>>

    @GET("hikes")
    suspend fun getHikes(): List<HikeDto>
}

