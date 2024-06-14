package put.paginarum.ui.screens.homeTabs

import android.content.res.Configuration
import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import coil.compose.AsyncImage
import kotlinx.parcelize.Parcelize
import put.paginarum.R
import put.paginarum.ui.models.LibraryScreenModel
import put.paginarum.ui.screens.ProviderMainScreen

@Parcelize
object ExploreTab : Tab, Parcelable {
    private fun readResolve(): Any = ExploreTab

    override val options: TabOptions
        @Composable
        get() {
            val title = "Przeglądaj"
            val icon = painterResource(id = R.drawable.outline_explore_24)

            return remember {
                TabOptions(
                    index = 1u,
                    title = title,
                    icon = icon,
                )
            }
        }

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current?.parent
        val libraryScreenModel = getScreenModel<LibraryScreenModel>()

        val providersList = libraryScreenModel.providers

        val configuration = LocalConfiguration.current
        val columns =
            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                2
            } else {
                1
            }
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            Modifier.fillMaxSize()
                .padding(12.dp),
        ) {
            items(providersList.values.toList()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            navigator!!.push(ProviderMainScreen(it.name))
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    AsyncImage(
                        model = it.iconUrl,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                    )
                    Text(text = it.name)
                }
            }
        }
    }
}
