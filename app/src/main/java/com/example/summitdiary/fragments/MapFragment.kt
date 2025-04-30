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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import com.example.summitdiary.R
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.example.summitdiary.database.AppDatabase
import com.example.summitdiary.database.Place
import androidx.core.graphics.drawable.toDrawable

class MapFragment : Fragment() {

    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
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

        val db = AppDatabase.getDatabase(requireContext())
        val placeDao = db.placeDao()
        Thread {
            placeDao.insert(
                Place(name = "Giewont", gps = "49.2636, 19.9233")
            )
            placeDao.insert(
                Place(name = "Rysy", gps = "49.1794, 20.0886")
            )
        }.start()

        return view
    }


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
        Thread {
            val places = placeDao.getAll()
            requireActivity().runOnUiThread {
                val originalMarkerBitmap = BitmapFactory.decodeResource(resources, R.drawable.pin)
                val scaledMarkerBitmap = originalMarkerBitmap.scale(80, 80)
                val scaledMarkerDrawable = scaledMarkerBitmap.toDrawable(resources)
                for (place in places) {
                    val parts = place.gps.split(",")
                    if (parts.size == 2) {
                        val lat = parts[0].trim().toDoubleOrNull()
                        val lon = parts[1].trim().toDoubleOrNull()
                        if (lat != null && lon != null) {
                            val marker = Marker(map).apply {
                                position = GeoPoint(lat, lon)
                                title = place.name
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                icon = scaledMarkerDrawable
                            }
                            map.overlays.add(marker)
                        }
                    }
                }
                map.invalidate()
            }
        }.start()
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
