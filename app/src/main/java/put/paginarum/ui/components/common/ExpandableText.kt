package put.paginarum.ui.components.common

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import put.paginarum.R

@Composable
fun ExpandableText(text: String) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    Column {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .animateContentSize()
                    .clickable { expanded = !expanded },
        ) {
            Text(
                text = text,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            shadowElevation = if (!expanded) 10f else 0f
                            alpha = if (!expanded) 0.75f else 1f
                        },
            )
        }
        IconButton(
            onClick = { expanded = !expanded },
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .graphicsLayer(rotationZ = rotation),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow_downward_24),
                contentDescription = if (expanded) "Collapse" else "Expand",
            )
        }
    }
}
