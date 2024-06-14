package put.paginarum.ui.components.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import put.paginarum.R
import put.paginarum.ui.components.providerMainScreen.OrderDirection

@Composable
fun SelectableSortButton(
    buttonText: String,
    isSelected: Boolean,
    selectedDirection: OrderDirection,
    action: (OrderDirection) -> Unit,
) {
    val alpha by animateFloatAsState(
        targetValue = if (isSelected && selectedDirection != OrderDirection.NONE) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
    )

    val rotation by animateFloatAsState(
        targetValue = if (selectedDirection == OrderDirection.ASC) 0f else 180f,
        animationSpec = tween(durationMillis = 300),
    )

    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    val newDirection =
                        when {
                            !isSelected -> OrderDirection.ASC
                            selectedDirection == OrderDirection.NONE -> OrderDirection.ASC
                            selectedDirection == OrderDirection.ASC -> OrderDirection.DESC
                            selectedDirection == OrderDirection.DESC -> OrderDirection.ASC
                            else -> OrderDirection.NONE
                        }
                    action(newDirection)
                },
            )
            .padding(8.dp),
    ) {
        Icon(
            painter =
                painterResource(
                    id = R.drawable.baseline_arrow_upward_24,
                ),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "sortingOrder",
            modifier =
                Modifier
                    .padding(end = 8.dp)
                    .graphicsLayer(rotationZ = rotation, alpha = alpha),
        )
        Text(text = buttonText)
    }
}
