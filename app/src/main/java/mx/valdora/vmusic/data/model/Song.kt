package mx.valdora.vmusic.data.model

import android.graphics.Bitmap
import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val artistId: Long,
    val duration: Long,
    val uri: Uri,
    val dateAdded: Long,
    val folderPath: String,
    val artwork: Bitmap? = null
)
