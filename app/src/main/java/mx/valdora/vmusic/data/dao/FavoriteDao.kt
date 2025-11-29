package mx.valdora.vmusic.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import mx.valdora.vmusic.data.entity.FavoriteSong

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favoriteSong: FavoriteSong)
    
    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun removeFavorite(songId: Long)
    
    @Query("SELECT * FROM favorite_songs")
    fun getAllFavorites(): Flow<List<FavoriteSong>>
    
    @Query("SELECT songId FROM favorite_songs")
    fun getFavoriteSongIds(): Flow<List<Long>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE songId = :songId)")
    suspend fun isFavorite(songId: Long): Boolean
    
    @Transaction
    suspend fun toggleFavorite(songId: Long) {
        if (isFavorite(songId)) {
            removeFavorite(songId)
        } else {
            addFavorite(FavoriteSong(songId = songId))
        }
    }
}
