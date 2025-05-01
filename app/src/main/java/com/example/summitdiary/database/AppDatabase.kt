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
        Photo::class,
        HikePhoto::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hikeDao(): HikeDao
    abstract fun placeDao(): PlaceDao
    abstract fun photoDao(): PhotoDao
    abstract fun hikePhotoDao(): HikePhotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val MIGRATION_3_4 = object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("DROP TABLE IF EXISTS Friends")
                    db.execSQL("DROP TABLE IF EXISTS HikesFriend")
                }
            }

            val MIGRATION_4_5 = object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE Hikes ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE HikesPhotos ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE Photos ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE Places ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                }
            }

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "summit_diary_database"
                ).addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
