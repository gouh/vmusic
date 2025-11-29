package mx.valdora.vmusic.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.valdora.vmusic.data.database.VMusicDatabase
import mx.valdora.vmusic.data.entity.FavoriteSong
import mx.valdora.vmusic.ui.viewmodel.SongsViewModel
import mx.valdora.vmusic.ui.viewmodel.PlayerViewModel
import mx.valdora.vmusic.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    navController: NavController, 
    playerViewModel: PlayerViewModel,
    searchState: SongsListState = rememberSongsListState()
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val songsViewModel: SongsViewModel = viewModel(viewModelStoreOwner = activity ?: LocalViewModelStoreOwner.current!!)
    val database = remember { VMusicDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val songs by songsViewModel.songs.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    var favoriteIds by remember { mutableStateOf(setOf<Long>()) }
    var selectedSong by remember { mutableStateOf<mx.valdora.vmusic.data.model.Song?>(null) }
    
    LaunchedEffect(Unit) {
        database.favoriteDao().getFavoriteSongIds().collect { ids ->
            favoriteIds = ids.toSet()
        }
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Canciones (${songs.size})",
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
        if (songs.isEmpty()) {
            EmptyStateView(
                icon = AppIcons.MusicNote,
                message = "No se encontraron canciones",
                modifier = Modifier.padding(padding)
            )
        } else {
            SongsList(
                songs = songs,
                currentSongId = currentSong?.id,
                isPlaying = isPlaying,
                favoriteIds = favoriteIds,
                onSongClick = { song, index ->
                    if (currentSong?.id != song.id) {
                        playerViewModel.playQueue(songs, index)
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
                modifier = Modifier.padding(padding),
                state = searchState
            )
        }
    }
    
    selectedSong?.let { song ->
        AddToPlaylistDialog(
            song = song,
            onDismiss = { selectedSong = null }
        )
    }
}
