package com.example.summitdiary.database

import androidx.room.*

data class HikeWithPhotos(
    @Embedded val hike: Hike,
    @Relation(
        parentColumn = "hike_id",
        entityColumn = "photo_id",
        associateBy = Junction(HikePhoto::class)
    )
    val photos: List<Photo>
)
