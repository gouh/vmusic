package mx.valdora.vmusic.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.valdora.vmusic.data.model.Album
import mx.valdora.vmusic.data.repository.MediaStoreRepository

class AlbumsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MediaStoreRepository(application)
    
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums
    
    init {
        loadAlbums()
    }
    
    fun loadAlbums() {
        viewModelScope.launch {
            _albums.value = repository.getAlbums()
        }
    }
}
