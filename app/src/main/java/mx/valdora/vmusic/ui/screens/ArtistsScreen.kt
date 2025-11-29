package mx.valdora.vmusic.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import mx.valdora.vmusic.ui.viewmodel.ArtistsViewModel
import mx.valdora.vmusic.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistsScreen(navController: NavController) {
    val viewModel: ArtistsViewModel = viewModel()
    val artists by viewModel.artists.collectAsState()
    var sortOrder by remember { mutableStateOf("Alfa asc") }
    
    val sortedArtists = remember(artists, sortOrder) {
        when (sortOrder) {
            "Alfa asc" -> artists.sortedBy { it.name.lowercase() }
            "Alfa desc" -> artists.sortedByDescending { it.name.lowercase() }
            else -> artists
        }
    }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Artistas (${artists.size})",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(AppIcons.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    SortDropdownMenu(
                        sortOrder = sortOrder,
                        onSortOrderChange = { sortOrder = it },
                        showDateOptions = false
                    )
                }
            )
        }
    ) { padding ->
        if (artists.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No se encontraron artistas")
            }
        } else {
            val listState = rememberLazyListState()
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sortedArtists) { artist ->
                        ListItem(
                            headlineContent = { Text(artist.name) },
                            supportingContent = { Text("${artist.trackCount} canciones • ${artist.albumCount} álbumes") },
                            modifier = Modifier.clickable { navController.navigate("artists/${artist.id}") }
                        )
                        HorizontalDivider()
                    }
                }
                
                VerticalScrollbar(
                    listState = listState,
                    itemCount = sortedArtists.size,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}
