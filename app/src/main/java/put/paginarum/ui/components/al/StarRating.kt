package put.paginarum.ui.components.al

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import put.paginarum.R

@Composable
fun StarRating(
    rating: Int,
    maxRating: Int = 10,
) {
    val filledStars = rating / 2
    val unfilledStars = (maxRating / 2) - filledStars

    Row {
        repeat(filledStars) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_star_24),
                contentDescription = "Filled Star",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        repeat(unfilledStars) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_star_border_24),
                contentDescription = "Unfilled Star",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
