package mx.valdora.vmusic.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import mx.valdora.vmusic.data.entity.Playlist
import mx.valdora.vmusic.data.entity.PlaylistSong

@Dao
interface PlaylistDao {
    @Insert
    suspend fun createPlaylist(playlist: Playlist): Long
    
    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
    
    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)
    
    @Query("UPDATE playlists SET name = :newName WHERE id = :playlistId")
    suspend fun updatePlaylistName(playlistId: Long, newName: String)
    
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>
    
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    
    @Insert
    suspend fun addSongToPlaylist(playlistSong: PlaylistSong)
    
    @Query("SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId)")
    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean
    
    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    
    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :playlistId")
    fun getSongIdsFromPlaylist(playlistId: Long): Flow<List<Long>>
    
    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)
}
