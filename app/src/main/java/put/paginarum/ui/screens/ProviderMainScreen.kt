package put.paginarum.ui.screens

import android.os.Parcelable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import put.paginarum.data.api.providerApi.SearchResponse
import put.paginarum.data.repository.network.CloudFlareException
import put.paginarum.domain.NovelFilters
import put.paginarum.domain.UiConstants.columns
import put.paginarum.domain.UiConstants.itemPadding
import put.paginarum.domain.UiConstants.rows
import put.paginarum.ui.components.common.ErrorFragment
import put.paginarum.ui.components.common.LoadingFragment
import put.paginarum.ui.components.library.NovelLibraryItem
import put.paginarum.ui.components.providerMainScreen.ProviderBottomSheet
import put.paginarum.ui.components.providerMainScreen.ProviderTopBar
import put.paginarum.ui.components.providerMainScreen.ResolveCloudFlareFragment
import put.paginarum.ui.models.CacheScreenModel
import put.paginarum.ui.models.LibraryScreenModel
import put.paginarum.util.DataStatus

@Parcelize
class ProviderMainScreen(private val providerName: String) : Parcelable, Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        var novels by remember {
            mutableStateOf<DataStatus<Set<SearchResponse>>>(
                DataStatus.Loading,
            )
        }
        var searchText by remember { mutableStateOf("") }
        var selectedFilters by remember { mutableStateOf(NovelFilters(emptyMap(), "Most Views", null)) }
        var page by remember { mutableIntStateOf(0) }
        val listState = rememberLazyGridState()
        val navigator = LocalNavigator.current
        val coroutineScope = rememberCoroutineScope()

        val provider = getScreenModel<LibraryScreenModel>().providers[providerName]!!
        val cacheScreenModel = getScreenModel<CacheScreenModel>()
        val cookies by cacheScreenModel.cookies.collectAsState()

        LaunchedEffect(selectedFilters, searchText, cookies) {
            page = 0
            novels = DataStatus.Success(emptySet())
            novels = DataStatus.Loading
            coroutineScope.launch {
                listState.scrollToItem(0)
            }
            coroutineScope.launch {
                try {
                    val newNovels =
                        if (searchText.isNotEmpty()) {
                            provider.search(searchText, page++, selectedFilters) +
                                provider.search(
                                    searchText,
                                    page++,
                                    selectedFilters,
                                )
                        } else {
                            provider.loadMainPage(page++, selectedFilters) +
                                provider.loadMainPage(
                                    page++,
                                    selectedFilters,
                                )
                        }
                    novels = DataStatus.Success(newNovels)
                } catch (e: Exception) {
                    if (e::class.java == CloudFlareException::class.java) {
                        novels = DataStatus.Error(e.toString())
                    }
                }
            }
        }

        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                .map { visibleItemsInfo ->
                    visibleItemsInfo.lastOrNull()?.index ?: 0
                }
                .distinctUntilChanged()
                .collect { lastVisibleItemIndex ->
                    if (novels !is DataStatus.Error) {
                        val currentNovels: MutableSet<SearchResponse> =
                            if (novels is DataStatus.Success) {
                                (novels as? DataStatus.Success)?.data?.toMutableSet()
                                    ?: mutableSetOf()
                            } else {
                                mutableSetOf()
                            }
                        if (lastVisibleItemIndex >= currentNovels.size - columns * rows * 2) {
                            coroutineScope.launch {
                                try {
                                    page++
                                    val moreNovels =
                                        if (searchText.isNotEmpty()) {
                                            provider.search(searchText, page, selectedFilters)
                                        } else {
                                            provider.loadMainPage(page, selectedFilters)
                                        }
                                    currentNovels.addAll(moreNovels)
                                    novels = DataStatus.Success(currentNovels)
                                } catch (e: Exception) {
                                    page--
                                    // novels = DataStatus.Error(e.toString())
                                }
                            }
                        }
                    }
                }
        }

        val scaffoldState =
            rememberBottomSheetScaffoldState(
                SheetState(
                    false,
                    density = Density(LocalContext.current),
                ),
            )

        BottomSheetScaffold(
            modifier = Modifier,
            scaffoldState = scaffoldState,
            topBar = {
                ProviderTopBar(
                    provider,
                    navigator!!,
                    { searchText = it },
                    { selectedFilters = it },
                ) {
                    coroutineScope.launch {
                        if (it) {
                            scaffoldState.bottomSheetState.expand()
                        } else {
                            scaffoldState.bottomSheetState.hide()
                        }
                    }
                }
            },
            sheetPeekHeight = 0.dp,
            sheetContent = {
                ProviderBottomSheet(provider) {
                    selectedFilters = it
                }
            },
            sheetShape = RoundedCornerShape(10.dp),
            sheetDragHandle = {},
        ) {
            when (novels) {
                is DataStatus.Error -> {
                    val errorNovels = (novels as DataStatus.Error).msg
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ErrorFragment(errorNovels)
                        ResolveCloudFlareFragment {
                            cacheScreenModel.resolveCloudFlare(provider)
                        }
                    }
                }
                DataStatus.Loading -> {
                    LoadingFragment()
                }
                is DataStatus.Success -> {
                    val loadedNovels = (novels as DataStatus.Success).data
                    val userAgent = cacheScreenModel.userAgent.collectAsState()
                    LazyVerticalGrid(
                        GridCells.Fixed(columns),
                        state = listState,
                        contentPadding = PaddingValues(itemPadding),
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(it),
                    ) {
                        items(loadedNovels.toList()) { novel ->
                            NovelLibraryItem(
                                novel = novel,
                                {
                                    navigator!!.push(
                                        NovelScreen(
                                            providerName,
                                            it.novelUrl,
                                            it.title.toString(),
                                        ),
                                    )
                                },
                                {},
                                userAgent = userAgent.value,
                                cookies[provider.mainUrl + '/'],
                            )
                        }
                    }
                }
            }
        }
    }
}
