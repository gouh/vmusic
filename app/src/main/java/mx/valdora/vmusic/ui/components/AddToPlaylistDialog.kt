package mx.valdora.vmusic.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mx.valdora.vmusic.data.database.VMusicDatabase
import mx.valdora.vmusic.data.entity.Playlist
import mx.valdora.vmusic.data.entity.PlaylistSong
import mx.valdora.vmusic.data.model.Song

@Composable
fun AddToPlaylistDialog(
    song: Song,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { VMusicDatabase.getDatabase(context) }
    val playlists by database.playlistDao().getAllPlaylists().collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar a playlist") },
        text = {
            Column {
                TextButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(AppIcons.Add, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crear nueva playlist")
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                if (playlists.isEmpty()) {
                    Text(
                        "No hay playlists creadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(playlists) { playlist ->
                            ListItem(
                                headlineContent = { Text(playlist.name) },
                                leadingContent = { Icon(AppIcons.QueueMusic, null) },
                                modifier = Modifier.clickable {
                                    scope.launch {
                                        val alreadyExists = database.playlistDao().isSongInPlaylist(playlist.id, song.id)
                                        if (!alreadyExists) {
                                            database.playlistDao().addSongToPlaylist(
                                                PlaylistSong(playlistId = playlist.id, songId = song.id)
                                            )
                                        }
                                        onDismiss()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
    
    if (showCreateDialog) {
        var playlistName by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nueva playlist") },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            scope.launch {
                                val playlistId = database.playlistDao().createPlaylist(
                                    Playlist(name = playlistName, createdAt = System.currentTimeMillis())
                                )
                                database.playlistDao().addSongToPlaylist(
                                    PlaylistSong(playlistId = playlistId, songId = song.id)
                                )
                                showCreateDialog = false
                                onDismiss()
                            }
                        }
                    }
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
