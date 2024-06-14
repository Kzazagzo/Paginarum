package put.paginarum.ui.components.al

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ReviewRating(rating: Int) {
    Text(
        text = rating.toString(),
        color =
            if (rating > 0) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
    )
}
