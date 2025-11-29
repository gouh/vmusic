package mx.valdora.vmusic.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import mx.valdora.vmusic.ui.components.AppIcons
import mx.valdora.vmusic.ui.viewmodel.PlaylistsViewModel
import mx.valdora.vmusic.ui.components.AppTopBar
import mx.valdora.vmusic.ui.components.SortDropdownMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(navController: NavController) {
    val viewModel: PlaylistsViewModel = viewModel()
    val playlists by viewModel.playlists.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf("Alfa asc") }
    
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Playlists",
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(AppIcons.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(AppIcons.Add, contentDescription = "Crear playlist")
                    }
                    SortDropdownMenu(
                        sortOrder = sortOrder,
                        onSortOrderChange = { sortOrder = it },
                        showDateOptions = false
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Playlists preestablecidas
            item {
                Text(
                    "Playlists del sistema",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
                )
            }
            
            item {
                ListItem(
                    headlineContent = { Text("Favoritos") },
                    leadingContent = { Icon(AppIcons.FavoriteBorder, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { navController.navigate("favorites") }
                )
                HorizontalDivider()
            }
            
            item {
                ListItem(
                    headlineContent = { Text("Últimos reproducidos") },
                    leadingContent = { Icon(AppIcons.History, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { navController.navigate("recents") }
                )
                HorizontalDivider()
            }
            
            // Playlists personalizadas
            if (playlists.isNotEmpty()) {
                item {
                    Text(
                        "Mis playlists (${playlists.size})",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
                    )
                }
                
                items(playlists) { playlist ->
                    var showOptionsMenu by remember { mutableStateOf(false) }
                    var showEditDialog by remember { mutableStateOf(false) }
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    
                    ListItem(
                        headlineContent = { Text(playlist.name) },
                        leadingContent = { Icon(AppIcons.QueueMusic, null) },
                        trailingContent = {
                            IconButton(onClick = { showOptionsMenu = true }) {
                                Icon(AppIcons.MoreVert, "Opciones")
                            }
                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Editar nombre") },
                                    onClick = {
                                        showOptionsMenu = false
                                        showEditDialog = true
                                    },
                                    leadingIcon = { Icon(AppIcons.Edit, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar") },
                                    onClick = {
                                        showOptionsMenu = false
                                        showDeleteDialog = true
                                    },
                                    leadingIcon = { Icon(AppIcons.Delete, null) }
                                )
                            }
                        },
                        modifier = Modifier.clickable { navController.navigate("playlists/${playlist.id}") }
                    )
                    
                    if (showEditDialog) {
                        var newName by remember { mutableStateOf(playlist.name) }
                        AlertDialog(
                            onDismissRequest = { showEditDialog = false },
                            title = { Text("Editar playlist") },
                            text = {
                                OutlinedTextField(
                                    value = newName,
                                    onValueChange = { newName = it },
                                    label = { Text("Nombre") },
                                    singleLine = true
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        if (newName.isNotBlank()) {
                                            viewModel.updatePlaylistName(playlist.id, newName)
                                            showEditDialog = false
                                        }
                                    }
                                ) {
                                    Text("Guardar")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEditDialog = false }) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }
                    
                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Eliminar playlist") },
                            text = { Text("¿Estás seguro de que quieres eliminar \"${playlist.name}\"?") },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.deletePlaylist(playlist.id)
                                        showDeleteDialog = false
                                    }
                                ) {
                                    Text("Eliminar")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }
                    
                    HorizontalDivider()
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text("No hay playlists personalizadas", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { showDialog = true }) {
                                Text("Crear playlist")
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nueva Playlist") },
            text = {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.createPlaylist(name)
                            showDialog = false
                        }
                    }
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
