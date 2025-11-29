package mx.valdora.vmusic.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.valdora.vmusic.data.dao.FavoriteDao
import mx.valdora.vmusic.data.dao.PlaylistDao
import mx.valdora.vmusic.data.dao.RecentDao
import mx.valdora.vmusic.data.entity.FavoriteSong
import mx.valdora.vmusic.data.entity.Playlist
import mx.valdora.vmusic.data.entity.PlaylistSong
import mx.valdora.vmusic.data.entity.RecentSong

@Database(
    entities = [
        Playlist::class,
        PlaylistSong::class,
        FavoriteSong::class,
        RecentSong::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VMusicDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentDao(): RecentDao
    
    companion object {
        @Volatile
        private var INSTANCE: VMusicDatabase? = null
        
        fun getDatabase(context: Context): VMusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VMusicDatabase::class.java,
                    "vmusic_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
