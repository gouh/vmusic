package mx.valdora.vmusic.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mx.valdora.vmusic.ui.components.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(navController: NavController) {
    var sortOrder by remember { mutableStateOf("Alfa asc") }
    var showMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carpetas") },
                windowInsets = WindowInsets(0.dp),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(AppIcons.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    TextButton(onClick = { showMenu = true }) {
                        Text(sortOrder)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Alfa asc") }, onClick = { sortOrder = "Alfa asc"; showMenu = false })
                        DropdownMenuItem(text = { Text("Alfa desc") }, onClick = { sortOrder = "Alfa desc"; showMenu = false })
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
                Text("Lista de carpetas aquí", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
