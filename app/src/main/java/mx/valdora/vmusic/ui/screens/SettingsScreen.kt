package mx.valdora.vmusic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mx.valdora.vmusic.ui.components.AppIcons
import mx.valdora.vmusic.ui.components.AppTopBar
import mx.valdora.vmusic.ui.theme.AccentColors
import mx.valdora.vmusic.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val appIconIndex by viewModel.appIconIndex.collectAsState()
    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }
    var isReloading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    val songsViewModel: SongsViewModel = viewModel()
    val albumsViewModel: AlbumsViewModel = viewModel()
    val artistsViewModel: ArtistsViewModel = viewModel()
    val playlistsViewModel: PlaylistsViewModel = viewModel()
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Ajustes",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                ListItem(
                    headlineContent = { Text("Volver a escanear los medios") },
                    leadingContent = { Icon(AppIcons.Refresh, null) },
                    modifier = Modifier.clickable {
                        isReloading = true
                        coroutineScope.launch {
                            songsViewModel.loadSongs()
                            albumsViewModel.loadAlbums()
                            artistsViewModel.loadArtists()
                            playlistsViewModel.loadPlaylists()
                            delay(500)
                            isReloading = false
                        }
                    }
                )
                HorizontalDivider()
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Personalización",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            item {
                ListItem(
                    headlineContent = { Text("Color de acento") },
                    supportingContent = { Text("Personaliza el color de la aplicación") },
                    leadingContent = { 
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(AccentColors[accentColorIndex])
                        )
                    },
                    trailingContent = { Icon(AppIcons.ChevronRight, null) },
                    modifier = Modifier.clickable { showColorPicker = true }
                )
                HorizontalDivider()
            }
            
            item {
                ListItem(
                    headlineContent = { Text("Icono de la aplicación") },
                    supportingContent = { Text("Cambia el icono de VMusic") },
                    leadingContent = { Icon(AppIcons.Palette, null) },
                    trailingContent = { Icon(AppIcons.ChevronRight, null) },
                    modifier = Modifier.clickable { showIconPicker = true }
                )
                HorizontalDivider()
            }
        }
    }
    
    if (showColorPicker) {
        ColorPickerDialog(
            selectedIndex = accentColorIndex,
            onColorSelected = { index ->
                viewModel.setAccentColor(index)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
    
    if (showIconPicker) {
        IconPickerDialog(
            selectedIndex = appIconIndex,
            onIconSelected = { index ->
                viewModel.setAppIcon(index)
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
    
    if (isReloading) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Cargando...")
                }
            }
        }
    }
}

@Composable
fun ColorPickerDialog(
    selectedIndex: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var previewIndex by remember { mutableIntStateOf(selectedIndex) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecciona un color") },
        text = {
            Column {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    itemsIndexed(AccentColors) { index, color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (index == previewIndex) 3.dp else 0.dp,
                                    color = if (index == previewIndex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { previewIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (index == previewIndex) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Vista previa", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Preview
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                AppIcons.MusicNote,
                                contentDescription = null,
                                tint = AccentColors[previewIndex]
                            )
                            Text("Canción de ejemplo", color = AccentColors[previewIndex])
                        }
                        
                        LinearProgressIndicator(
                            progress = { 0.5f },
                            modifier = Modifier.fillMaxWidth(),
                            color = AccentColors[previewIndex]
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {}) {
                                Icon(AppIcons.SkipPrevious, null, tint = AccentColors[previewIndex])
                            }
                            IconButton(onClick = {}) {
                                Icon(AppIcons.PlayArrow, null, tint = AccentColors[previewIndex])
                            }
                            IconButton(onClick = {}) {
                                Icon(AppIcons.SkipNext, null, tint = AccentColors[previewIndex])
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                onColorSelected(previewIndex)
            }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun IconPickerDialog(
    selectedIndex: Int,
    onIconSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var previewIndex by remember { mutableIntStateOf(selectedIndex) }
    
    val iconNames = listOf("Rosa (Predeterminado)", "Blanco", "Negro")
    val iconColors = listOf(
        Color(0xFFFF4081),
        Color.White,
        Color.Black
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecciona un icono") },
        text = {
            Column {
                iconNames.forEachIndexed { index, name ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (index == previewIndex) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { previewIndex = index }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(iconColors[index])
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    AppIcons.MusicNote,
                                    contentDescription = null,
                                    tint = if (index == 1) Color.Black else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            if (index == previewIndex) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                onIconSelected(previewIndex)
            }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
