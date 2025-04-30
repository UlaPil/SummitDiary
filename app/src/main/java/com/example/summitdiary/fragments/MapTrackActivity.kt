package com.example.summitdiary.fragments

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import android.graphics.Color
import android.preference.PreferenceManager
import com.example.summitdiary.viewmodel.GpxTrackLoader
import com.example.summitdiary.R

class MapTrackActivity : AppCompatActivity() {
    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        setContentView(R.layout.activity_map_track)

        map = findViewById(R.id.mapView)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setBuiltInZoomControls(false)
        map.setMultiTouchControls(true)

        val gpxPath = intent.getStringExtra("gpxPath")
        if (gpxPath != null) {
            val points = GpxTrackLoader.loadTrackFromFile(gpxPath)
            if (!points.isNullOrEmpty()) {
                GpxTrackLoader.drawTrackOnMap(map, points)
                map.controller.setZoom(17.0)
                map.controller.setCenter(points.first())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}
