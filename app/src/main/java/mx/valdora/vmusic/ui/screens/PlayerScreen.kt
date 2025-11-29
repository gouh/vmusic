package mx.valdora.vmusic.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as AnimationRepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import mx.valdora.vmusic.ui.viewmodel.PlayerViewModel
import mx.valdora.vmusic.player.RepeatMode
import mx.valdora.vmusic.player.ShuffleMode
import mx.valdora.vmusic.ui.components.*
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(onDismiss: () -> Unit, viewModel: PlayerViewModel) {
    android.util.Log.d("PlayerScreen", "PlayerScreen composing")
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val shuffleMode by viewModel.shuffleMode.collectAsState()
    
    var isFavorite by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(currentSong) {
        android.util.Log.d("PlayerScreen", "Song: ${currentSong?.title}, Artwork: ${currentSong?.artwork != null}")
    }
    
    LaunchedEffect(currentSong?.id) {
        currentSong?.id?.let {
            isFavorite = viewModel.isFavorite(it)
        }
    }
    
    val progress = if (duration > 0) {
        (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    
    fun closePlayer() {
        scope.launch {
            offsetY.animateTo(2000f, animationSpec = tween(150))
        }.invokeOnCompletion {
            onDismiss()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = offsetY.value
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val (x, y) = dragAmount
                            
                            if (abs(y) > abs(x) && y > 0) {
                                scope.launch {
                                    offsetY.snapTo((offsetY.value + y).coerceAtLeast(0f))
                                }
                            }
                        },
                        onDragEnd = {
                            if (offsetY.value > 200) {
                                closePlayer()
                            } else {
                                scope.launch {
                                    offsetY.animateTo(0f, animationSpec = tween(150))
                                }
                            }
                        }
                    )
                }
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Card(
                modifier = Modifier
                    .size(300.dp)
                    .pointerInput(Unit) {
                        var totalDragX = 0f
                        var totalDragY = 0f
                        
                        detectDragGestures(
                            onDragStart = {
                                totalDragX = 0f
                                totalDragY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val (x, y) = dragAmount
                                totalDragX += x
                                totalDragY += y
                                
                                if (abs(totalDragY) > abs(totalDragX) && y > 0) {
                                    scope.launch {
                                        offsetY.snapTo((offsetY.value + y).coerceAtLeast(0f))
                                    }
                                }
                            },
                            onDragEnd = {
                                if (abs(totalDragX) > abs(totalDragY) && abs(totalDragX) > 150) {
                                    if (totalDragX > 0) {
                                        viewModel.skipPrevious()
                                    } else {
                                        viewModel.skipNext()
                                    }
                                } else if (offsetY.value > 200) {
                                    closePlayer()
                                } else {
                                    scope.launch {
                                        offsetY.animateTo(0f, animationSpec = tween(150))
                                    }
                                }
                            }
                        )
                    }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    currentSong?.artwork?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Album Art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: Icon(
                        AppIcons.Album,
                        contentDescription = "Sin portada",
                        modifier = Modifier.size(120.dp)
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                MarqueeText(
                    text = currentSong?.title ?: "Sin canción",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentSong?.artist ?: "Artista desconocido",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
            }
            
            Column {
                Box(modifier = Modifier.fillMaxWidth()) {
                    WaveSlider(
                        value = progress,
                        onValueChange = { 
                            viewModel.seekTo((it * duration).toLong())
                        }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatTime(currentPosition))
                    Text(formatTime(duration))
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.rewind10() }) {
                    Icon(AppIcons.Replay10, "Retroceder 10s")
                }
                IconButton(onClick = { viewModel.skipPrevious() }) {
                    Icon(AppIcons.SkipPrevious, "Anterior")
                }
                IconButton(onClick = { 
                    if (isPlaying) viewModel.pause() else viewModel.resume()
                }) {
                    Icon(
                        if (isPlaying) AppIcons.Pause else AppIcons.PlayArrow,
                        "Play/Pause",
                        modifier = Modifier.size(48.dp)
                    )
                }
                IconButton(onClick = { viewModel.skipNext() }) {
                    Icon(AppIcons.SkipNext, "Siguiente")
                }
                IconButton(onClick = { viewModel.forward10() }) {
                    Icon(AppIcons.Forward10, "Adelantar 10s")
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        AppIcons.Shuffle,
                        "Shuffle",
                        tint = if (shuffleMode == ShuffleMode.ON) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { viewModel.toggleRepeat() }) {
                    Icon(
                        AppIcons.Repeat,
                        "Repeat",
                        tint = if (repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { 
                    currentSong?.let { showPlaylistDialog = true }
                }) {
                    Icon(AppIcons.PlaylistPlay, "Agregar a playlist")
                }
                IconButton(onClick = { 
                    currentSong?.id?.let {
                        viewModel.toggleFavorite(it)
                        isFavorite = !isFavorite
                    }
                }) {
                    Icon(
                        if (isFavorite) AppIcons.Favorite else AppIcons.FavoriteBorder,
                        "Favorito",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        IconButton(
            onClick = { closePlayer() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 24.dp)
        ) {
            Icon(AppIcons.ExpandMore, "Cerrar")
        }
    }
    }
    
    if (showPlaylistDialog && currentSong != null) {
        AddToPlaylistDialog(
            song = currentSong!!,
            onDismiss = { showPlaylistDialog = false }
        )
    }
}

private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%d:%02d", minutes, seconds)
}

@Composable
fun WaveSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant
    var dragValue by remember { mutableStateOf(value) }
    
    LaunchedEffect(value) {
        if (!dragValue.isNaN()) {
            dragValue = value
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragValue = (offset.x / size.width).coerceIn(0f, 1f)
                        onValueChange(dragValue)
                    },
                    onDragEnd = {
                        dragValue = Float.NaN
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        val newValue = ((dragValue * size.width + dragAmount) / size.width).coerceIn(0f, 1f)
                        dragValue = newValue
                        onValueChange(newValue)
                    }
                )
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            val width = size.width
            val height = size.height
            val centerY = height / 2
            val amplitude = 15f
            val frequency = 4f
            val progressX = width * value
            
            // Calcular Y del thumb basado en la onda
            val thumbProgress = value * width
            val thumbY = centerY + amplitude * sin((thumbProgress / width) * frequency * 2 * PI).toFloat()
            
            // Dibujar path inactivo (completo)
            val inactivePath = Path().apply {
                moveTo(0f, centerY)
                for (x in 0..width.toInt() step 2) {
                    val y = centerY + amplitude * sin((x / width) * frequency * 2 * PI).toFloat()
                    lineTo(x.toFloat(), y)
                }
            }
            
            drawPath(
                path = inactivePath,
                color = inactiveColor,
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )
            
            // Dibujar path activo (hasta el progreso)
            if (progressX > 0) {
                val activePath = Path().apply {
                    moveTo(0f, centerY)
                    for (x in 0..progressX.toInt() step 2) {
                        val y = centerY + amplitude * sin((x / width) * frequency * 2 * PI).toFloat()
                        lineTo(x.toFloat(), y)
                    }
                }
                
                drawPath(
                    path = activePath,
                    color = primaryColor,
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )
            }
            
            // Dibujar thumb en la posición de la onda
            drawCircle(
                color = primaryColor,
                radius = 16f,
                center = Offset(progressX, thumbY)
            )
        }
    }
}

@Composable
fun MarqueeText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val offsetX = remember { Animatable(0f) }
    
    LaunchedEffect(text, textLayoutResult) {
        textLayoutResult?.let { layout ->
            if (layout.didOverflowWidth) {
                val textWidth = layout.size.width.toFloat()
                offsetX.snapTo(0f)
                kotlinx.coroutines.delay(1000)
                offsetX.animateTo(
                    targetValue = -textWidth - 100f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = ((textWidth / 50) * 1000).toInt(),
                            easing = LinearEasing
                        ),
                        repeatMode = AnimationRepeatMode.Restart
                    )
                )
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = style,
            maxLines = 1,
            softWrap = false,
            onTextLayout = { textLayoutResult = it },
            modifier = Modifier.graphicsLayer {
                translationX = offsetX.value
            }
        )
    }
}
