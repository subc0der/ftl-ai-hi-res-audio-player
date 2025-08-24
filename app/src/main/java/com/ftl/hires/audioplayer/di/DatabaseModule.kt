package com.ftl.hires.audioplayer.di

import android.content.Context
import androidx.room.Room
import com.ftl.hires.audioplayer.data.database.FTLAudioDatabase
import com.ftl.hires.audioplayer.data.database.dao.TrackDao
import com.ftl.hires.audioplayer.data.database.dao.PlaylistDao
import com.ftl.hires.audioplayer.data.database.dao.LibraryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FTLAudioDatabase {
        return FTLAudioDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideTrackDao(database: FTLAudioDatabase): TrackDao {
        return database.trackDao()
    }
    
    @Provides
    fun providePlaylistDao(database: FTLAudioDatabase): PlaylistDao {
        return database.playlistDao()
    }
    
    @Provides
    fun provideLibraryDao(database: FTLAudioDatabase): LibraryDao {
        return database.libraryDao()
    }
}