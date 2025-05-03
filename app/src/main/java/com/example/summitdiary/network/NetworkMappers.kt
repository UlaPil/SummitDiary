package com.example.summitdiary.network

import com.example.summitdiary.database.Hike
import com.example.summitdiary.database.Photo
import com.example.summitdiary.database.Place

fun Hike.toNetworkModel(place: Place): HikePayload {
    return HikePayload(
        title = this.title,
        date = this.date,
        distance = this.distance,
        time = this.time,
        placeName = place.name,
        placeGps = place.gps,
        gpxPath = this.gpx_path
    )
}

fun Photo.toNetworkModel(): PhotoPayload {
    return PhotoPayload(
        localPhotoId = this.photo_id,
        location = this.location ?: "",
        path = this.path
    )
}
