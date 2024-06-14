package put.paginarum.ui.components.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import put.paginarum.data.api.providerApi.SearchResponse
import put.paginarum.domain.UiConstants.columns
import put.paginarum.domain.UiConstants.itemPadding

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NovelLibraryItem(
    novel: SearchResponse,
    onClick: (SearchResponse) -> Unit,
    onLongClick: (SearchResponse) -> Unit = {},
    userAgent: String? = "",
    cookie: String? = "",
    modifier: Modifier = Modifier,
    isNovelSelected: Boolean = false
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Box(
        modifier =
            modifier
                .padding(itemPadding)
                .width((screenWidth - itemPadding * (columns + 1)) / columns)
                .aspectRatio(0.73f)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = if (isNovelSelected) 3.dp else 1.dp,
                    color = if (isNovelSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(20.dp)
                )
                .combinedClickable(
                    onClick = { onClick(novel) },
                    onLongClick = { onLongClick(novel) },
                ),
    ) {
        SubcomposeAsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .data(novel.imageUrl)
                    .crossfade(true)
                    .addHeader("User-Agent", userAgent ?: "")
                    .addHeader("Cookie", cookie ?: "")
                    .build(),
            contentDescription = novel.title,
            modifier = Modifier.fillMaxSize(),
            loading = {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            },
            success = {
                SubcomposeAsyncImageContent(
                    modifier = Modifier.fillMaxSize(),
                )
            },
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        Color.Black,
                                    ),
                            ),
                    ),
        ) {
            Text(
                text = novel.title.toString(),
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp),
                color = Color.White,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        shadow =
                            Shadow(
                                color = Color.Black,
                                offset = Offset(2f, 2f),
                                blurRadius = 4f,
                            ),
                    ),
            )
        }
    }
}
