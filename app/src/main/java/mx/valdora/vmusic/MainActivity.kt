package mx.valdora.vmusic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.valdora.vmusic.ui.theme.VMusicTheme
import mx.valdora.vmusic.ui.screens.*
import mx.valdora.vmusic.ui.viewmodel.PlayerViewModel
import mx.valdora.vmusic.ui.viewmodel.SearchViewModel
import mx.valdora.vmusic.ui.components.MiniPlayer
import mx.valdora.vmusic.ui.components.SearchButton
import mx.valdora.vmusic.ui.components.SongsListState

class MainActivity : ComponentActivity() {
    
    private var permissionGranted by mutableStateOf(false)
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        permissionGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        
        if (!permissionGranted) {
            requestPermissionLauncher.launch(permission)
        }
        
        setContent {
            VMusicTheme {
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation(permissionGranted)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(permissionGranted: Boolean) {
    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity
    val playerViewModel: PlayerViewModel = viewModel(viewModelStoreOwner = activity)
    val searchViewModel: SearchViewModel = viewModel(viewModelStoreOwner = activity)
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    var showPlayerScreen by remember { mutableStateOf(false) }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showSearchButton = currentRoute == "songs"
            || currentRoute == "favorites"
            || currentRoute == "recents"
            || currentRoute?.startsWith("artists/") == true
            || currentRoute?.startsWith("albums/") == true
            || currentRoute?.startsWith("artists/") == true

    val searchState = remember {
        SongsListState().apply {
            searchQuery = searchViewModel.searchQuery
            isSearchActive = searchViewModel.isSearchActive
        }
    }
    
    LaunchedEffect(searchState.searchQuery, searchState.isSearchActive) {
        searchViewModel.searchQuery = searchState.searchQuery
        searchViewModel.isSearchActive = searchState.isSearchActive
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {},
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            NavHost(
                navController = navController, 
                startDestination = "home",
                modifier = Modifier.padding(padding),
                enterTransition = { 
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = { 
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                composable("home") { HomeScreen(navController, permissionGranted) }
                composable("songs") { SongsScreen(navController, playerViewModel, searchState) }
                composable("albums") { AlbumsScreen(navController) }
                composable("albums/{albumId}") { backStackEntry ->
                    val albumId = backStackEntry.arguments?.getString("albumId")?.toLongOrNull() ?: 0L
                    AlbumDetailScreen(navController, playerViewModel, albumId, searchState)
                }
                composable("artists") { ArtistsScreen(navController) }
                composable("artists/{artistId}") { backStackEntry ->
                    val artistId = backStackEntry.arguments?.getString("artistId")?.toLongOrNull() ?: 0L
                    ArtistDetailScreen(navController, playerViewModel, artistId, searchState)
                }
                composable("folders") { FoldersScreen(navController) }
                composable("playlists") { PlaylistsScreen(navController) }
                composable("playlists/{playlistId}") { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLongOrNull() ?: 0L
                    PlaylistDetailScreen(navController, playerViewModel, playlistId)
                }
                composable("favorites") { FavoritesScreen(navController, playerViewModel, searchState) }
                composable("recents") { RecentsScreen(navController, playerViewModel, searchState) }
            }
        }
        
        // Sticky bottom bar con MiniPlayer y SearchButton
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
                .padding(bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentSong?.let { song ->
                    Box(modifier = Modifier.weight(1f)) {
                        MiniPlayer(
                            currentSong = song,
                            isPlaying = isPlaying,
                            onPlayPauseClick = {
                                if (isPlaying) playerViewModel.pause() else playerViewModel.resume()
                            },
                            onPreviousClick = { playerViewModel.skipPrevious() },
                            onNextClick = { playerViewModel.skipNext() },
                            onPlayerClick = {
                                showPlayerScreen = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    if (showSearchButton) {
                        Box(
                            modifier = Modifier.size(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SearchButton(state = searchState)
                        }
                    }
                } ?: run {
                    if (showSearchButton) {
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier.size(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SearchButton(state = searchState)
                        }
                    }
                }
            }
        }
        
        AnimatedVisibility(
            visible = showPlayerScreen,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(300))
        ) {
            PlayerScreen(
                onDismiss = { showPlayerScreen = false },
                viewModel = playerViewModel
            )
        }
    }
}
