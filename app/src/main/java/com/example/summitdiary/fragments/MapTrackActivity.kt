package com.example.summitdiary.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.preference.PreferenceManager
import androidx.lifecycle.lifecycleScope
import com.example.summitdiary.viewmodel.GpxTrackLoader
import com.example.summitdiary.R
import com.example.summitdiary.database.AppDatabase
import com.example.summitdiary.database.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.core.graphics.scale
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.createBitmap
import android.media.ExifInterface
import android.graphics.Matrix
import com.bumptech.glide.Glide

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

        val hikeId = intent.getLongExtra("hikeId", -1)
        if (hikeId != -1L) {
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(this@MapTrackActivity)
                val hikePhotoDao = db.hikePhotoDao()
                val photos = hikePhotoDao.getPhotosForHike(hikeId)
                withContext(Dispatchers.Main) {
                    addPhotoMarkers(map, photos)
                }
            }
        }

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

    private fun addPhotoMarkers(map: MapView, photoList: List<Photo>) {
        val context = this
        val resources = context.resources

        for (photo in photoList) {
            val loc = photo.location?.split(",")?.map { it.trim().toDoubleOrNull() }
            if (loc != null && loc.size == 2 && loc[0] != null && loc[1] != null) {
                val point = GeoPoint(loc[0]!!, loc[1]!!)

                val bitmap = decodeScaledBitmapWithRotation(photo.path, 100)
                val circular = createCircularBitmap(bitmap, 100)

                val marker = org.osmdroid.views.overlay.Marker(map).apply {
                    position = point
                    icon = circular.toDrawable(resources)
                    setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                    title = "Zdjęcie"
                    setOnMarkerClickListener { _, _ ->
                        showPhotoDialog(photo.path)
                        true
                    }
                }

                map.overlays.add(marker)
            }
        }
        map.invalidate()
    }

    private fun createCircularBitmap(bitmap: Bitmap, targetSize: Int = 100): Bitmap {
        val scaledBitmap = bitmap.scale(targetSize, targetSize)
        val output = createBitmap(targetSize, targetSize)

        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }

        val radius = targetSize / 2f
        canvas.drawCircle(radius, radius, radius, paint)

        return output
    }

    fun rotateBitmapIfRequired(path: String, bitmap: Bitmap): Bitmap {
        return try {
            val exif = ExifInterface(path)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            bitmap
        }
    }

    fun decodeScaledBitmapWithRotation(path: String, targetSize: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)

        options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)
        options.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeFile(path, options)
        return rotateBitmapIfRequired(path, bitmap)
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }


    private fun showPhotoDialog(photoPath: String) {
        val dialog = android.app.AlertDialog.Builder(this).create()
        val imageView = android.widget.ImageView(this)
        imageView.setImageURI(Uri.fromFile(File(photoPath)))
        imageView.adjustViewBounds = true
        dialog.setView(imageView)
        dialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Zamknij") { d, _ -> d.dismiss() }
        dialog.show()
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
