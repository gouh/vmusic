package mx.valdora.vmusic.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mx.valdora.vmusic.data.database.VMusicDatabase
import mx.valdora.vmusic.data.repository.MediaStoreRepository
import mx.valdora.vmusic.ui.components.*
import mx.valdora.vmusic.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, permissionGranted: Boolean) {
    val context = LocalContext.current
    val mediaStoreRepository = remember { MediaStoreRepository(context) }
    val database = remember { VMusicDatabase.getDatabase(context) }
    
    val songsViewModel: SongsViewModel = viewModel()
    val albumsViewModel: AlbumsViewModel = viewModel()
    val artistsViewModel: ArtistsViewModel = viewModel()
    val playlistsViewModel: PlaylistsViewModel = viewModel()
    
    val songs by songsViewModel.songs.collectAsState()
    val albums by albumsViewModel.albums.collectAsState()
    val artists by artistsViewModel.artists.collectAsState()
    val playlists by playlistsViewModel.playlists.collectAsState()
    
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            songsViewModel.loadSongs()
            albumsViewModel.loadAlbums()
            artistsViewModel.loadArtists()
            playlistsViewModel.loadPlaylists()
        }
    }
    
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            songsViewModel.loadSongs()
            albumsViewModel.loadAlbums()
            artistsViewModel.loadArtists()
            playlistsViewModel.loadPlaylists()
            pullToRefreshState.endRefresh()
        }
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "VMusic",
                centered = true,
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(AppIcons.MoreVert, "Ajustes")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Playlists
                item {
                    ListItem(
                        headlineContent = { Text("Playlists") },
                        supportingContent = { Text("${playlists.size} playlists") },
                        leadingContent = { Icon(AppIcons.PlaylistPlay, null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(AppIcons.ChevronRight, null) },
                        modifier = Modifier.clickable { navController.navigate("playlists") }
                    )
                    HorizontalDivider()
                }
                
                // Artistas
                item {
                    ListItem(
                        headlineContent = { Text("Artistas") },
                        supportingContent = { Text("${artists.size} artistas") },
                        leadingContent = { Icon(AppIcons.Person, null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(AppIcons.ChevronRight, null) },
                        modifier = Modifier.clickable { navController.navigate("artists") }
                    )
                    HorizontalDivider()
                }
                
                // Álbumes
                item {
                    ListItem(
                        headlineContent = { Text("Álbumes") },
                        supportingContent = { Text("${albums.size} álbumes") },
                        leadingContent = { Icon(AppIcons.Album, null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(AppIcons.ChevronRight, null) },
                        modifier = Modifier.clickable { navController.navigate("albums") }
                    )
                    HorizontalDivider()
                }
                
                // Canciones
                item {
                    ListItem(
                        headlineContent = { Text("Canciones") },
                        supportingContent = { Text("${songs.size} canciones") },
                        leadingContent = { Icon(AppIcons.MusicNote, null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(AppIcons.ChevronRight, null) },
                        modifier = Modifier.clickable { navController.navigate("songs") }
                    )
                    HorizontalDivider()
                }
            }
            
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
