package put.paginarum.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import put.paginarum.R

@Composable
fun BackButton(
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier =
            modifier.padding(16.dp).clickable {
                navigator.pop()
            },
        painter = painterResource(id = R.drawable.baseline_arrow_back_24),
        contentDescription =
            "back",
    )
}
