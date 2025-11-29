package mx.valdora.vmusic.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun VerticalScrollbar(
    listState: LazyListState,
    itemCount: Int,
    modifier: Modifier = Modifier
) {
    val isScrolling = listState.isScrollInProgress
    val alpha by animateFloatAsState(
        targetValue = if (isScrolling) 1f else 0.3f,
        label = "scrollbar_alpha"
    )
    
    val scope = rememberCoroutineScope()
    
    if (itemCount > 0) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxHeight()
                .width(20.dp)
                .pointerInput(itemCount) {
                    detectTapGestures { offset ->
                        val containerHeight = size.height.toFloat()
                        val scrollProgress = (offset.y / containerHeight).coerceIn(0f, 1f)
                        val targetIndex = (scrollProgress * itemCount).toInt().coerceIn(0, itemCount - 1)
                        
                        scope.launch {
                            listState.scrollToItem(targetIndex)
                        }
                    }
                }
                .pointerInput(itemCount) {
                    detectDragGestures { change, _ ->
                        val containerHeight = size.height.toFloat()
                        val scrollProgress = (change.position.y / containerHeight).coerceIn(0f, 1f)
                        val targetIndex = (scrollProgress * itemCount).toInt().coerceIn(0, itemCount - 1)
                        
                        scope.launch {
                            listState.scrollToItem(targetIndex)
                        }
                    }
                },
            contentAlignment = Alignment.TopEnd
        ) {
            val firstVisibleIndex = listState.firstVisibleItemIndex.toFloat()
            val totalItemsCount = itemCount.toFloat()
            
            val visibleItems = listState.layoutInfo.visibleItemsInfo.size.toFloat()
            val scrollbarHeightFraction = (visibleItems / totalItemsCount).coerceIn(0.1f, 1f)
            
            val scrollProgress = if (totalItemsCount > 0) {
                (firstVisibleIndex / totalItemsCount).coerceIn(0f, 1f - scrollbarHeightFraction)
            } else 0f
            
            val containerHeight = maxHeight
            val scrollbarOffset = containerHeight * scrollProgress
            
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight(scrollbarHeightFraction)
                    .offset(y = scrollbarOffset)
                    .alpha(alpha)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}
