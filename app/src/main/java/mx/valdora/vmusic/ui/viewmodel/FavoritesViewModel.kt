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

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val database = VMusicDatabase.getDatabase(application)
    private val mediaStoreRepository = MediaStoreRepository(application)
    
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs
    
    init {
        loadFavorites()
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            database.favoriteDao().getFavoriteSongIds().collect { favoriteIds ->
                withContext(Dispatchers.IO) {
                    val allSongs = mediaStoreRepository.getAllSongs()
                    _songs.value = allSongs.filter { it.id in favoriteIds }
                }
            }
        }
    }
}
