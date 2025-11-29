package mx.valdora.vmusic.data.repository

import kotlinx.coroutines.flow.Flow
import mx.valdora.vmusic.data.dao.FavoriteDao
import mx.valdora.vmusic.data.dao.PlaylistDao
import mx.valdora.vmusic.data.dao.RecentDao
import mx.valdora.vmusic.data.entity.FavoriteSong
import mx.valdora.vmusic.data.entity.Playlist
import mx.valdora.vmusic.data.entity.PlaylistSong
import mx.valdora.vmusic.data.entity.RecentSong

class RoomRepository(
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    private val recentDao: RecentDao
) {
    // Playlist operations
    suspend fun createPlaylist(name: String): Long {
        return playlistDao.createPlaylist(Playlist(name = name))
    }
    
    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylistById(playlistId)
    }
    
    fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists()
    }
    
    suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return playlistDao.getPlaylistById(playlistId)
    }
    
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(PlaylistSong(playlistId = playlistId, songId = songId))
    }
    
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }
    
    fun getSongsFromPlaylist(playlistId: Long): Flow<List<Long>> {
        return playlistDao.getSongIdsFromPlaylist(playlistId)
    }
    
    suspend fun clearPlaylist(playlistId: Long) {
        playlistDao.clearPlaylist(playlistId)
    }
    
    // Favorite operations
    suspend fun toggleFavorite(songId: Long) {
        favoriteDao.toggleFavorite(songId)
    }
    
    suspend fun addFavorite(songId: Long) {
        favoriteDao.addFavorite(FavoriteSong(songId = songId))
    }
    
    suspend fun removeFavorite(songId: Long) {
        favoriteDao.removeFavorite(songId)
    }
    
    fun getAllFavorites(): Flow<List<FavoriteSong>> {
        return favoriteDao.getAllFavorites()
    }
    
    fun getFavoriteSongIds(): Flow<List<Long>> {
        return favoriteDao.getFavoriteSongIds()
    }
    
    suspend fun isFavorite(songId: Long): Boolean {
        return favoriteDao.isFavorite(songId)
    }
    
    // Recent operations
    suspend fun addRecentSong(songId: Long) {
        recentDao.updateRecentSong(songId)
    }
    
    fun getAllRecentSongs(): Flow<List<RecentSong>> {
        return recentDao.getAllRecentSongs()
    }
    
    fun getRecentSongIds(): Flow<List<Long>> {
        return recentDao.getRecentSongIds()
    }
    
    fun getAllRecentSongsAsc(): Flow<List<RecentSong>> {
        return recentDao.getAllRecentSongsAsc()
    }
    
    suspend fun removeRecentSong(songId: Long) {
        recentDao.removeRecentSong(songId)
    }
    
    suspend fun clearAllRecent() {
        recentDao.clearAllRecent()
    }
    
    // Sorting utilities for lists (generic)
    fun <T> sortByAlphaAsc(list: List<T>, selector: (T) -> String): List<T> {
        return list.sortedBy { selector(it).lowercase() }
    }
    
    fun <T> sortByAlphaDesc(list: List<T>, selector: (T) -> String): List<T> {
        return list.sortedByDescending { selector(it).lowercase() }
    }
    
    fun <T> sortByDateAsc(list: List<T>, selector: (T) -> Long): List<T> {
        return list.sortedBy { selector(it) }
    }
    
    fun <T> sortByDateDesc(list: List<T>, selector: (T) -> Long): List<T> {
        return list.sortedByDescending { selector(it) }
    }
}
