package com.example.summitdiary.workers

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.summitdiary.database.AppDatabase
import com.example.summitdiary.network.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID

class SyncWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val db = AppDatabase.getDatabase(appContext)
    private val placeDao = db.placeDao()
    private val hikeDao = db.hikeDao()
    private val hikePhotoDao = db.hikePhotoDao()
    private val api = ApiClient.apiService
    private val gson = Gson()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("SyncWorker", "Start working")
        try {
            val sharedPref = applicationContext.getSharedPreferences("summit_prefs", Context.MODE_PRIVATE)
            var uuid = sharedPref.getString("user_uuid", null)
            var userId = sharedPref.getInt("user_id", -1)

            if (uuid == null) {
                uuid = UUID.randomUUID().toString()
                sharedPref.edit { putString("user_uuid", uuid) }
                Log.d("SyncWorker", "Generated new UUID: $uuid")
            }
            if (userId == -1) {
                Log.d("SyncWorker", "Registering user with UUID: $uuid")
                val userResponse = api.postUser(UserRequest(uuid))
                if (userResponse.isSuccessful) {
                    userId = userResponse.body()?.get("user_id") ?: -1
                    if (userId != -1) {
                        sharedPref.edit { putInt("user_id", userId) }
                        Log.d("SyncWorker", "Registered user with ID: $userId")
                    } else {
                        Log.e("SyncWorker", "No user_id in response")
                        return@withContext Result.failure()
                    }
                } else {
                    Log.e("SyncWorker", "Failed to register user: ${userResponse.code()}")
                    return@withContext Result.failure()
                }
            }

            val hikes = hikeDao.getUnsyncedHikes()
            Log.d("SyncWorker", "Found ${hikes.size} unsynced hikes")

            for (hike in hikes) {
                Log.d("SyncWorker", "Preparing hike ${hike.hike_id}")

                val hikePhotos = hikePhotoDao.getPhotosForHike(hike.hike_id)
                val place = placeDao.getPlaceById(hike.place_id)

                val metadataPayload = HikeWithPhotosPayload(
                    userId = userId,
                    hike = hike.toNetworkModel(place),
                    photos = hikePhotos.map { it.toNetworkModel() }
                )
                val metadataJson = gson.toJson(metadataPayload)
                Log.d("SyncWorker", "Metadata JSON: $metadataJson")

                val metadataPart = MultipartBody.Part.createFormData(
                    "metadata",
                    null,
                    metadataJson.toRequestBody("application/json".toMediaTypeOrNull())
                )

                val photosParts = hikePhotos.mapNotNull { photo ->
                    val file = File(photo.path)
                    if (file.exists()) {
                        Log.d("SyncWorker", "Adding photo: ${file.name}, size: ${file.length()} bytes")
                        val body = file.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("photo", file.name, body)
                    } else {
                        Log.w("SyncWorker", "Photo file not found: ${photo.path}")
                        null
                    }
                }

                val gpxFile = hike.gpx_path.takeIf { it.isNotEmpty() }?.let { File(it) }
                val gpxPart = gpxFile?.takeIf { it.exists() }?.let {
                    Log.d("SyncWorker", "Adding GPX file: ${it.name}, size: ${it.length()} bytes")
                    MultipartBody.Part.createFormData(
                        "gpx",
                        it.name,
                        it.asRequestBody("application/gpx+xml".toMediaTypeOrNull())
                    )
                }

                Log.d("SyncWorker", "Sending hike ${hike.hike_id} to server...")
                val response = api.postHike(metadataPart, photosParts, gpxPart)
                if (response.isSuccessful) {
                    Log.d("SyncWorker", "Hike ${hike.hike_id} synced successfully")
                    hikeDao.markAsSynced(hike.hike_id)
                } else {
                    Log.e("SyncWorker", "Failed to sync hike ${hike.hike_id}: ${response.code()} ${response.message()}")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error during sync", e)
            Result.failure()
        }
    }

}
