package mx.valdora.vmusic.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.valdora.vmusic.data.model.Song
import mx.valdora.vmusic.data.repository.MediaStoreRepository

class SongsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MediaStoreRepository(application)
    
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs
    
    init {
        loadSongs()
    }
    
    fun loadSongs() {
        viewModelScope.launch {
            _songs.value = repository.getAllSongs()
        }
    }
}
