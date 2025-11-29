package mx.valdora.vmusic.data.repository

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import mx.valdora.vmusic.data.model.Album
import mx.valdora.vmusic.data.model.Artist
import mx.valdora.vmusic.data.model.Folder
import mx.valdora.vmusic.data.model.Song
import java.io.File

class MediaStoreRepository(private val context: Context) {

    fun getAllSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED
        )

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val artistIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val albumId = cursor.getLong(albumIdColumn)
                val artistId = cursor.getLong(artistIdColumn)
                val duration = cursor.getLong(durationColumn)
                val data = cursor.getString(dataColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val folderPath = File(data).parent ?: ""

                songs.add(Song(id, title, artist, album, albumId, artistId, duration, uri, dateAdded, folderPath, null))
            }
        }
        return songs
    }

    fun getAlbums(): List<Album> {
        val albums = mutableListOf<Album>()
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums.FIRST_YEAR
        )

        context.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
            val songCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(albumColumn) ?: "Unknown Album"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val songCount = cursor.getInt(songCountColumn)
                val year = cursor.getInt(yearColumn)

                albums.add(Album(id, name, artist, songCount, year))
            }
        }
        return albums
    }

    fun getArtists(): List<Artist> {
        val artists = mutableListOf<Artist>()
        val projection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        )

        context.contentResolver.query(
            MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
            val albumCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
            val trackCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown Artist"
                val albumCount = cursor.getInt(albumCountColumn)
                val trackCount = cursor.getInt(trackCountColumn)

                artists.add(Artist(id, name, albumCount, trackCount))
            }
        }
        return artists
    }

    fun getFolders(): List<Folder> {
        val folderMap = mutableMapOf<String, Int>()
        getAllSongs().forEach { song ->
            val path = song.folderPath
            folderMap[path] = (folderMap[path] ?: 0) + 1
        }
        return folderMap.map { (path, count) ->
            val name = File(path).name
            Folder(path, name, count)
        }
    }

    fun getSongsByAlbum(albumId: Long): List<Song> {
        return getAllSongs().filter { it.albumId == albumId }
    }

    fun getSongsByArtist(artistId: Long): List<Song> {
        return getAllSongs().filter { it.artistId == artistId }
    }

    fun getSongsByFolder(path: String): List<Song> {
        return getAllSongs().filter { it.folderPath == path }
    }

    fun getAlbumArt(albumId: Long): Bitmap? {
        val uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getSongArt(song: Song): Bitmap? {
        getAlbumArt(song.albumId)?.let { return it }
        
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, song.uri)
            val art = retriever.embeddedPicture
            retriever.release()
            art?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        } catch (e: Exception) {
            null
        }
    }

    fun searchSongs(query: String): List<Song> {
        val lowerQuery = query.lowercase()
        return getAllSongs().filter {
            it.title.lowercase().contains(lowerQuery) ||
            it.artist.lowercase().contains(lowerQuery) ||
            it.album.lowercase().contains(lowerQuery)
        }
    }

    fun searchAlbums(query: String): List<Album> {
        val lowerQuery = query.lowercase()
        return getAlbums().filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.artist.lowercase().contains(lowerQuery)
        }
    }

    fun searchArtists(query: String): List<Artist> {
        val lowerQuery = query.lowercase()
        return getArtists().filter {
            it.name.lowercase().contains(lowerQuery)
        }
    }
}
