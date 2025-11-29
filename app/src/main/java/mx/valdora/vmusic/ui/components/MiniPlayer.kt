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
fun MiniPlayer(
    currentSong: Song,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onPlayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onPlayerClick),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Artwork
            Box(modifier = Modifier.padding(start = 4.dp)) {
                AlbumArtwork(
                    albumId = currentSong.albumId,
                    size = 40.dp,
                    shape = RoundedCornerShape(8.dp)
                )
            }
            
            // Song info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = currentSong.title,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentSong.artist,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousClick) {
                    Icon(
                        AppIcons.SkipPrevious,
                        "Anterior",
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onPlayPauseClick) {
                    Icon(
                        if (isPlaying) AppIcons.Pause else AppIcons.PlayArrow,
                        "Play/Pause",
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onNextClick) {
                    Icon(
                        AppIcons.SkipNext,
                        "Siguiente",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
