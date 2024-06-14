package put.paginarum.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun SelectableRowWithIcon(
    rowText: String,
    painterResource: Painter,
    action: (String) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    action(rowText)
                },
            )
            .padding(8.dp),
    ) {
        Icon(
            painter =
            painterResource,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "icon",
            modifier =
                Modifier
                    .padding(end = 8.dp),
        )
        Text(text = rowText)
    }
}
