package mx.valdora.vmusic.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun SortDropdownMenu(
    sortOrder: String,
    onSortOrderChange: (String) -> Unit,
    showDateOptions: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    IconButton(
        onClick = { showMenu = true },
        modifier = modifier
    ) {
        Icon(AppIcons.Sort, contentDescription = "Ordenar")
    }
    
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("Alfabético") },
            onClick = {
                val newOrder = if (sortOrder == "Alfa asc") "Alfa desc" else "Alfa asc"
                onSortOrderChange(newOrder)
                showMenu = false
            },
            leadingIcon = { Icon(AppIcons.SortByAlpha, null) },
            trailingIcon = { 
                Icon(
                    if (sortOrder == "Alfa asc") AppIcons.North else AppIcons.South, 
                    null
                ) 
            }
        )
        
        if (showDateOptions) {
            DropdownMenuItem(
                text = { Text("Fecha") },
                onClick = {
                    val newOrder = if (sortOrder == "Fecha desc") "Fecha asc" else "Fecha desc"
                    onSortOrderChange(newOrder)
                    showMenu = false
                },
                leadingIcon = { Icon(AppIcons.Schedule, null) },
                trailingIcon = { 
                    Icon(
                        if (sortOrder == "Fecha desc") AppIcons.North else AppIcons.South, 
                        null
                    ) 
                }
            )
        }
    }
}
