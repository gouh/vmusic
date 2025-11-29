package mx.valdora.vmusic.database

import androidx.room.*

@Dao
interface MusicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteSong)
    
    @Delete
    suspend fun removeFavorite(favorite: FavoriteSong)
    
    @Query("SELECT * FROM favorite_songs")
    suspend fun getAllFavorites(): List<FavoriteSong>
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE songId = :songId)")
    suspend fun isFavorite(songId: Long): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecent(recent: RecentSong)
    
    @Query("SELECT * FROM recent_songs ORDER BY playedAt DESC")
    suspend fun getAllRecents(): List<RecentSong>
}
