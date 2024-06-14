package put.paginarum.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun <T> LazyItemPicker(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
) where T : Any {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = remember { LazyListState() }
    val selectedIndex = items.indexOf(selectedItem)

    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(selectedIndex)
            }
        }
    }

    LazyRow(
        state = lazyListState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        itemsIndexed(items) { index, item ->
            Box(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .size(100.dp, 50.dp)
                        .background(
                            if (item == selectedItem) MaterialTheme.colorScheme.primary else Color.LightGray,
                            RoundedCornerShape(8.dp),
                        )
                        .clickable {
                            onItemSelected(item)
                            coroutineScope.launch {
                                lazyListState.animateScrollToItem(index)
                            }
                        },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item.toString(),
                    textAlign = TextAlign.Center,
                    color = if (item == selectedItem) Color.White else Color.Black,
                )
            }
        }
    }
}
