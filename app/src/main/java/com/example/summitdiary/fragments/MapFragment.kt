package com.example.summitdiary.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.example.summitdiary.R
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.example.summitdiary.database.AppDatabase
import com.example.summitdiary.database.Place
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import com.example.summitdiary.viewmodel.GpxTrackLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.views.overlay.Polyline
import androidx.core.view.isVisible
import androidx.core.graphics.toColorInt

class MapFragment : Fragment() {

    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var placeInfoBar: LinearLayout
    private lateinit var placeNameText: TextView
    private lateinit var editPlaceButton: Button
    private var currentVisiblePlaceId: Long? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Configuration.getInstance().load(
            requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )

        val view = inflater.inflate(R.layout.fragment_map, container, false)
        map = view.findViewById(R.id.mapView)
        map.setBuiltInZoomControls(false)
        map.setMultiTouchControls(true)
        map.setTileSource(TileSourceFactory.MAPNIK)

        placeInfoBar = view.findViewById<LinearLayout>(R.id.placeInfoBar)
        placeNameText = view.findViewById<TextView>(R.id.placeNameText)
        editPlaceButton = view.findViewById<Button>(R.id.editPlaceButton)

        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                setupMapLocation()
            } else {
                Toast.makeText(requireContext(), "Brak zgody na lokalizację", Toast.LENGTH_SHORT).show()
            }
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            setupMapLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        return view
    }


    private val drawnTrackOverlays = mutableMapOf<Long, List<Polyline>>()

    private fun setupMapLocation() {
        map.controller.setZoom(15.0)

        val locationProvider = GpsMyLocationProvider(requireContext())
        val locationIcon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ludzik)
        val scaledIcon = locationIcon.scale(100, 100)
        locationOverlay = MyLocationNewOverlay(locationProvider, map).apply {
            setPersonIcon(locationIcon)
            setDirectionArrow(scaledIcon, scaledIcon)
            enableMyLocation()
            enableFollowLocation()
        }
        map.overlays.add(locationOverlay)

        locationOverlay.runOnFirstFix {
            val loc = locationOverlay.myLocation
            if (loc != null) {
                requireActivity().runOnUiThread {
                    map.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
                }
            }
        }

        val db = AppDatabase.getDatabase(requireContext())
        val placeDao = db.placeDao()
        val hikeDao = db.hikeDao()
        Thread {
            val places = placeDao.getAll()
            requireActivity().runOnUiThread {
                val originalMarkerBitmap = BitmapFactory.decodeResource(resources, R.drawable.pin)
                val scaledMarkerBitmap = originalMarkerBitmap.scale(80, 80)
                val scaledMarkerDrawable = scaledMarkerBitmap.toDrawable(resources)

                for (place in places) {
                    val parts = place.gps.split(",")
                    val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull()
                    val lon = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
                    if (lat != null && lon != null) {
                        val marker = Marker(map).apply {
                            position = GeoPoint(lat, lon)
                            title = place.name
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = scaledMarkerDrawable
                            id = place.place_id.toString()
                        }
                        marker.setOnMarkerClickListener { _, _ ->
                            lifecycleScope.launch {
                                if (drawnTrackOverlays.containsKey(place.place_id)) {
                                    drawnTrackOverlays[place.place_id]?.forEach { map.overlays.remove(it) }
                                    drawnTrackOverlays.remove(place.place_id)
                                    placeInfoBar.visibility = View.GONE
                                    currentVisiblePlaceId = null
                                } else {
                                    drawnTrackOverlays[currentVisiblePlaceId]?.forEach { map.overlays.remove(it) }
                                    drawnTrackOverlays.remove(currentVisiblePlaceId)
                                    currentVisiblePlaceId = place.place_id
                                    placeInfoBar.visibility = View.VISIBLE
                                    placeNameText.text = place.name

                                    editPlaceButton.setOnClickListener {
                                        showEditPlaceDialog(place)
                                    }
                                    val hikes = withContext(Dispatchers.IO) {
                                        hikeDao.getHikesForPlace(place.place_id)
                                    }

                                    val colors = listOf("#720000".toColorInt(), "#005264".toColorInt(), "#483D8B".toColorInt(), "#770737".toColorInt())
                                    val polylines = mutableListOf<Polyline>()
                                    hikes.forEachIndexed { index, hike ->
                                        val points = GpxTrackLoader.loadTrackFromFile(hike.gpx_path)
                                        if (!points.isNullOrEmpty()) {
                                            val polyline = Polyline().apply {
                                                setPoints(points)
                                                color = colors[index % colors.size]
                                                width = 15f
                                            }
                                            map.overlays.add(polyline)
                                            polylines.add(polyline)
                                        }
                                    }
                                    drawnTrackOverlays[place.place_id] = polylines
                                    val thisMarker = map.overlays.find {
                                        it is Marker && (it as Marker).id == place.place_id.toString()
                                    } as? Marker

                                    thisMarker?.let {
                                        map.overlays.remove(it)
                                        map.overlays.add(it)
                                    }
                                    if (map.overlays.contains(locationOverlay)) {
                                        map.overlays.remove(locationOverlay)
                                        map.overlays.add(locationOverlay)
                                    }
                                }
                                map.invalidate()
                            }
                            true
                        }
                        map.overlays.add(marker)
                    }
                }
                if (map.overlays.contains(locationOverlay)) {
                    map.overlays.remove(locationOverlay)
                    map.overlays.add(locationOverlay)
                }
                map.invalidate()
            }
        }.start()
    }

    private fun showEditPlaceDialog(place: Place) {
        val input = EditText(requireContext()).apply {
            setText(place.name)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edytuj nazwę miejsca")
            .setView(input)
            .setPositiveButton("Zapisz") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getDatabase(requireContext())
                        db.placeDao().insert(place.copy(name = newName))
                        withContext(Dispatchers.Main) {
                            placeNameText.text = newName
                            Toast.makeText(requireContext(), "Zmieniono nazwę!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }


    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun toString(): String {
        return "Map"
    }
}
