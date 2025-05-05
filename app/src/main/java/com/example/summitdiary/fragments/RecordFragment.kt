package com.example.summitdiary.fragments


import com.example.summitdiary.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.summitdiary.databinding.FragmentRecordBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.graphics.scale
import androidx.fragment.app.viewModels
import com.example.summitdiary.database.AppDatabase
import com.example.summitdiary.database.Hike
import com.example.summitdiary.database.HikeDao
import com.example.summitdiary.database.HikePhoto
import com.example.summitdiary.database.HikePhotoDao
import com.example.summitdiary.database.PhotoDao
import com.example.summitdiary.database.Photo
import androidx.lifecycle.lifecycleScope
import com.example.summitdiary.viewmodel.RecordViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import kotlin.math.roundToInt

class RecordFragment : Fragment() {
    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var photoDao: PhotoDao
    private lateinit var hikePhotoDao: HikePhotoDao
    private lateinit var hikeDao: HikeDao

    private val locationPoints = mutableListOf<Pair<Double, Double>>()
    private lateinit var locationCallback: android.location.LocationListener
    private lateinit var locationManager: android.location.LocationManager

    private val viewModel: RecordViewModel by viewModels()
    private var totalDistanceMeters = 0.0
    private var startTimeInMillis: Long? = 0L
    private var elapsedSeconds = 0L
    private var isRecording = false
    private var photoUri: Uri? = null
    private var timerJob: Job? = null

    private lateinit var miniMapView: MapView
    private lateinit var miniLocationOverlay: MyLocationNewOverlay

    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private var currentPhotoPath: String? = null
    private val CAMERA_PERMISSION_CODE = 100

    companion object {
        private const val KEY_IS_RECORDING = "key_is_recording"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isRecording = savedInstanceState?.getBoolean(KEY_IS_RECORDING) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startTimeInMillis = savedInstanceState?.getLong("startTimeInMillis")
        elapsedSeconds = savedInstanceState?.getLong("elapsedSeconds") ?: 0L
        if (startTimeInMillis != null && isRecording) {
            startTimer()
        }
        val db = AppDatabase.getDatabase(requireContext())
        hikeDao = db.hikeDao()
        photoDao = db.photoDao()
        hikePhotoDao = db.hikePhotoDao()
        updateStartStopIcon()

        miniMapView = binding.miniMapView

        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        miniMapView.setTileSource(TileSourceFactory.MAPNIK)
        miniMapView.setMultiTouchControls(true)
        miniMapView.setBuiltInZoomControls(false)
        miniMapView.controller.setZoom(15.0)

        val locationProvider = GpsMyLocationProvider(requireContext())
        val locationIcon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ludzik)
        val scaledIcon = locationIcon.scale(100, 100)

        miniLocationOverlay = MyLocationNewOverlay(locationProvider, miniMapView).apply {
            setPersonIcon(locationIcon)
            setDirectionArrow(scaledIcon, scaledIcon)
            enableMyLocation()
            enableFollowLocation()
        }
        miniMapView.overlays.add(miniLocationOverlay)

        miniLocationOverlay.runOnFirstFix {
            val loc = miniLocationOverlay.myLocation
            if (loc != null) {
                requireActivity().runOnUiThread {
                    miniMapView.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
                }
            }
        }

        binding.startStopButton.setOnClickListener {
            isRecording = !isRecording
            updateStartStopIcon()

            if (isRecording) {
                startTimeInMillis = System.currentTimeMillis()
                elapsedSeconds = 0L
                startTimer()
                startLocationTracking()

                lifecycleScope.launch(Dispatchers.IO) {
                    val hike = Hike(
                        title = "Nowa wędrówka",
                        date = SimpleDateFormat("dd.MM.yyyy (HH:mm)", Locale.getDefault()).format(Date()),
                        distance = 0.0,
                        time = "0",
                        place_id = 0,
                        gpx_path = ""
                    )
                    viewModel.currentHikeId = hikeDao.insert(hike)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Rozpoczęto wędrówkę!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                stopLocationTracking()
                saveGpxFile()
                stopTimer()
                binding.timeText.text = "00:00:00"
                binding.distanceText.text = "0,00 km"
                lifecycleScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main) {
                        showNameHikeDialog()
                        Toast.makeText(requireContext(), "Zakończono wędrówkę", Toast.LENGTH_SHORT).show()
                    }
                }
                Toast.makeText(requireContext(), "Zakończono wędrówkę", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cameraButton.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.locationButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.currentHikeId?.let { hikeId ->
                    val db = AppDatabase.getDatabase(requireContext())
                    val hike = db.hikeDao().getHikeById(hikeId)
                    if (hike != null) {
                        val hasPlace = hike.place_id != 0L

                        withContext(Dispatchers.Main) {
                            if (hasPlace) {
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Zmień miejsce")
                                    .setMessage("Czy chcesz zmienić miejsce przypisane do tej wędrówki?")
                                    .setPositiveButton("Tak") { _, _ ->
                                        pickPlaceAndUpdate(hikeId)
                                    }
                                    .setNegativeButton("Nie", null)
                                    .show()
                            } else {
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Dodaj miejsce")
                                    .setMessage("Chcesz dodać miejsce dla tej wędrówki?")
                                    .setPositiveButton("Tak") { _, _ ->
                                        pickPlaceAndUpdate(hikeId)
                                    }
                                    .setNegativeButton("Nie", null)
                                    .show()
                            }
                        }
                    }
                }
            }
        }

        requestCameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Brak pozwolenia na aparat", Toast.LENGTH_SHORT).show()
            }
        }

        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                currentPhotoPath?.let { path ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.currentHikeId?.let { hikeId ->
                            val locationManager = requireContext().getSystemService(Activity.LOCATION_SERVICE) as android.location.LocationManager
                            val location = if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                            } else null

                            val locationString = location?.let { "${it.latitude},${it.longitude}" }

                            val photoId = photoDao.insert(Photo(location = locationString, path = path))
                            if (photoId != null) {
                                hikePhotoDao.insert(HikePhoto(
                                    hike_id = hikeId,
                                    photo_id = photoId
                                ))
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "Zdjęcie zapisane do wędrówki", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "Błąd: Nie udało się zapisać zdjęcia", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } ?: run {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Brak aktywnej wędrówki", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Anulowano robienie zdjęcia.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickPlaceAndUpdate(hikeId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            val placeDao = db.placeDao()
            val places = placeDao.getAll()

            val placeNames = places.map { it.name } + "Dodaj nowe miejsce"

            withContext(Dispatchers.Main) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Wybierz miejsce")
                    .setSingleChoiceItems(placeNames.toTypedArray(), -1) { dialog, which ->
                        if (which < places.size) {
                            val selectedPlaceId = places[which].place_id
                            lifecycleScope.launch(Dispatchers.IO) {
                                db.hikeDao().updatePlaceId(hikeId, selectedPlaceId)
                            }
                            dialog.dismiss()
                        } else {
                            val lastPoint = locationPoints.lastOrNull()
                            if (lastPoint != null) {
                                askForNewPlaceName(lastPoint.first, lastPoint.second) { newPlaceId ->
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        db.hikeDao().updatePlaceId(hikeId, newPlaceId)
                                    }
                                }
                            } else {
                                Toast.makeText(requireContext(), "Brak sygnału GPS", Toast.LENGTH_SHORT).show()
                            }
                            dialog.dismiss()
                        }
                    }
                    .setNegativeButton("Anuluj", null)
                    .show()
            }
        }
    }


    private fun updateStartStopIcon() {
        if (isRecording) {
            binding.startStopButton.setImageResource(R.drawable.baseline_stop_24)
        } else {
            binding.startStopButton.setImageResource(R.drawable.baseline_play_arrow_24)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_RECORDING, isRecording)
        startTimeInMillis.let { outState.putLong("startTimeInMillis", startTimeInMillis ?: 0L)
        }
        outState.putLong("elapsedSeconds", elapsedSeconds)
    }

    private fun dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = createImageFile()
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            takePictureLauncher.launch(intent)
        }
    }

    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private suspend fun showNameHikeDialog() {
        val durationFormatted = formatDuration(elapsedSeconds)

        val startLat = locationPoints.firstOrNull()?.first
        val startLon = locationPoints.firstOrNull()?.second

        val hikeId = viewModel.currentHikeId ?: return
        val hike = withContext(Dispatchers.IO) { hikeDao.getHikeById(hikeId) }

        if (startLat == null || startLon == null) {
            if (hike?.place_id != null && hike.place_id != 0L) {
                showHikeNameDialog(durationFormatted, hike.place_id)
                return
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Brak sygnału GPS", Toast.LENGTH_SHORT).show()
            }
            showHikeNameDialog(durationFormatted, 0)
            return
        }

        if (hike?.place_id != null && hike.place_id != 0L) {
            showHikeNameDialog(durationFormatted, hike.place_id)
            return
        }

        val db = AppDatabase.getDatabase(requireContext())
        val placeDao = db.placeDao()

        val nearbyPlaces = withContext(Dispatchers.IO) {
            placeDao.getAll().filter { place ->
                val (lat, lon) = place.gps.split(",").map { it.trim().toDoubleOrNull() ?: 0.0 }
                distanceInMeters(startLat, startLon, lat, lon) < 2000
            }
        }

        val placeNames = nearbyPlaces.map { it.name } + "Dodaj nowe miejsce"

        withContext(Dispatchers.Main) {
            AlertDialog.Builder(requireContext())
                .setTitle("Wybierz miejsce")
                .setSingleChoiceItems(placeNames.toTypedArray(), -1) { dialog, which ->
                    if (which < nearbyPlaces.size) {
                        val selectedPlaceId = nearbyPlaces[which].place_id
                        showHikeNameDialog(durationFormatted, selectedPlaceId)
                        dialog.dismiss()
                    } else {
                        askForNewPlaceName(locationPoints.lastOrNull()!!.first, locationPoints.lastOrNull()!!.second) { newPlaceId ->
                            showHikeNameDialog(durationFormatted, newPlaceId)
                        }
                        dialog.dismiss()
                    }
                }
                .setNegativeButton("Anuluj", null)
                .show()
        }
    }

    private fun askForNewPlaceName(lat: Double, lon: Double, onPlaceAdded: (Long) -> Unit) {
        val input = EditText(requireContext())
        input.hint = "Nazwa nowego miejsca"

        AlertDialog.Builder(requireContext())
            .setTitle("Dodaj nowe miejsce")
            .setView(input)
            .setPositiveButton("Zapisz") { _, _ ->
                val name = input.text.toString().ifBlank { "Nowe miejsce" }
                val gpsString = "$lat, $lon"

                lifecycleScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(requireContext())
                    val placeId = db.placeDao().insert(com.example.summitdiary.database.Place(name = name, gps = gpsString))
                    withContext(Dispatchers.Main) {
                        onPlaceAdded(placeId)
                    }
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun showHikeNameDialog(durationFormatted: String, placeId: Long) {
        val input = EditText(requireContext())
        input.hint = "Nazwa wędrówki"

        AlertDialog.Builder(requireContext())
            .setTitle("Zakończ wędrówkę")
            .setMessage("Czas trwania: $durationFormatted")
            .setView(input)
            .setPositiveButton("Zapisz") { _, _ ->
                val name = input.text.toString().ifBlank { "Wędrówka $durationFormatted" }

                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.currentHikeId?.let { hikeId ->
                        val db = AppDatabase.getDatabase(requireContext())
                        val hike = db.hikeDao().getHikeById(hikeId)
                        if (hike != null) {
                            hike.title = name
                            hike.time = durationFormatted
                            hike.place_id = placeId
                            db.hikeDao().update(hike)
                        }
                    }
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }


    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            photoUri?.let {}
        }
    }

    private fun startTimer() {
        timerJob = lifecycleScope.launch {
            while (isRecording) {
                delay(1000)
                elapsedSeconds++
                updateTimerUI()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun updateTimerUI() {
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60
        binding.timeText.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun startLocationTracking() {
        locationManager = requireContext().getSystemService(Activity.LOCATION_SERVICE) as android.location.LocationManager

        locationCallback = android.location.LocationListener { location ->
            val newPoint = location.latitude to location.longitude

            val lastPoint = locationPoints.lastOrNull()
            if (lastPoint != null) {
                val distance = distanceInMeters(
                    lastPoint.first, lastPoint.second,
                    newPoint.first, newPoint.second
                )
                totalDistanceMeters += distance
                updateDistanceUI()
            }

            locationPoints.add(newPoint)
        }


        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 200)
            return
        }

        locationManager.requestLocationUpdates(
            android.location.LocationManager.GPS_PROVIDER,
            2000L,
            1f,
            locationCallback
        )
    }

    private fun stopLocationTracking() {
        if (::locationManager.isInitialized && ::locationCallback.isInitialized) {
            locationManager.removeUpdates(locationCallback)
        }
    }

    private fun updateDistanceUI() {
        val km = totalDistanceMeters / 1000
        val formatted = String.format(Locale.getDefault(), "%.2f km", km)
        binding.distanceText.text = formatted
    }

    private fun saveGpxFile() {
        if (locationPoints.isEmpty()) return

        val gpx = buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            append("<gpx version=\"1.1\" creator=\"SummitDiary\">\n")
            append("<trk><name>Recorded Track</name><trkseg>\n")
            for ((lat, lon) in locationPoints) {
                append("<trkpt lat=\"$lat\" lon=\"$lon\"></trkpt>\n")
            }
            append("</trkseg></trk>\n")
            append("</gpx>")
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "track_$timeStamp.gpx"
        val file = File(requireContext().getExternalFilesDir(null), fileName)
        file.writeText(gpx)

        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.currentHikeId?.let { hikeId ->
                val hike = hikeDao.getHikeById(hikeId)
                if (hike != null) {
                    hike.gpx_path = file.absolutePath
                    hike.distance = (totalDistanceMeters / 1000)
                    hikeDao.update(hike)
                }
            }
        }

        Toast.makeText(requireContext(), "Zapisano ślad do pliku GPX", Toast.LENGTH_SHORT).show()
    }

    private fun distanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0].toDouble()
    }

    override fun onResume() {
        super.onResume()
        miniMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        miniMapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun toString(): String {
        return "Record"
    }
}