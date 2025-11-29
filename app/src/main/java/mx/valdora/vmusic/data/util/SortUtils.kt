package mx.valdora.vmusic.data.util

import mx.valdora.vmusic.data.model.Album
import mx.valdora.vmusic.data.model.Artist
import mx.valdora.vmusic.data.model.Song

object SortUtils {
    fun sortSongsByAlphaAsc(songs: List<Song>) = songs.sortedBy { it.title.lowercase() }
    fun sortSongsByAlphaDesc(songs: List<Song>) = songs.sortedByDescending { it.title.lowercase() }
    fun sortSongsByDateAsc(songs: List<Song>) = songs.sortedBy { it.dateAdded }
    fun sortSongsByDateDesc(songs: List<Song>) = songs.sortedByDescending { it.dateAdded }

    fun sortAlbumsByAlphaAsc(albums: List<Album>) = albums.sortedBy { it.name.lowercase() }
    fun sortAlbumsByAlphaDesc(albums: List<Album>) = albums.sortedByDescending { it.name.lowercase() }
    fun sortAlbumsByDateAsc(albums: List<Album>) = albums.sortedBy { it.year }
    fun sortAlbumsByDateDesc(albums: List<Album>) = albums.sortedByDescending { it.year }

    fun sortArtistsByAlphaAsc(artists: List<Artist>) = artists.sortedBy { it.name.lowercase() }
    fun sortArtistsByAlphaDesc(artists: List<Artist>) = artists.sortedByDescending { it.name.lowercase() }
}
