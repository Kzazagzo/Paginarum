package put.paginarum.ui.components.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import put.paginarum.domain.al.UserAl
import put.paginarum.ui.components.al.UserAvatarAlFragment

@Composable
fun TrackerUserRow(user: UserAl) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(200.dp),
    ) {
        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .data(user.bannerImage)
                    .crossfade(true)
                    .build(),
            contentDescription = "banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Row(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatarAlFragment(user)
        }
    }
}
