package mx.valdora.vmusic.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_songs")
data class FavoriteSong(
    @PrimaryKey val songId: Long
)

@Entity(tableName = "recent_songs")
data class RecentSong(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val playedAt: Long
)
