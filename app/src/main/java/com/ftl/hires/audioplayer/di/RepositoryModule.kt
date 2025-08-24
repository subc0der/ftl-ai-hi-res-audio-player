package com.ftl.hires.audioplayer.di

import com.ftl.hires.audioplayer.data.database.dao.TrackDao
import com.ftl.hires.audioplayer.data.database.dao.PlaylistDao
import com.ftl.hires.audioplayer.data.database.dao.LibraryDao
import com.ftl.hires.audioplayer.data.repository.TrackRepository
import com.ftl.hires.audioplayer.data.repository.TrackRepositoryImpl
import com.ftl.hires.audioplayer.data.repository.PlaylistRepository
import com.ftl.hires.audioplayer.data.repository.PlaylistRepositoryImpl
import com.ftl.hires.audioplayer.data.repository.LibraryRepository
import com.ftl.hires.audioplayer.data.repository.LibraryRepositoryImpl
import com.ftl.hires.audioplayer.data.repository.MediaScannerRepository
import com.ftl.hires.audioplayer.data.repository.MediaScannerRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindTrackRepository(
        trackRepositoryImpl: TrackRepositoryImpl
    ): TrackRepository
    
    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        playlistRepositoryImpl: PlaylistRepositoryImpl
    ): PlaylistRepository
    
    @Binds
    @Singleton
    abstract fun bindLibraryRepository(
        libraryRepositoryImpl: LibraryRepositoryImpl
    ): LibraryRepository
    
    @Binds
    @Singleton
    abstract fun bindMediaScannerRepository(
        mediaScannerRepositoryImpl: MediaScannerRepositoryImpl
    ): MediaScannerRepository
    
    companion object {
        @Provides
        @Singleton
        fun provideTrackRepositoryImpl(
            trackDao: TrackDao
        ): TrackRepositoryImpl {
            return TrackRepositoryImpl(trackDao)
        }
        
        @Provides
        @Singleton
        fun providePlaylistRepositoryImpl(
            playlistDao: PlaylistDao,
            trackDao: TrackDao
        ): PlaylistRepositoryImpl {
            return PlaylistRepositoryImpl(playlistDao, trackDao)
        }
        
        @Provides
        @Singleton
        fun provideLibraryRepositoryImpl(
            libraryDao: LibraryDao,
            trackDao: TrackDao
        ): LibraryRepositoryImpl {
            return LibraryRepositoryImpl(libraryDao, trackDao)
        }
        
        @Provides
        @Singleton
        fun provideMediaScannerRepositoryImpl(
            @ApplicationContext context: Context,
            trackDao: TrackDao,
            libraryDao: LibraryDao
        ): MediaScannerRepositoryImpl {
            return MediaScannerRepositoryImpl(context, trackDao, libraryDao)
        }
    }
}