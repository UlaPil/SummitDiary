package com.example.summitdiary.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Hike::class,
        Place::class,
        Friend::class,
        Photo::class,
        HikeFriend::class,
        HikePhoto::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hikeDao(): HikeDao
    abstract fun placeDao(): PlaceDao
    abstract fun friendDao(): FriendDao
    abstract fun photoDao(): PhotoDao
    abstract fun hikeFriendDao(): HikeFriendDao
    abstract fun hikePhotoDao(): HikePhotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "summit_diary_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}