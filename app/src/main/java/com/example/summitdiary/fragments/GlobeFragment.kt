package com.example.summitdiary.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.summitdiary.GlobeHikeAdapter
import com.example.summitdiary.database.Hike
import com.example.summitdiary.database.Photo
import com.example.summitdiary.databinding.FragmentGlobeBinding
import com.example.summitdiary.network.ApiClient
import com.example.summitdiary.network.HikeDto
import com.example.summitdiary.workers.SyncWorker
import kotlinx.coroutines.launch
import java.io.File
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import com.example.summitdiary.database.HikePhoto
import com.example.summitdiary.database.Place
import com.example.summitdiary.database.AppDatabase
import com.example.summitdiary.network.Config.BASE_URL
import com.example.summitdiary.network.HikeDetailDto
import com.example.summitdiary.network.PhotoDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class GlobeFragment : Fragment() {

    private var _binding: FragmentGlobeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GlobeHikeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGlobeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = GlobeHikeAdapter { hike ->
            applyHike(hike)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.buttonSyncNow.setOnClickListener {
            val workRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
            WorkManager.getInstance(requireContext()).enqueue(workRequest)
        }

        fetchHikes()
    }

    private fun fetchHikes() {
        lifecycleScope.launch {
            try {
                val sharedPref = requireContext().getSharedPreferences("summit_prefs", Context.MODE_PRIVATE)
                val userId = sharedPref.getInt("user_id", -1)
                val allhikes = ApiClient.apiService.getHikes()
                val hikes = allhikes.filter { it.userId != userId }

                if (_binding != null) {
                    adapter.updateData(hikes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (_binding != null) {
                    binding.errorText.text = "Brak połączenia z serwerem"
                    binding.errorText.visibility = View.VISIBLE
                }
            }
        }
    }

    suspend fun downloadFile(url: String, destFile: File): Boolean {
        return try {
            val response = ApiClient.apiService.downloadFile(url)
            if (response.isSuccessful) {
                response.body()?.byteStream()?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }


    private fun parseLatLng(gps: String?): android.location.Location {
        val (lat, lon) = gps?.split(",")?.map { it.trim().toDouble() } ?: listOf(0.0, 0.0)
        return android.location.Location("").apply {
            latitude = lat
            longitude = lon
        }
    }

    private fun distanceInMeters(a: android.location.Location, b: android.location.Location): Double {
        return a.distanceTo(b).toDouble()
    }

    private fun showPlaceSelectionDialog(places: List<Place>, onSelected: (Place?) -> Unit) {
        val names = places.map { it.name }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Wybierz miejsce")
            .setItems(names) { _, which ->
                onSelected(places[which])
            }
            .setNegativeButton("Dodaj nowe") { _, _ ->
                onSelected(null)
            }
            .show()
    }


    private fun applyHike(hike: HikeDto) {
        val db = AppDatabase.getDatabase(requireContext())

        lifecycleScope.launch {
            try {
                val hikeDetail = ApiClient.apiService.getHikeById(hike.id)

                fun getDownloadUrl(type: String, path: String): String {
                    val baseUrl = BASE_URL
                    val filename = path.substringAfterLast("/")
                    return "$baseUrl/api/$type/$filename"
                }

                val gpxUrl = getDownloadUrl("gpx", hikeDetail.gpxPath)
                val gpxFile = File(requireContext().filesDir, "gpx_${hikeDetail.id}.gpx")
                val gpxSuccess = downloadFile(gpxUrl, gpxFile)

                if (!gpxSuccess) {
                    Toast.makeText(requireContext(), "Nie udało się pobrać pliku GPX", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val photoEntities = mutableListOf<Photo>()
                for (photoDto in hikeDetail.photos) {
                    val photoUrl = getDownloadUrl("photo", photoDto.path)
                    val photoFile = File(requireContext().filesDir, "photo_${photoDto.id}.jpg")
                    val photoSuccess = downloadFile(photoUrl, photoFile)
                    if (!photoSuccess) {
                        Toast.makeText(requireContext(), "Nie udało się pobrać zdjęcia ${photoDto.id}", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    photoEntities.add(
                        Photo(location = photoDto.location, path = photoFile.absolutePath, isSynced = true)
                    )
                }

                val places = withContext(Dispatchers.IO) {
                    db.placeDao().getAll()
                }

                val serverLatLng = parseLatLng(hikeDetail.placeGps)
                val nearbyPlaces = places.filter {
                    distanceInMeters(parseLatLng(it.gps), serverLatLng) <= 2000
                }

                val selectedPlaceId = suspendCoroutine<Long> { cont ->
                    if (nearbyPlaces.isNotEmpty()) {
                        showPlaceSelectionDialog(nearbyPlaces) { selectedPlace ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                val id = selectedPlace?.place_id ?: db.placeDao().insert(
                                    Place(name = hikeDetail.placeName ?: "Nowe miejsce", gps = hikeDetail.placeGps ?: "")
                                )
                                cont.resume(id)
                            }
                        }
                    } else {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val id = db.placeDao().insert(
                                Place(name = hikeDetail.placeName ?: "Nowe miejsce", gps = hikeDetail.placeGps ?: "")
                            )
                            cont.resume(id)
                        }
                    }
                }

                withContext(Dispatchers.IO) {
                    db.runInTransaction {
                        val localHikeId = db.hikeDao().insert(
                            Hike(
                                title = hikeDetail.title,
                                date = hikeDetail.date,
                                distance = hikeDetail.distance,
                                time = hikeDetail.time,
                                place_id = selectedPlaceId,
                                gpx_path = gpxFile.absolutePath,
                                isSynced = true
                            )
                        )
                        photoEntities.forEach { photo ->
                            val localPhotoId = db.photoDao().insert(photo)
                            db.hikePhotoDao().insert(HikePhoto(hike_id = localHikeId!!, photo_id = localPhotoId!!))
                        }
                    }
                }

                Toast.makeText(requireContext(), "Aktywność zapisana!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Log.e("ApplyHike", "Błąd: ${e.message}")
                Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun toString(): String {
        return "Global Activities"
    }
}


