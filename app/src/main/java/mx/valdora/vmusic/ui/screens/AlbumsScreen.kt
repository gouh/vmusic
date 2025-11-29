package mx.valdora.vmusic.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import mx.valdora.vmusic.ui.viewmodel.AlbumsViewModel
import mx.valdora.vmusic.data.repository.MediaStoreRepository
import mx.valdora.vmusic.ui.components.AppIcons
import mx.valdora.vmusic.ui.components.AppTopBar
import mx.valdora.vmusic.ui.components.SortDropdownMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(navController: NavController) {
    val viewModel: AlbumsViewModel = viewModel()
    val albums by viewModel.albums.collectAsState()
    val context = LocalContext.current
    val mediaStoreRepository = remember { MediaStoreRepository(context) }
    var sortOrder by remember { mutableStateOf("Alfa asc") }
    var viewMode by remember { mutableStateOf("grid") }
    
    val sortedAlbums = remember(albums, sortOrder) {
        when (sortOrder) {
            "Alfa asc" -> albums.sortedBy { it.name.lowercase() }
            "Alfa desc" -> albums.sortedByDescending { it.name.lowercase() }
            "Fecha desc" -> albums.sortedByDescending { it.year }
            "Fecha asc" -> albums.sortedBy { it.year }
            else -> albums
        }
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Álbumes (${albums.size})",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(AppIcons.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { viewMode = if (viewMode == "grid") "list" else "grid" }) {
                        Icon(
                            if (viewMode == "grid") AppIcons.ViewList else AppIcons.GridView,
                            contentDescription = "Cambiar vista"
                        )
                    }
                    SortDropdownMenu(
                        sortOrder = sortOrder,
                        onSortOrderChange = { sortOrder = it },
                        showDateOptions = true
                    )
                }
            )
        }
    ) { padding ->
        if (albums.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No se encontraron álbumes")
            }
        } else {
            if (viewMode == "grid") {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedAlbums.size) { index ->
                        val album = sortedAlbums[index]
                        val artwork = remember(album.id) { mediaStoreRepository.getAlbumArt(album.id) }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable { navController.navigate("albums/${album.id}") },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (artwork != null) {
                                    Image(
                                        bitmap = artwork.asImageBitmap(),
                                        contentDescription = album.name,
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
                                            AppIcons.Album,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                // Overlay con gradiente
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.7f)
                                                ),
                                                startY = 100f
                                            )
                                        )
                                )
                                
                                // Info del álbum
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = album.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = album.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${album.songCount} canciones",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(sortedAlbums) { album ->
                        val artwork = remember(album.id) { mediaStoreRepository.getAlbumArt(album.id) }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("albums/${album.id}") }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.size(80.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (artwork != null) {
                                    Image(
                                        bitmap = artwork.asImageBitmap(),
                                        contentDescription = album.name,
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
                                            AppIcons.Album,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterVertically),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = album.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = album.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${album.songCount} canciones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
