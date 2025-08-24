package com.ftl.hires.audioplayer.di

import android.content.Context
import com.ftl.hires.audioplayer.data.database.dao.LibraryDao
import com.ftl.hires.audioplayer.data.database.dao.TrackDao
import com.ftl.hires.audioplayer.data.repository.MediaScannerRepository
import com.ftl.hires.audioplayer.data.repository.MediaScannerRepositoryImpl
import com.ftl.hires.audioplayer.data.scanner.ArtworkExtractor
import com.ftl.hires.audioplayer.data.scanner.LibraryIndexer
import com.ftl.hires.audioplayer.data.scanner.MetadataExtractor
import com.ftl.hires.audioplayer.data.scanner.ScanProgressTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScannerModule {

    @Provides
    @Singleton
    fun provideMetadataExtractor(
        @ApplicationContext context: Context
    ): MetadataExtractor {
        return MetadataExtractor(context)
    }

    @Provides
    @Singleton
    fun provideArtworkExtractor(
        @ApplicationContext context: Context
    ): ArtworkExtractor {
        return ArtworkExtractor(context)
    }

    @Provides
    @Singleton
    fun provideScanProgressTracker(): ScanProgressTracker {
        return ScanProgressTracker()
    }

    @Provides
    @Singleton
    fun provideLibraryIndexer(
        trackDao: TrackDao,
        libraryDao: LibraryDao,
        metadataExtractor: MetadataExtractor,
        artworkExtractor: ArtworkExtractor
    ): LibraryIndexer {
        return LibraryIndexer(
            trackDao = trackDao,
            libraryDao = libraryDao,
            metadataExtractor = metadataExtractor,
            artworkExtractor = artworkExtractor
        )
    }

    @Provides
    @Singleton
    fun provideMediaScannerRepository(
        @ApplicationContext context: Context,
        trackDao: TrackDao,
        libraryDao: LibraryDao,
        metadataExtractor: MetadataExtractor,
        artworkExtractor: ArtworkExtractor,
        libraryIndexer: LibraryIndexer,
        scanProgressTracker: ScanProgressTracker
    ): MediaScannerRepository {
        return MediaScannerRepositoryImpl(
            context = context,
            trackDao = trackDao,
            libraryDao = libraryDao,
            metadataExtractor = metadataExtractor,
            artworkExtractor = artworkExtractor,
            libraryIndexer = libraryIndexer,
            scanProgressTracker = scanProgressTracker
        )
    }
}