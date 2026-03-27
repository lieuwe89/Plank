package com.planktracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlankRecord::class], version = 1, exportSchema = false)
abstract class PlankDatabase : RoomDatabase() {

    abstract fun plankDao(): PlankDao

    companion object {
        @Volatile
        private var INSTANCE: PlankDatabase? = null

        fun getInstance(context: Context): PlankDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PlankDatabase::class.java,
                    "plank_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
