package com.example.summitdiary.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("/api/hikes")
    suspend fun postHike(@Body hike: HikeRequest): Response<Map<String, String>>

    @POST("/api/places")
    suspend fun postPlace(@Body place: PlaceRequest): Response<Map<String, String>>

    @POST("/api/photos")
    suspend fun postPhoto(@Body photo: PhotoRequest): Response<Map<String, String>>

    @Multipart
    @POST("/api/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody
    ): Response<Map<String, String>>

    @POST("/api/users")
    suspend fun postUser(@Body user: UserRequest): Response<Map<String, Int>>

    @POST("/api/hikephotos")
    suspend fun postHikePhoto(@Body link: HikePhotoRequest): Response<Map<String, String>>
}

