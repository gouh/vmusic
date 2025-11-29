package mx.valdora.vmusic.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import mx.valdora.vmusic.data.entity.RecentSong

@Dao
interface RecentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecentSong(recentSong: RecentSong)
    
    @Query("SELECT * FROM recent_songs ORDER BY playedAt DESC")
    fun getAllRecentSongs(): Flow<List<RecentSong>>
    
    @Query("SELECT songId FROM recent_songs ORDER BY playedAt DESC")
    fun getRecentSongIds(): Flow<List<Long>>
    
    @Query("SELECT * FROM recent_songs ORDER BY playedAt ASC")
    fun getAllRecentSongsAsc(): Flow<List<RecentSong>>
    
    @Query("DELETE FROM recent_songs WHERE songId = :songId")
    suspend fun removeRecentSong(songId: Long)
    
    @Query("DELETE FROM recent_songs")
    suspend fun clearAllRecent()
    
    @Transaction
    suspend fun updateRecentSong(songId: Long) {
        removeRecentSong(songId)
        addRecentSong(RecentSong(songId = songId, playedAt = System.currentTimeMillis()))
    }
}
