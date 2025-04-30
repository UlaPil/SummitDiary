package com.example.summitdiary.viewmodel

import android.graphics.Color
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

object GpxTrackLoader {
    fun loadTrackFromFile(filePath: String): List<GeoPoint>? {
        return try {
            val file = File(filePath)
            if (!file.exists()) return null

            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            val trkpts = doc.getElementsByTagName("trkpt")
            val points = mutableListOf<GeoPoint>()

            for (i in 0 until trkpts.length) {
                val node = trkpts.item(i) as? Element ?: continue
                val lat = node.getAttribute("lat").toDoubleOrNull()
                val lon = node.getAttribute("lon").toDoubleOrNull()
                if (lat != null && lon != null) {
                    points.add(GeoPoint(lat, lon))
                }
            }

            points
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun drawTrackOnMap(map: MapView, points: List<GeoPoint>, color: Int = Color.RED) {
        val polyline = Polyline().apply {
            setPoints(points)
            this.color = color
            width = 20f
        }
        map.overlays.add(polyline)
        map.invalidate()
    }
}
