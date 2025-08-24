package com.ftl.hires.audioplayer.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    
    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideTrackSelector(): DefaultTrackSelector {
        return DefaultTrackSelector.Builder()
            .build()
            .apply {
                // Configure for hi-res audio preference
                setParameters(
                    buildUponParameters()
                        .setMaxAudioBitrate(Int.MAX_VALUE) // No bitrate limit for hi-res
                        .setMaxAudioChannelCount(8) // Support up to 7.1 surround
                        .build()
                )
            }
    }
    
    @Provides
    @Singleton
    fun provideAudioSink(@ApplicationContext context: Context, audioAttributes: AudioAttributes): AudioSink {
        return DefaultAudioSink.Builder()
            .setAudioAttributes(audioAttributes, true)
            .setEnableFloatOutput(true) // Enable float output for better quality
            .setEnableAudioTrackPlaybackParams(true)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        trackSelector: DefaultTrackSelector,
        audioAttributes: AudioAttributes
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): MediaSession {
        return MediaSession.Builder(context, player)
            .build()
    }
}