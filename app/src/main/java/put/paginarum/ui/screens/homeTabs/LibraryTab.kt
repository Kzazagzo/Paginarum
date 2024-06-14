package put.paginarum.ui.screens.homeTabs

import android.os.Parcelable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import put.paginarum.R
import put.paginarum.database.novel.NovelData
import put.paginarum.ui.components.common.InputField
import put.paginarum.ui.components.common.LoadingFragment
import put.paginarum.ui.components.library.CategoryManager
import put.paginarum.ui.models.CacheScreenModel
import put.paginarum.ui.models.CategoryScreenModel
import put.paginarum.ui.models.LibraryScreenModel
import put.paginarum.ui.models.PrivilegeScreenModel
import put.paginarum.ui.screens.PriviledgeLoginFragment
import put.paginarum.util.DataStatus

@Parcelize
object LibraryTab : Tab, Parcelable {
    private fun readResolve(): Any = HistoryTab

    override val options: TabOptions
        @Composable
        get() {
            val title = "Biblioteka"
            val icon = painterResource(id = R.drawable.outline_book_24)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon,
                )
            }
        }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val privilegeScreenModel = getScreenModel<PrivilegeScreenModel>()
        val isAuthenticated = privilegeScreenModel.isAuthenticated.collectAsState()
        val library = getScreenModel<LibraryScreenModel>()
        val novelData by library.novelsInCategories.collectAsStateWithLifecycle()

        Surface {
            val navigator = LocalNavigator.currentOrThrow.parent

            when (novelData) {
                is DataStatus.Error -> DataStatus.Error((novelData as DataStatus.Error).msg)
                is DataStatus.Loading -> LoadingFragment()
                is DataStatus.Success -> {
                    val categories = (novelData as DataStatus.Success).data
                    val selectedNovels = remember { mutableStateOf(listOf<NovelData>()) }

                    val scope = rememberCoroutineScope()
                    val librarySearch = library.searchText.collectAsStateWithLifecycle()
                    val pagerState = rememberPagerState(pageCount = { categories.size })
                    val selectedTabIndex = remember { derivedStateOf { pagerState.currentPage } }
                    Scaffold(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(
                                    WindowInsets.navigationBars.only(WindowInsetsSides.Start + WindowInsetsSides.End),
                                ),
                        topBar = {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                            ) {
                                InputField(
                                    modifier = Modifier,
                                    text = librarySearch.value,
                                ) {
                                    library.onSearchTextChange(it)
                                }
                                Spacer(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .height(16.dp),
                                )
                                TabRow(
                                    selectedTabIndex = selectedTabIndex.value,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    categories.forEachIndexed { index, categoryTab ->
                                        Tab(
                                            selected = selectedTabIndex.value == index,
                                            onClick = {
                                                scope.launch {
                                                    pagerState.animateScrollToPage(index)
                                                }
                                            },
                                            text = {
                                                Text(
                                                    categoryTab.categoryName,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        },
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(top = it.calculateTopPadding()),
                        ) { page ->
                            if (categories[page].isDeviant && !isAuthenticated.value) {
                                PriviledgeLoginFragment(privilegeScreenModel)
                            } else {
                                CategoryManager(
                                    novels = categories[page].elementsInCategory!!,
                                    screenModel = library,
                                    categoryScreenModel = getScreenModel<CategoryScreenModel>(),
                                    selectedNovels,
                                    getScreenModel<CacheScreenModel>(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
