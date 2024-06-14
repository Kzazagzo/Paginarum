package put.paginarum.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import put.paginarum.R

@Composable
fun TagedTriStateCheckBox(
    buttonText: String,
    selectedState: SelectionState,
    action: (SelectionState) -> Unit,
) {
    val onClick = {
        val newDirection =
            when (selectedState) {
                SelectionState.UNSELECTED -> SelectionState.SELECTED
                SelectionState.SELECTED -> SelectionState.SKIPPED
                SelectionState.SKIPPED -> SelectionState.UNSELECTED
            }
        action(newDirection)
    }

    val iconId =
        when (selectedState) {
            SelectionState.UNSELECTED -> R.drawable.baseline_empty_24
            SelectionState.SELECTED -> R.drawable.baseline_check_24
            SelectionState.SKIPPED -> R.drawable.baseline_close_24
        }

    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(24.dp)
                    .border(2.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.small)
                    .background(Color.Transparent),
        ) {
            Icon(
                modifier =
                    Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            if (selectedState == SelectionState.UNSELECTED) {
                                Color.Transparent
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        )
                        .padding(4.dp),
                painter = painterResource(id = iconId),
                tint = MaterialTheme.colorScheme.surface,
                contentDescription = "selectedTag",
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = buttonText)
    }
}

enum class SelectionState {
    UNSELECTED,
    SELECTED,
    SKIPPED,
}
