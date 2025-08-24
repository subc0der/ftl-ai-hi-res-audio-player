package com.ftl.hires.audioplayer.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.ftl.hires.audioplayer.data.database.entities.Track
import com.ftl.hires.audioplayer.data.database.entities.Artist
import com.ftl.hires.audioplayer.data.database.entities.Album
import com.ftl.hires.audioplayer.data.database.entities.Playlist
import com.ftl.hires.audioplayer.data.database.entities.PlaylistTrack
import com.ftl.hires.audioplayer.data.database.dao.TrackDao
import com.ftl.hires.audioplayer.data.database.dao.PlaylistDao
import com.ftl.hires.audioplayer.data.database.dao.LibraryDao
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Track::class,
        Artist::class,
        Album::class,
        Playlist::class,
        PlaylistTrack::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DatabaseConverters::class)
abstract class FTLAudioDatabase : RoomDatabase() {
    
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun libraryDao(): LibraryDao
    
    companion object {
        const val DATABASE_NAME = "ftl_audio_database"
        
        @Volatile
        private var INSTANCE: FTLAudioDatabase? = null
        
        fun getDatabase(context: Context): FTLAudioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FTLAudioDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(
                        // Future migrations will be added here
                    )
                    .fallbackToDestructiveMigration() // Remove in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        // Migration examples for future versions
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example migration - will be implemented when needed
                // database.execSQL("ALTER TABLE tracks ADD COLUMN new_column TEXT")
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example migration - will be implemented when needed
                // database.execSQL("CREATE INDEX index_tracks_new_column ON tracks(new_column)")
            }
        }
    }
}

class DatabaseConverters {
    // Room type converters for complex data types if needed in the future
    // Example:
    // @TypeConverter
    // fun fromStringList(value: List<String>): String {
    //     return Gson().toJson(value)
    // }
    // 
    // @TypeConverter
    // fun toStringList(value: String): List<String> {
    //     return Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
    // }
}