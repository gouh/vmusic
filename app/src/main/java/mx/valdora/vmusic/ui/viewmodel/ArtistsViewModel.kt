package mx.valdora.vmusic.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.valdora.vmusic.data.model.Artist
import mx.valdora.vmusic.data.repository.MediaStoreRepository

class ArtistsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MediaStoreRepository(application)
    
    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists
    
    init {
        loadArtists()
    }
    
    fun loadArtists() {
        viewModelScope.launch {
            _artists.value = repository.getArtists()
        }
    }
}
