package com.subcoder.ftlhiresaudioplayer.di

import android.content.Context
import androidx.room.Room
import com.subcoder.ftlhiresaudioplayer.data.database.FTLAudioDatabase
import com.subcoder.ftlhiresaudioplayer.data.database.dao.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Database Module - Provides Room database and DAOs
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FTLAudioDatabase {
        return Room.databaseBuilder(
            context,
            FTLAudioDatabase::class.java,
            "ftl_audio_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideTrackDao(database: FTLAudioDatabase): TrackDao {
        return database.trackDao()
    }
}