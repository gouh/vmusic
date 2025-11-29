package mx.valdora.vmusic.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.valdora.vmusic.data.database.VMusicDatabase
import mx.valdora.vmusic.data.model.Song
import mx.valdora.vmusic.data.repository.MediaStoreRepository

class RecentsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = VMusicDatabase.getDatabase(application)
    private val mediaStoreRepository = MediaStoreRepository(application)
    
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs
    
    init {
        loadRecents()
    }
    
    private fun loadRecents() {
        viewModelScope.launch {
            database.recentDao().getRecentSongIds().collect { recentIds ->
                withContext(Dispatchers.IO) {
                    val allSongs = mediaStoreRepository.getAllSongs()
                    _songs.value = recentIds.take(50).mapNotNull { id -> allSongs.find { it.id == id } }
                }
            }
        }
    }
}
