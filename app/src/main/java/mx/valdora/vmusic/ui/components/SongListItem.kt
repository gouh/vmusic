package mx.valdora.vmusic.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mx.valdora.vmusic.data.model.Song

@Composable
fun SongListItem(
    song: Song,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    secondaryText: String? = null,
    showArtwork: Boolean = true,
    showMenu: Boolean = true,
    isFavorite: Boolean = false,
    onAddToPlaylist: ((Song) -> Unit)? = null,
    onRemoveFromPlaylist: ((Song) -> Unit)? = null,
    onToggleFavorite: ((Song) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showOptionsMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showArtwork) {
            Box {
                AlbumArtwork(
                    albumId = song.albumId,
                    size = 56.dp,
                    shape = RoundedCornerShape(8.dp)
                )
                if (isFavorite) {
                    Icon(
                        AppIcons.Favorite,
                        contentDescription = "Favorito",
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp),
                        tint = MaterialTheme.colorScheme.primary
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
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = secondaryText ?: song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        if (isCurrentSong && isPlaying) {
            Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                PlaybackIndicator()
            }
        }
        
        if (showMenu && (onAddToPlaylist != null || onRemoveFromPlaylist != null || onToggleFavorite != null)) {
            Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                IconButton(onClick = { showOptionsMenu = true }) {
                    Icon(AppIcons.MoreVert, "Opciones")
                }
                
                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false }
                ) {
                    onToggleFavorite?.let { callback ->
                        DropdownMenuItem(
                            text = { Text(if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos") },
                            onClick = {
                                showOptionsMenu = false
                                callback(song)
                            },
                            leadingIcon = { Icon(if (isFavorite) AppIcons.FavoriteBorder else AppIcons.Favorite, null) }
                        )
                    }
                    onAddToPlaylist?.let { callback ->
                        DropdownMenuItem(
                            text = { Text("Agregar a playlist") },
                            onClick = {
                                showOptionsMenu = false
                                callback(song)
                            },
                            leadingIcon = { Icon(AppIcons.PlaylistPlay, null) }
                        )
                    }
                    onRemoveFromPlaylist?.let { callback ->
                        DropdownMenuItem(
                            text = { Text("Eliminar de playlist") },
                            onClick = {
                                showOptionsMenu = false
                                callback(song)
                            },
                            leadingIcon = { Icon(AppIcons.Delete, null) }
                        )
                    }
                }
            }
        }
    }
}
