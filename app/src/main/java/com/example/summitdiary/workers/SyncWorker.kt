package com.example.summitdiary.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.summitdiary.database.AppDatabase
import com.example.summitdiary.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID
import androidx.core.content.edit

class SyncWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val db = AppDatabase.getDatabase(appContext)
    private val placeDao = db.placeDao()
    private val hikeDao = db.hikeDao()
    private val photoDao = db.photoDao()
    private val hikePhotoDao = db.hikePhotoDao()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("SyncWorker", "Starting SyncWorker")

        val api = ApiClient.apiService
        val sharedPref = applicationContext.getSharedPreferences("summit_prefs", Context.MODE_PRIVATE)
//        sharedPref.edit().remove("user_id").apply()
        var uuid = sharedPref.getString("user_uuid", null)
        var userId = sharedPref.getInt("user_id", -1)

        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            sharedPref.edit { putString("user_uuid", uuid) }
            Log.d("SyncWorker", "Generated new UUID: $uuid")
        }

        Log.d("SyncWorker", "Loaded UUID: $uuid, userId: $userId")

        if (userId == -1) {
            Log.d("SyncWorker", "Fetching user_id from server...")
            val userResponse = api.postUser(UserRequest(uuid))
            if (userResponse.isSuccessful) {
                userId = userResponse.body()?.get("user_id") ?: -1
                if (userId != -1) {
                    sharedPref.edit { putInt("user_id", userId) }
                    Log.d("SyncWorker", "Received and saved user_id: $userId")
                } else {
                    Log.e("SyncWorker", "Server response missing user_id")
                    return@withContext Result.failure()
                }
            } else {
                Log.e("SyncWorker", "Failed to register user: ${userResponse.code()} ${userResponse.message()}")
                return@withContext Result.failure()
            }
        }

        try {
            val places = placeDao.getUnsyncedPlaces()
            Log.d("SyncWorker", "Found ${places.size} unsynced places")
            for (place in places) {
                val response = api.postPlace(PlaceRequest(place.name, place.gps, userId))
                Log.d("SyncWorker", "Sent place '${place.name}' → ${response.code()} ${response.message()}")
                if (response.isSuccessful) {
                    place.isSynced = true
                    placeDao.update(place)
                }
            }

            val hikes = hikeDao.getUnsyncedHikes()
            Log.d("SyncWorker", "Found ${hikes.size} unsynced hikes")
            for (hike in hikes) {
                val response = api.postHike(HikeRequest(hike.title, hike.date, hike.distance, hike.time, hike.place_id.toInt(), hike.gpx_path, userId))
                Log.d("SyncWorker", "Sent hike '${hike.title}' → ${response.code()} ${response.message()}")
                if (response.isSuccessful) {
                    hike.isSynced = true
                    hikeDao.update(hike)
                }
                uploadFile(hike.gpx_path, "gpx")
            }

            val photos = photoDao.getUnsyncedPhotos()
            Log.d("SyncWorker", "Found ${photos.size} unsynced photos")
            for (photo in photos) {
                val response = api.postPhoto(PhotoRequest(photo.location, photo.path, userId))
                Log.d("SyncWorker", "Sent photo '${photo.path}' → ${response.code()} ${response.message()}")
                if (response.isSuccessful) {
                    photo.isSynced = true
                    photoDao.update(photo)
                }
                uploadFile(photo.path, "photo")
            }

            val hikePhotos = hikePhotoDao.getUnsyncedLinks()
            Log.d("SyncWorker", "Found ${hikePhotos.size} unsynced hike-photo links")
            for (link in hikePhotos) {
                val response = api.postHikePhoto(HikePhotoRequest(link.hike_id.toInt(), link.photo_id.toInt()))
                Log.d("SyncWorker", "Sent hike-photo link ${link.hike_id} - ${link.photo_id} → ${response.code()} ${response.message()}")
                if (response.isSuccessful) {
                    link.isSynced = true
                    hikePhotoDao.update(link)
                }
            }

            Log.d("SyncWorker", "Sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error: ${e.message}", e)
            Result.failure()
        }
    }


    private suspend fun uploadFile(path: String, type: String) {
        val api = ApiClient.apiService
        val file = File(path)
        if (file.exists()) {
            Log.d("SyncWorker", "Uploading file: $path as type: $type")
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val typePart = type.toRequestBody("multipart/form-data".toMediaTypeOrNull())

            val uploadResponse = api.uploadFile(body, typePart)
            if (uploadResponse.isSuccessful) {
                Log.d("SyncWorker", "Upload success: ${uploadResponse.body()?.get("message")}")
            } else {
                Log.e("SyncWorker", "Upload failed: ${uploadResponse.code()} ${uploadResponse.message()}")
            }
        } else {
            Log.e("SyncWorker", "File not found: $path")
        }
    }


    private fun getOrCreateUserId(context: Context): String {
        val sharedPref = context.getSharedPreferences("summit_prefs", Context.MODE_PRIVATE)
        var userId = sharedPref.getString("user_id", null)
        if (userId == null) {
            userId = UUID.randomUUID().toString()
            sharedPref.edit() { putString("user_id", userId) }
        }
        return userId
    }
}