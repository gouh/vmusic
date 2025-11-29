package mx.valdora.vmusic.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mx.valdora.vmusic.data.model.Song
import mx.valdora.vmusic.data.entity.FavoriteSong
import mx.valdora.vmusic.data.dao.FavoriteDao
import mx.valdora.vmusic.data.entity.RecentSong
import mx.valdora.vmusic.data.dao.RecentDao
import mx.valdora.vmusic.data.repository.MediaStoreRepository

class MusicPlayerRepository(
    private val context: Context,
    private val favoriteDao: FavoriteDao,
    private val recentDao: RecentDao
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private val mediaStoreRepository = MediaStoreRepository(context)
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration
    
    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode
    
    private val _shuffleMode = MutableStateFlow(ShuffleMode.OFF)
    val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode
    
    private var currentQueue: List<Song> = emptyList()
    private var positionUpdateJob: Job? = null

    fun initialize() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicService::class.java)
        )
        
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            controller?.addListener(playerListener)
            startPositionUpdates()
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
        
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateCurrentSong()
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                controller?.let {
                    _currentPosition.value = it.currentPosition
                    _duration.value = it.duration.coerceAtLeast(0L)
                }
                delay(100)
            }
        }
    }

    fun play(song: Song) {
        android.util.Log.d("MusicPlayer", "Playing song: ${song.title}")
        currentQueue = listOf(song)
        
        val songWithArt = loadArtwork(song)
        _currentSong.value = songWithArt
        android.util.Log.d("MusicPlayer", "Current song set to: ${_currentSong.value?.title}")
        
        val mediaItem = createMediaItem(songWithArt)
        controller?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
        
        saveToRecents(song.id)
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        
        android.util.Log.d("MusicPlayer", "Playing queue, starting at: ${songs[startIndex].title}")
        currentQueue = songs
        
        val songWithArt = loadArtwork(songs[startIndex])
        _currentSong.value = songWithArt
        android.util.Log.d("MusicPlayer", "Current song set to: ${_currentSong.value?.title}, artwork: ${_currentSong.value?.artwork != null}")
        
        val mediaItems = songs.map { createMediaItem(it) }
        controller?.apply {
            setMediaItems(mediaItems, startIndex, 0)
            prepare()
            play()
        }
        
        saveToRecents(songs[startIndex].id)
    }

    fun pause() {
        controller?.pause()
    }

    fun resume() {
        controller?.play()
    }

    fun seekTo(position: Long) {
        controller?.seekTo(position)
    }

    fun skipNext() {
        controller?.seekToNext()
        updateCurrentSong()
    }

    fun skipPrevious() {
        controller?.seekToPrevious()
        updateCurrentSong()
    }

    fun forward10() {
        controller?.let {
            val newPosition = (it.currentPosition + 10000).coerceAtMost(it.duration)
            it.seekTo(newPosition)
        }
    }

    fun rewind10() {
        controller?.let {
            val newPosition = (it.currentPosition - 10000).coerceAtLeast(0)
            it.seekTo(newPosition)
        }
    }

    fun toggleShuffle() {
        _shuffleMode.value = when (_shuffleMode.value) {
            ShuffleMode.OFF -> ShuffleMode.ON
            ShuffleMode.ON -> ShuffleMode.OFF
        }
        controller?.shuffleModeEnabled = _shuffleMode.value == ShuffleMode.ON
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        
        controller?.repeatMode = when (_repeatMode.value) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    suspend fun toggleFavorite(songId: Long) {
        if (favoriteDao.isFavorite(songId)) {
            favoriteDao.removeFavorite(songId)
        } else {
            favoriteDao.addFavorite(FavoriteSong(songId = songId))
        }
    }

    suspend fun isFavorite(songId: Long): Boolean {
        return favoriteDao.isFavorite(songId)
    }

    private fun saveToRecents(songId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            recentDao.updateRecentSong(songId)
        }
    }

    private fun updateCurrentSong() {
        controller?.currentMediaItem?.let { mediaItem ->
            val index = controller?.currentMediaItemIndex ?: 0
            if (index < currentQueue.size) {
                val currentSongValue = _currentSong.value
                val queueSong = currentQueue[index]
                
                // Si ya es la misma canción con artwork, no recargar
                if (currentSongValue?.id == queueSong.id && currentSongValue.artwork != null) {
                    return
                }
                
                val songWithArt = loadArtwork(queueSong)
                _currentSong.value = songWithArt
                saveToRecents(queueSong.id)
            }
        }
    }

    private fun loadArtwork(song: Song): Song {
        android.util.Log.d("MusicPlayer", "Loading artwork for: ${song.title}, albumId: ${song.albumId}")
        if (song.artwork != null) {
            android.util.Log.d("MusicPlayer", "Artwork already loaded")
            return song
        }
        
        val artwork = mediaStoreRepository.getSongArt(song)
        if (artwork != null) {
            android.util.Log.d("MusicPlayer", "Artwork loaded successfully: ${artwork.width}x${artwork.height}")
        } else {
            android.util.Log.d("MusicPlayer", "No artwork found")
        }
        
        return song.copy(artwork = artwork)
    }

    private fun createMediaItem(song: Song): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .build()
        
        return MediaItem.Builder()
            .setUri(song.uri)
            .setMediaMetadata(metadata)
            .build()
    }

    fun release() {
        positionUpdateJob?.cancel()
        controller?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture ?: return)
    }
}
