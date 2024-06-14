package put.paginarum.ui.components.providerMainScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.internal.BackHandler
import put.paginarum.R
import put.paginarum.data.api.providerApi.ProviderApi
import put.paginarum.domain.NovelFilters
import put.paginarum.ui.components.common.InputField
import put.paginarum.util.BrowserUtils.openInBrowser

@OptIn(InternalVoyagerApi::class)
@Composable
fun ProviderTopBar(
    provider: ProviderApi,
    navigator: Navigator,
    onChange: (String) -> Unit,
    selectedFilter: (NovelFilters) -> Unit,
    filerWindowControl: (Boolean) -> Unit,
) {
    // naprawia kolory
    Surface {
        Column {
            Row(
                Modifier.padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var searchText by remember { mutableStateOf("") }
                var searchBarShown by remember { mutableStateOf(false) }

                BackHandler(enabled = searchBarShown) {
                    searchBarShown = false
                }

                val context = LocalContext.current
                IconButton(modifier = Modifier, onClick = {
                    searchBarShown = false
                    navigator.pop()
                }) {
                    Icon(
                        painterResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "back",
                    )
                }

                AnimatedVisibility(
                    visible = searchBarShown,
                    enter = expandHorizontally(animationSpec = tween(300)),
                    exit = shrinkHorizontally(animationSpec = tween(300)),
                ) {
                    InputField(
                        text = searchText,
                        onTextChange = { newText ->
                            searchText = newText
                            onChange(newText)
                        },
                        modifier = Modifier.weight(0.1f),
                    )
                }

                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier =
                        Modifier
                            .weight(0.6f)
                            .align(Alignment.CenterVertically),
                    maxLines = 1,
                )

                IconButton(modifier = Modifier.weight(0.1f), onClick = {
                    searchBarShown = !searchBarShown
                }) {
                    Icon(
                        painterResource(id = R.drawable.baseline_search_24),
                        contentDescription = "search",
                    )
                }

                IconButton(modifier = Modifier.weight(0.1f), onClick = {
                    searchBarShown = false
                    openInBrowser(provider.mainUrl, context)
                }) {
                    Icon(
                        painterResource(id = R.drawable.baseline_public_24),
                        contentDescription = "browser",
                    )
                }
            }
            Row(
                Modifier
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 0 - żaden, 1 -- pierwszy etc
                var selectedChip by remember { mutableIntStateOf(1) }

                // tam słucham tego druga godiznę
                cipiCipiCiapaCiapa(1, selectedChip, {
                    if (selectedChip == 1) {
                        selectedFilter(NovelFilters(tag = emptyMap(), orderBy = "", null))
                    } else {
                        selectedFilter(NovelFilters(tag = emptyMap(), orderBy = "Most Views", null))
                    }
                    selectedChip = it
                }, "Popular", R.drawable.rounded_favorite_24)
                cipiCipiCiapaCiapa(2, selectedChip, {
                    if (selectedChip == 2) {
                        selectedFilter(NovelFilters(tag = emptyMap(), orderBy = "", null))
                    } else {
                        selectedFilter(NovelFilters(tag = emptyMap(), orderBy = "Latest", null))
                    }
                    selectedChip = it
                }, "Latest", R.drawable.outline_new_releases_24)
                cipiCipiCiapaCiapa(
                    3,
                    selectedChip,
                    {
                        selectedChip = it
                        filerWindowControl(true)
                    },
                    "Filter",
                    R.drawable.baseline_sort_24,
                )
            }
        }
    }
}

@Composable
private fun cipiCipiCiapaCiapa(
    chipNumber: Int,
    selectedChip: Int,
    onChipSelected: (Int) -> Unit,
    label: String,
    painter: Int,
) {
    FilterChip(
        modifier = Modifier.padding(horizontal = 4.dp),
        colors =
            FilterChipDefaults.filterChipColors().copy(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        leadingIcon = {
            Icon(
                painter = painterResource(id = painter),
                contentDescription = "",
                tint =
                    if (selectedChip == chipNumber) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
            )
        },
        label = {
            Text(label)
        },
        selected = selectedChip == chipNumber,
        onClick = {
            onChipSelected(if (selectedChip == chipNumber) 0 else chipNumber)
        },
    )
}
