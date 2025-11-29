package mx.valdora.vmusic.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.valdora.vmusic.data.repository.MediaStoreRepository

private val artworkCache = mutableMapOf<Long, Bitmap?>()

@Composable
fun AlbumArtwork(
    albumId: Long,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    shape: Shape = RoundedCornerShape(8.dp),
    iconSize: Dp = (size.value * 0.4f).dp
) {
    val context = LocalContext.current
    var artwork by remember(albumId) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(albumId) { mutableStateOf(true) }
    
    LaunchedEffect(albumId) {
        if (artworkCache.containsKey(albumId)) {
            artwork = artworkCache[albumId]
            isLoading = false
        } else {
            withContext(Dispatchers.IO) {
                val mediaStoreRepository = MediaStoreRepository(context)
                val loadedArtwork = mediaStoreRepository.getAlbumArt(albumId)
                artworkCache[albumId] = loadedArtwork
                artwork = loadedArtwork
                isLoading = false
            }
        }
    }
    
    Card(
        modifier = modifier.size(size),
        shape = shape
    ) {
        if (!isLoading && artwork != null) {
            Image(
                bitmap = artwork!!.asImageBitmap(),
                contentDescription = "Album Art",
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
                    contentDescription = "Sin portada",
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
