package com.example.summitdiary.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Hike::class,
        Place::class,
        Friend::class,
        Photo::class,
        HikeFriend::class,
        HikePhoto::class
    ],
    version = 3,
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
//                val MIGRATION_2_3 = object : Migration(2, 3) {
//                    override fun migrate(db: SupportSQLiteDatabase) {
//                        db.execSQL("ALTER TABLE Hikes ADD COLUMN description TEXT DEFAULT ''")
//                    }
//                }
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "summit_diary_database"
                ).addMigrations().build()
                INSTANCE = instance
                instance
            }
        }
    }
}