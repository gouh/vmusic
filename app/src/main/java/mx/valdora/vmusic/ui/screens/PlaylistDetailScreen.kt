package mx.valdora.vmusic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.valdora.vmusic.data.database.VMusicDatabase
import mx.valdora.vmusic.data.entity.FavoriteSong
import mx.valdora.vmusic.data.repository.MediaStoreRepository
import mx.valdora.vmusic.ui.viewmodel.PlayerViewModel
import mx.valdora.vmusic.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    playlistId: Long,
    searchState: SongsListState = rememberSongsListState()
) {
    val context = LocalContext.current
    val mediaStoreRepository = remember { MediaStoreRepository(context) }
    val database = remember { VMusicDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    var playlist by remember { mutableStateOf<mx.valdora.vmusic.data.entity.Playlist?>(null) }
    var playlistSongs by remember { mutableStateOf(listOf<mx.valdora.vmusic.data.model.Song>()) }
    var favoriteIds by remember { mutableStateOf(setOf<Long>()) }
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    var selectedSong by remember { mutableStateOf<mx.valdora.vmusic.data.model.Song?>(null) }
    
    LaunchedEffect(Unit) {
        database.favoriteDao().getFavoriteSongIds().collect { ids ->
            favoriteIds = ids.toSet()
        }
    }
    
    LaunchedEffect(playlistId) {
        withContext(Dispatchers.IO) {
            playlist = database.playlistDao().getPlaylistById(playlistId)
        }
        database.playlistDao().getSongIdsFromPlaylist(playlistId).collect { songIds ->
            withContext(Dispatchers.IO) {
                val allSongs = mediaStoreRepository.getAllSongs()
                playlistSongs = songIds.mapNotNull { id -> allSongs.find { it.id == id } }
            }
        }
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = playlist?.name ?: "Playlist",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(AppIcons.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    SortDropdownMenu(
                        sortOrder = searchState.sortOrder,
                        onSortOrderChange = { searchState.sortOrder = it }
                    )
                }
            )
        }
    ) { padding ->
        if (playlistSongs.isEmpty()) {
            EmptyStateView(
                icon = AppIcons.QueueMusic,
                message = "Esta playlist está vacía",
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${playlistSongs.size} canciones",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                SongsList(
                    songs = playlistSongs,
                    currentSongId = currentSong?.id,
                    isPlaying = isPlaying,
                    favoriteIds = favoriteIds,
                    onSongClick = { song, index ->
                        if (currentSong?.id != song.id) {
                            playerViewModel.playQueue(playlistSongs, index)
                        }
                    },
                    onToggleFavorite = { song ->
                        scope.launch(Dispatchers.IO) {
                            if (song.id in favoriteIds) {
                                database.favoriteDao().removeFavorite(song.id)
                            } else {
                                database.favoriteDao().addFavorite(FavoriteSong(songId = song.id))
                            }
                        }
                    },
                    onAddToPlaylist = { selectedSong = it },
                    onRemoveFromPlaylist = { song ->
                        scope.launch(Dispatchers.IO) {
                            database.playlistDao().removeSongFromPlaylist(playlistId, song.id)
                        }
                    },
                    state = searchState
                )
            }
        }
    }
    
    selectedSong?.let { song ->
        AddToPlaylistDialog(
            song = song,
            onDismiss = { selectedSong = null }
        )
    }
}
