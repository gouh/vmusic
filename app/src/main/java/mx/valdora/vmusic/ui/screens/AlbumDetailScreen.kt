package mx.valdora.vmusic.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
fun AlbumDetailScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    albumId: Long,
    searchState: SongsListState = rememberSongsListState()
) {
    val context = LocalContext.current
    val mediaStoreRepository = remember { MediaStoreRepository(context) }
    val database = remember { VMusicDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    val songs = remember(albumId) { mediaStoreRepository.getSongsByAlbum(albumId) }
    val album = remember(albumId) { mediaStoreRepository.getAlbums().find { it.id == albumId } }
    val artwork = remember(albumId) { mediaStoreRepository.getAlbumArt(albumId) }
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
                title = album?.name ?: "Álbum",
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header del álbum
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.size(200.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (artwork != null) {
                            Image(
                                bitmap = artwork.asImageBitmap(),
                                contentDescription = album?.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    AppIcons.AlbumArt,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = album?.name ?: "Álbum desconocido",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = album?.artist ?: "Artista desconocido",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${songs.size} canciones",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Lista de canciones
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
                    secondaryTextProvider = { song -> formatDuration(song.duration) },
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
    }

    selectedSong?.let { song ->
        AddToPlaylistDialog(
            song = song,
            onDismiss = { selectedSong = null }
        )
    }
}

private fun formatDuration(millis: Long): String {
    if (millis <= 0) return "---"
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%d:%02d", minutes, seconds)
}
