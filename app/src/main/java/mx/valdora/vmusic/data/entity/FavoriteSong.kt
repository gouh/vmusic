package mx.valdora.vmusic.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_songs")
data class FavoriteSong(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long
)
