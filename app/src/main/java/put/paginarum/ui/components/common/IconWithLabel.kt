package put.paginarum.ui.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun IconWithLabel(
    modifier: Modifier = Modifier,
    painter: Painter,
    label: String,
    onClick: () -> Unit,
    activated: Boolean,
) {
    IconButton(onClick = onClick, modifier = modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painter,
                contentDescription = "",
                tint =
                    if (activated) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        LocalContentColor.current
                    },
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                modifier =
                    Modifier
                        .width(60.dp)
                        .wrapContentHeight(),
                textAlign = TextAlign.Center,
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color =
                    if (activated) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Unspecified
                    },
            )
        }
    }
}
