package com.subcoder.ftlhiresaudioplayer.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.subcoder.ftlhiresaudioplayer.data.database.dao.TrackDao
import com.subcoder.ftlhiresaudioplayer.data.database.entities.Track

/**
 * FTL Audio Database - Room database for hi-res audio player
 * 
 * Version 1: Initial database with Track entity
 */
@Database(
    entities = [Track::class],
    version = 1,
    exportSchema = false
)
abstract class FTLAudioDatabase : RoomDatabase() {
    
    abstract fun trackDao(): TrackDao
    
    companion object {
        @Volatile
        private var INSTANCE: FTLAudioDatabase? = null
        
        fun getDatabase(context: Context): FTLAudioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FTLAudioDatabase::class.java,
                    "ftl_audio_database"
                )
                .fallbackToDestructiveMigration() // For development only
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}