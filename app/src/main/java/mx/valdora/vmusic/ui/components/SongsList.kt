package mx.valdora.vmusic.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import mx.valdora.vmusic.data.model.Song

class SongsListState {
    var searchQuery by mutableStateOf("")
    var isSearchActive by mutableStateOf(false)
    var sortOrder by mutableStateOf("Alfa asc")
}

@Composable
fun rememberSongsListState() = remember { SongsListState() }

@Composable
fun SearchButton(
    state: SongsListState,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = {
            state.isSearchActive = !state.isSearchActive
            if (!state.isSearchActive) {
                state.searchQuery = ""
            }
        },
        modifier = modifier
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(65.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (state.isSearchActive) AppIcons.Close else AppIcons.Search,
                    if (state.isSearchActive) "Cerrar búsqueda" else "Buscar",
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    }
}

@Composable
fun SongsList(
    songs: List<Song>,
    currentSongId: Long?,
    isPlaying: Boolean,
    favoriteIds: Set<Long>,
    onSongClick: (Song, Int) -> Unit,
    onToggleFavorite: ((Song) -> Unit)? = null,
    onAddToPlaylist: ((Song) -> Unit)? = null,
    onRemoveFromPlaylist: ((Song) -> Unit)? = null,
    modifier: Modifier = Modifier,
    secondaryTextProvider: (Song) -> String = { song -> "${song.artist} • ${song.album}" },
    state: SongsListState = rememberSongsListState()
) {
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    val filteredSongs = remember(songs, state.searchQuery, state.sortOrder) {
        val filtered = if (state.searchQuery.isBlank()) {
            songs
        } else {
            songs.filter { song ->
                song.title.contains(state.searchQuery, ignoreCase = true) ||
                song.artist.contains(state.searchQuery, ignoreCase = true) ||
                song.album.contains(state.searchQuery, ignoreCase = true)
            }
        }
        
        when (state.sortOrder) {
            "Alfa asc" -> filtered.sortedBy { it.title.lowercase() }
            "Alfa desc" -> filtered.sortedByDescending { it.title.lowercase() }
            "Fecha desc" -> filtered.sortedByDescending { it.dateAdded }
            "Fecha asc" -> filtered.sortedBy { it.dateAdded }
            else -> filtered
        }
    }
    
    LaunchedEffect(state.isSearchActive) {
        if (state.isSearchActive) {
            focusRequester.requestFocus()
        } else {
            keyboardController?.hide()
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        if (state.isSearchActive) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { state.searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("Buscar canciones...") },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { state.searchQuery = "" }) {
                            Icon(AppIcons.Close, "Limpiar")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { keyboardController?.hide() }
                )
            )
        }
        
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(filteredSongs) { index, song ->
                    val originalIndex = songs.indexOf(song)
                    SongListItem(
                        song = song,
                        isCurrentSong = currentSongId == song.id,
                        isPlaying = isPlaying,
                        isFavorite = song.id in favoriteIds,
                        onClick = { onSongClick(song, originalIndex) },
                        secondaryText = secondaryTextProvider(song),
                        onToggleFavorite = onToggleFavorite,
                        onAddToPlaylist = onAddToPlaylist,
                        onRemoveFromPlaylist = onRemoveFromPlaylist
                    )
                    HorizontalDivider()
                }
                
                // Espaciador para que el último item no quede oculto por el MiniPlayer
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
            
            if (songs.size >= 50) {
                VerticalScrollbar(
                    listState = listState,
                    itemCount = filteredSongs.size,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}
