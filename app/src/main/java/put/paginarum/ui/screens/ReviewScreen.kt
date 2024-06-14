package put.paginarum.ui.screens

import android.content.res.Configuration
import android.os.Parcelable
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import kotlinx.parcelize.Parcelize
import put.paginarum.domain.al.ReviewAl
import put.paginarum.ui.components.al.ReviewRating
import put.paginarum.ui.components.al.StarRating
import put.paginarum.ui.components.al.UserAvatarAlFragment
import put.paginarum.ui.components.common.ErrorFragment
import put.paginarum.ui.components.common.LoadingFragment
import put.paginarum.ui.models.TrackerScreenModel
import put.paginarum.util.DataStatus

@Parcelize
class ReviewScreen(private val alId: String) : Screen, Parcelable {
    @Composable
    override fun Content() {
        val trackerScreenModel = getScreenModel<TrackerScreenModel>()
        var loadedReviews by remember { mutableStateOf<DataStatus<List<ReviewAl>>>(DataStatus.Loading) }
        var page by remember { mutableIntStateOf(0) }
        val lazyGridState = rememberLazyGridState()

        LaunchedEffect(Unit) {
            loadedReviews = trackerScreenModel.getNovelReviews(page++, alId)
        }

        LaunchedEffect(lazyGridState) {
            snapshotFlow { lazyGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { index ->
                    if (loadedReviews is DataStatus.Success && index ==
                        (loadedReviews as DataStatus.Success<List<ReviewAl>>).data.size - 1
                    ) {
                        val newReviews = trackerScreenModel.getNovelReviews(page++, alId)

                        loadedReviews =
                            if (newReviews is DataStatus.Success) {
                                DataStatus.Success(
                                    (loadedReviews as DataStatus.Success<List<ReviewAl>>).data + newReviews.data,
                                )
                            } else {
                                loadedReviews
                            }
                    }
                }
        }

        val columns =
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                3
            } else {
                1
            }
        when (loadedReviews) {
            is DataStatus.Error -> {
                ErrorFragment(
                    (
                        loadedReviews as DataStatus
                            .Error
                    ).msg,
                )
            }

            DataStatus.Loading -> LoadingFragment()
            is DataStatus.Success -> {
                val reviews = (loadedReviews as DataStatus.Success<List<ReviewAl>>).data

                LazyVerticalGrid(
                    state = lazyGridState,
                    columns = GridCells.Fixed(columns),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    items(reviews) { review ->
                        ReviewItem(reviewAl = review)
                    }
                }
                if (reviews.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        ErrorFragment("Pozycja nie ma żadnych recenzji")
                    }
                }
            }
        }
    }

    @Composable
    fun ReviewItem(reviewAl: ReviewAl) {
        var expanded by remember { mutableStateOf(false) }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { expanded = !expanded },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                UserAvatarAlFragment(reviewAl.reviewer)
                StarRating(reviewAl.ratingByRewier.toInt())
                ReviewRating(reviewAl.reviewScore.toInt())
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = reviewAl.summary, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = reviewAl.reviweBody,
                modifier = Modifier.animateContentSize(),
                overflow = TextOverflow.Ellipsis,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
            )
        }
    }
}
