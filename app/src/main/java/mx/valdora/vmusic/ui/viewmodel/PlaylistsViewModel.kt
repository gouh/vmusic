package mx.valdora.vmusic.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.valdora.vmusic.data.database.VMusicDatabase
import mx.valdora.vmusic.data.entity.Playlist

class PlaylistsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = VMusicDatabase.getDatabase(application)
    private val playlistDao = database.playlistDao()
    
    val playlists: StateFlow<List<Playlist>> = playlistDao.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun loadPlaylists() {
        // Playlists are loaded automatically via Flow
    }
    
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistDao.createPlaylist(Playlist(name = name, createdAt = System.currentTimeMillis()))
        }
    }
    
    fun updatePlaylistName(playlistId: Long, newName: String) {
        viewModelScope.launch {
            playlistDao.updatePlaylistName(playlistId, newName)
        }
    }
    
    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistDao.deletePlaylistById(playlistId)
        }
    }
}
