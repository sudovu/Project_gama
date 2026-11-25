package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        FavoriteTrackEntity::class,
        RecentPlaybackEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        SearchHistoryEntity::class,
        UserPreferenceEntity::class,
        UploadedTrackEntity::class,
        DiscoveredTrackEntity::class,
        TrackPlayStatEntity::class,
        CuePointEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class GammaDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentPlaybackDao(): RecentPlaybackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun userPreferenceDao(): UserPreferenceDao
    abstract fun uploadedTrackDao(): UploadedTrackDao
    abstract fun discoveryCacheDao(): DiscoveryCacheDao
    abstract fun trackPlayStatDao(): TrackPlayStatDao
    abstract fun cuePointDao(): CuePointDao

    companion object {
        @Volatile
        private var INSTANCE: GammaDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cue_points` (
                        `id` TEXT NOT NULL,
                        `trackId` TEXT NOT NULL,
                        `positionMs` INTEGER NOT NULL,
                        `label` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cue_points_trackId` ON `cue_points` (`trackId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cue_points_positionMs` ON `cue_points` (`positionMs`)")
            }
        }

        fun getInstance(context: Context): GammaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GammaDatabase::class.java,
                    "gamma_music.db"
                )
                    .addMigrations(MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
