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
import mx.valdora.vmusic.ui.viewmodel.FavoritesViewModel
import mx.valdora.vmusic.ui.viewmodel.PlayerViewModel
import mx.valdora.vmusic.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    searchState: SongsListState = rememberSongsListState()
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val favoritesViewModel: FavoritesViewModel = viewModel(viewModelStoreOwner = activity ?: LocalViewModelStoreOwner.current!!)
    val database = remember { VMusicDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val favoriteSongs by favoritesViewModel.songs.collectAsState()
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
                title = "Favoritos (${favoriteSongs.size})",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(AppIcons.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    SortDropdownMenu(
                        sortOrder = searchState.sortOrder,
                        onSortOrderChange = { searchState.sortOrder = it },
                        showDateOptions = false
                    )
                }
            )
        }
    ) { padding ->
        if (favoriteSongs.isEmpty()) {
            EmptyStateView(
                icon = AppIcons.FavoriteBorder,
                message = "No hay canciones favoritas",
                modifier = Modifier.padding(padding)
            )
        } else {
            SongsList(
                songs = favoriteSongs,
                currentSongId = currentSong?.id,
                isPlaying = isPlaying,
                favoriteIds = favoriteSongs.map { it.id }.toSet(),
                onSongClick = { song, index ->
                    if (currentSong?.id != song.id) {
                        playerViewModel.playQueue(favoriteSongs, index)
                    }
                },
                onToggleFavorite = { song ->
                    scope.launch(Dispatchers.IO) {
                        database.favoriteDao().removeFavorite(song.id)
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
