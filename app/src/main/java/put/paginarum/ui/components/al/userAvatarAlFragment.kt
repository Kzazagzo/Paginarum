package put.paginarum.ui.components.al

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import put.paginarum.domain.al.UserAl

@Composable
fun UserAvatarAlFragment(user: UserAl) {
    AsyncImage(
        model =
            ImageRequest.Builder(LocalContext.current)
                .data(user.avatar.large)
                .crossfade(true)
                .build(),
        contentDescription = "avatar",
        modifier =
            Modifier
                .size(50.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
        contentScale = ContentScale.Crop,
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
        text = user.name,
        style = MaterialTheme.typography.headlineMedium,
        modifier =
            Modifier.background(
                Color.Black.copy(alpha = 0.7f),
                shape =
                    RoundedCornerShape(4.dp),
            ).padding(8.dp),
    )
}
