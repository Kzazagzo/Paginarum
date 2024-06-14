package put.paginarum.ui.components.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectableRow(
    rowText: String,
    action: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = action,
                onLongClick = onLongClick,
            )
            .padding(8.dp),
    ) {
        Text(text = rowText)
    }
}
