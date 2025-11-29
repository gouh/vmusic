<div align="center">
  <img src="logo.webp" alt="VMusic Logo" width="120"/>
  <h1>VMusic</h1>
</div>

A modern, feature-rich music player for Android built with the latest Android technologies. VMusic provides a smooth and intuitive experience for managing and playing your local music library.

## Why VMusic?

In an era dominated by streaming subscriptions, we've lost something fundamental: freedom. We pay monthly fees to access music, but we're locked into platforms that control what we hear and when we hear it.

VMusic is built on a different philosophy. Music is more than entertainment—it moves us, defines moments, and preserves memories. It deserves to be free from corporate control, not trapped behind paywalls. This project is a reminder that we can break free from streaming giants and reclaim what matters to us, starting with something as essential as our music collection.

Let's take back control, one song at a time.

## Overview

VMusic is designed to be a lightweight yet powerful music player that focuses on local music playback with advanced organization features. Built entirely with Jetpack Compose and Material Design 3, it offers a clean, modern interface with dark theme support.

## Key Features

### Playback
- Local music playback using Media3 ExoPlayer
- Background playback with media session support
- Playback controls: play, pause, next, previous
- Skip forward/backward 10 seconds
- Shuffle and repeat modes
- Album artwork display

### Library Organization
- **Songs**: Browse all your music tracks
- **Albums**: View music organized by albums
- **Artists**: Browse by artist
- **Playlists**: Create and manage custom playlists
- **Favorites**: Quick access to your favorite tracks
- **Recents**: See your recently played songs

### Sorting & Filtering
- Sort by date (ascending/descending)
- Sort alphabetically (A-Z, Z-A)
- Available across all library sections

### Technical Stack

**Architecture & UI**
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern declarative UI toolkit
- [Material Design 3](https://m3.material.io/) - Latest Material Design components and theming
- MVVM architecture with ViewModels
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) - Type-safe navigation

**Media Playback**
- [Media3 ExoPlayer](https://developer.android.com/media/media3/exoplayer) - High-performance media player
- [Media3 Session](https://developer.android.com/media/media3/session) - Media session management for background playback
- [MediaStyle Notifications](https://developer.android.com/reference/android/app/Notification.MediaStyle) - Rich media notifications

**Data & Storage**
- [Room Database](https://developer.android.com/training/data-storage/room) - Local database for playlists, favorites, and playback history
- [MediaStore API](https://developer.android.com/reference/android/provider/MediaStore) - System music library integration
- Kotlin Coroutines & Flow for reactive data streams

**Build & Dependencies**
- Kotlin 2.0.21
- Gradle 8.13.1 with Kotlin DSL
- Min SDK 24 (Android 7.0) / Target SDK 36
- [KSP](https://github.com/google/ksp) for annotation processing

## Project Structure

```
app/src/main/java/mx/valdora/vmusic/
├── MainActivity.kt
├── data/
│   ├── dao/                    # Room DAOs for database operations
│   │   ├── FavoriteDao.kt
│   │   ├── PlaylistDao.kt
│   │   └── RecentDao.kt
│   ├── database/
│   │   └── VMusicDatabase.kt   # Room database configuration
│   ├── entity/                 # Room entities
│   │   ├── FavoriteSong.kt
│   │   ├── Playlist.kt
│   │   ├── PlaylistSong.kt
│   │   └── RecentSong.kt
│   ├── model/                  # Data models
│   │   ├── Album.kt
│   │   ├── Artist.kt
│   │   ├── Folder.kt
│   │   └── Song.kt
│   ├── repository/
│   │   ├── MediaStoreRepository.kt  # MediaStore integration
│   │   └── RoomRepository.kt
│   └── util/
│       └── SortUtils.kt
├── player/                     # Media playback
│   ├── MusicPlayerRepository.kt
│   ├── MusicService.kt         # Background playback service
│   └── PlaybackMode.kt
└── ui/
    ├── components/             # Reusable UI components
    │   ├── AddToPlaylistDialog.kt
    │   ├── AlbumArtwork.kt
    │   ├── AppIcons.kt
    │   ├── AppTopBar.kt
    │   ├── EmptyStateView.kt
    │   ├── MiniPlayer.kt
    │   ├── PlaybackIndicator.kt
    │   ├── SongListItem.kt
    │   ├── SongsList.kt
    │   ├── SortDropdownMenu.kt
    │   └── VerticalScrollbar.kt
    ├── screens/                # App screens
    │   ├── AlbumDetailScreen.kt
    │   ├── AlbumsScreen.kt
    │   ├── ArtistDetailScreen.kt
    │   ├── ArtistsScreen.kt
    │   ├── FavoritesScreen.kt
    │   ├── FoldersScreen.kt
    │   ├── HomeScreen.kt
    │   ├── PlayerScreen.kt
    │   ├── PlaylistDetailScreen.kt
    │   ├── PlaylistsScreen.kt
    │   ├── RecentsScreen.kt
    │   └── SongsScreen.kt
    ├── theme/                  # Material Design 3 theming
    │   ├── Color.kt
    │   └── Theme.kt
    └── viewmodel/              # MVVM ViewModels
        ├── AlbumsViewModel.kt
        ├── ArtistsViewModel.kt
        ├── FavoritesViewModel.kt
        ├── PlayerViewModel.kt
        ├── PlaylistsViewModel.kt
        ├── RecentsViewModel.kt
        ├── SearchViewModel.kt
        └── SongsViewModel.kt
```

## Building

```bash
./gradlew assembleRelease
```

## License

GPL-3.0-or-later

---

Developed with ❤️ by [Hugh](https://hangouh.me)
