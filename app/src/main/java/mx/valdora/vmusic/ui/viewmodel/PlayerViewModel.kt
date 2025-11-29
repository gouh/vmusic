package mx.valdora.vmusic.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.valdora.vmusic.data.model.Song
import mx.valdora.vmusic.data.database.VMusicDatabase
import mx.valdora.vmusic.player.MusicPlayerRepository

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = VMusicDatabase.getDatabase(application)
    private val repository = MusicPlayerRepository(
        application,
        database.favoriteDao(),
        database.recentDao()
    )
    
    val currentSong: StateFlow<Song?> = repository.currentSong
    val isPlaying: StateFlow<Boolean> = repository.isPlaying
    val currentPosition: StateFlow<Long> = repository.currentPosition
    val duration: StateFlow<Long> = repository.duration
    val repeatMode: StateFlow<mx.valdora.vmusic.player.RepeatMode> = repository.repeatMode
    val shuffleMode: StateFlow<mx.valdora.vmusic.player.ShuffleMode> = repository.shuffleMode
    
    init {
        repository.initialize()
    }
    
    fun playSong(song: Song) {
        repository.play(song)
    }
    
    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        repository.playQueue(songs, startIndex)
    }
    
    fun pause() {
        repository.pause()
    }
    
    fun resume() {
        repository.resume()
    }
    
    fun skipNext() {
        repository.skipNext()
    }
    
    fun skipPrevious() {
        repository.skipPrevious()
    }
    
    fun forward10() {
        repository.forward10()
    }
    
    fun rewind10() {
        repository.rewind10()
    }
    
    fun seekTo(position: Long) {
        repository.seekTo(position)
    }
    
    fun toggleShuffle() {
        repository.toggleShuffle()
    }
    
    fun toggleRepeat() {
        repository.toggleRepeat()
    }
    
    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(songId)
        }
    }
    
    suspend fun isFavorite(songId: Long): Boolean {
        return repository.isFavorite(songId)
    }
    
    override fun onCleared() {
        super.onCleared()
        repository.release()
    }
}
