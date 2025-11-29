package mx.valdora.vmusic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.valdora.vmusic.data.database.VMusicDatabase
import mx.valdora.vmusic.data.entity.FavoriteSong
import mx.valdora.vmusic.data.repository.MediaStoreRepository
import mx.valdora.vmusic.ui.viewmodel.PlayerViewModel
import mx.valdora.vmusic.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    artistId: Long,
    searchState: SongsListState = rememberSongsListState()
) {
    val context = LocalContext.current
    val mediaStoreRepository = remember { MediaStoreRepository(context) }
    val database = remember { VMusicDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val songs = remember(artistId) { mediaStoreRepository.getSongsByArtist(artistId) }
    val artist = remember(artistId) { mediaStoreRepository.getArtists().find { it.id == artistId } }
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
                title = artist?.name ?: "Artista",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = artist?.name ?: "Artista desconocido",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "${songs.size} canciones • ${artist?.albumCount ?: 0} álbumes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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
