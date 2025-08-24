package com.subcoder.ftlhiresaudioplayer.di

import com.subcoder.ftlhiresaudioplayer.data.repository.PlaylistRepository
import com.subcoder.ftlhiresaudioplayer.data.repository.PlaylistRepositoryImpl
import com.subcoder.ftlhiresaudioplayer.data.repository.TrackRepository
import com.subcoder.ftlhiresaudioplayer.data.repository.TrackRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Repository Module - Binds repository interfaces to implementations
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    abstract fun bindTrackRepository(
        trackRepositoryImpl: TrackRepositoryImpl
    ): TrackRepository
    
    @Binds
    abstract fun bindPlaylistRepository(
        playlistRepositoryImpl: PlaylistRepositoryImpl
    ): PlaylistRepository
}