package com.ai.jobfinder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [JobEntity::class, SavedSearchEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao
    abstract fun savedSearchDao(): SavedSearchDao

    companion object {
        fun create(context: Context): AppDatabase = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "job_finder.db"
        ).fallbackToDestructiveMigration().build()
    }
}

