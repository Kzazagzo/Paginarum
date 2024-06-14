package put.paginarum.ui.screens

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.ui.theme.provider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.jsoup.internal.StringUtil.padding
import put.paginarum.R
import put.paginarum.data.api.providerApi.AlSearchResponseImpl
import put.paginarum.data.repository.network.AniListRepository
import put.paginarum.database.al.AlTrackerEntity
import put.paginarum.database.novel.ChapterTextEntity
import put.paginarum.database.novel.NovelData
import put.paginarum.domain.SettingCategory
import put.paginarum.ui.components.common.ErrorFragment
import put.paginarum.ui.components.common.ExpandableText
import put.paginarum.ui.components.common.IconWithLabel
import put.paginarum.ui.components.common.LazyItemPicker
import put.paginarum.ui.components.common.LoadingFragment
import put.paginarum.ui.components.library.NovelLibraryItem
import put.paginarum.ui.components.novelScreen.DownloadButton
import put.paginarum.ui.components.providerMainScreen.ResolveCloudFlareFragment
import put.paginarum.ui.models.CacheScreenModel
import put.paginarum.ui.models.LibraryScreenModel
import put.paginarum.ui.models.TrackerScreenModel
import put.paginarum.ui.screens.settingsScreen.SettingScreenImpl
import put.paginarum.util.BrowserUtils.openInBrowser
import put.paginarum.util.DataStatus

@Parcelize
class NovelScreen(
    private val providerName: String,
    private val novelUrl: String,
    private val novelName: String,
) : Parcelable, Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        val library = getScreenModel<LibraryScreenModel>()
        val tracker = getScreenModel<TrackerScreenModel>()
        tracker.fetchCurrentUserData()
        val provider = library.providers[providerName]!!

        val alLoged by tracker.alTrackingStatus.collectAsState()
        var trackingStatus by remember { mutableStateOf<DataStatus<AlTrackerEntity?>>(DataStatus.Loading) }

        var novelDataStatus by remember {
            mutableStateOf<DataStatus<NovelData>>(
                DataStatus
                    .Loading,
            )
        }

        LaunchedEffect(Unit) {
            novelDataStatus =
                try {
                    DataStatus.Success(provider.loadNovelData(novelUrl))
                } catch (e: Exception) {
                    DataStatus.Error(e.message)
                }
        }

        val navigator = LocalNavigator.current

        var chapterTextsStatus by remember {
            mutableStateOf<DataStatus<List<ChapterTextEntity>>>(
                DataStatus
                    .Loading,
            )
        }
        var inLibrary by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(1000L)
            inLibrary = library.novelInLibrary(novelUrl)
        }
        val scaffoldState =
            rememberBottomSheetScaffoldState(
                SheetState(
                    false,
                    density = Density(LocalContext.current),
                    initialValue = SheetValue.Hidden,
                ),
            )

        LaunchedEffect(Unit) {
            chapterTextsStatus =
                try {
                    DataStatus.Success(library.chapterTextGetForNovel(novelUrl))
                } catch (e: Exception) {
                    DataStatus.Error(e.message)
                }
            trackingStatus = DataStatus.Success(tracker.getNovelTrackingStatus(novelName))
        }

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        WindowInsets.systemBars.asPaddingValues(),
                    ),
            sheetContent = {
                val coroutine = rememberCoroutineScope()
                if (alLoged) {
                    when (trackingStatus) {
                        is DataStatus.Error -> ErrorFragment()
                        DataStatus.Loading -> LoadingFragment()
                        is DataStatus.Success -> {
                            val loaded = (trackingStatus as DataStatus.Success<AlTrackerEntity?>).data
                            TrackerStatusTooltip(loaded, {
                                trackingStatus = DataStatus.Success(loaded)
                                scope.launch {
                                    tracker.insertAlTracker(it)
                                }
                            }) {
                                coroutine.launch {
                                    novelDataStatus =
                                        try {
                                            DataStatus.Success(provider.loadNovelData(novelUrl))
                                        } catch (e: Exception) {
                                            DataStatus.Error(e.message)
                                        }
                                    trackingStatus = DataStatus.Success(tracker.getNovelTrackingStatus(novelName))
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ErrorFragment(
                            "Nie jesteś zalogowany do usługi AniList \n" +
                                "Kliknij poniższy przycisk aby przejść do ekranu logowania",
                        )
                        Button(
                            modifier = Modifier.padding(bottom = 32.dp),
                            onClick = { navigator!!.push(SettingScreenImpl(SettingCategory.Tracking)) },
                        ) {
                            Text(text = "Zaloguj ")
                        }
                    }
                }
            },
            sheetPeekHeight = 0.dp,
            sheetShape = RoundedCornerShape(10.dp),
            sheetDragHandle = {},
        ) {
            val cacheScreenModel = getScreenModel<CacheScreenModel>()
            when (novelDataStatus) {
                is DataStatus.Error -> {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ErrorFragment((novelDataStatus as DataStatus.Error).msg)
                            ResolveCloudFlareFragment {
                                cacheScreenModel.resolveCloudFlare(provider)
                            }
                        }
                    }
                }
                DataStatus.Loading -> LoadingFragment()
                is DataStatus.Success -> {
                    val novelData = (novelDataStatus as DataStatus.Success<NovelData>).data
                    val trackingData = (trackingStatus as DataStatus.Success<AlTrackerEntity?>).data
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(it),
                    ) {
                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                            ) {
                                val cacheScreenModel = getScreenModel<CacheScreenModel>()
                                val userAgent = cacheScreenModel.userAgent.collectAsState()
                                val cookie = cacheScreenModel.cookies.collectAsState(emptyMap())
                                SubcomposeAsyncImage(
                                    model =
                                        ImageRequest.Builder(LocalContext.current)
                                            .data(novelData.imageUrl)
                                            .crossfade(true)
                                            .addHeader("User-Agent", userAgent.value ?: "")
                                            .addHeader("Cookie", cookie.value["https://novelsonline.net/"] ?: "")
                                            .build(),
                                    contentDescription = "",
                                    modifier = Modifier.weight(0.3f),
                                    loading = {
                                        CircularProgressIndicator(
                                            modifier = Modifier.align(Alignment.Center),
                                        )
                                    },
                                    success = {
                                        SubcomposeAsyncImageContent(
                                            modifier = Modifier.height(200.dp),
                                        )
                                    },
                                    contentScale = ContentScale.Crop,
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(Modifier.weight(0.5f)) {
                                    Text(
                                        text = novelData.title ?: "Unknown title",
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = novelData.author ?: "Unknown author",
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                    Text(
                                        text = "${novelData.status}",
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                IconWithLabel(
                                    painter =
                                        painterResource(
                                            if (inLibrary) {
                                                R.drawable.baseline_book_24
                                            } else {
                                                R.drawable.outline_book_24
                                            },
                                        ),
                                    label =
                                        if (inLibrary) {
                                            "Remove from library"
                                        } else {
                                            "Add to library"
                                        },
                                    onClick = {
                                        if (inLibrary) {
                                            library.deleteNovelFromLibrary(novelData.novelUrl)
                                        } else {
                                            library.addNovelToLibrary(novelData)
                                        }
                                        inLibrary = inLibrary.not()
                                    },
                                    modifier = Modifier.weight(1f),
                                    activated = inLibrary,
                                )
                                IconWithLabel(
                                    painter =
                                        painterResource(
                                            if (trackingData != null) {
                                                R.drawable.baseline_cloud_sync_24
                                            } else {
                                                R.drawable.outline_cloud_sync_24
                                            },
                                        ),
                                    label =
                                        if (trackingData != null) {
                                            "Remove tracker"
                                        } else {
                                            "Add tracker"
                                        },
                                    onClick = {
                                        scope.launch {
                                            scaffoldState.bottomSheetState.expand()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    activated = trackingData != null,
                                )
                                if (trackingData != null) {
                                    IconWithLabel(
                                        painter = painterResource(id = R.drawable.outline_polyline_24),
                                        label = "Reviews",
                                        onClick = {
                                            navigator!!.push(
                                                ReviewScreen(
                                                    trackingData.alId,
                                                ),
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        activated = false,
                                    )
                                }
                                val context = LocalContext.current
                                IconWithLabel(
                                    painter = painterResource(id = R.drawable.baseline_public_24),
                                    label = "Open in browser",
                                    onClick = { openInBrowser(novelData.novelUrl, context) },
                                    modifier = Modifier.weight(1f),
                                    activated = false,
                                )
                            }
                        }

                        item {
                            ExpandableText(novelData.description ?: "No description available")
                        }

                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${novelData.chapters.size} chapters",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_sort_24),
                                        contentDescription = "sort",
                                    )
                                }
                            }
                        }

                        itemsIndexed(novelData.chapters) { index, chapter ->

                            Row(
                                Modifier
                                    .padding(6.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        navigator!!.push(
                                            ReaderScreen(
                                                providerName,
                                                novelData
                                                    .chapters,
                                                index,
                                            ),
                                        )
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    Modifier
                                        .weight(0.9f)
                                        .padding(12.dp),
                                ) {
                                    Text(
                                        text = chapter.name,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                    Text(
                                        text = chapter.releaseDate ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                                DownloadButton(
                                    provider,
                                    library,
                                    inLibrary,
                                    chapterTextsStatus,
                                    novelData.novelUrl,
                                    chapter.chapterUrl,
                                    Modifier.weight(0.1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TrackerStatusTooltip(
        alTrackerEntity: AlTrackerEntity?,
        onChange: (AlTrackerEntity) -> Unit,
        restart: () -> Unit,
    ) {
        var selectedStatus by remember { mutableStateOf(alTrackerEntity?.selectedTracking) }
        val trackerScreenModel = getScreenModel<TrackerScreenModel>()
        val coroutine = rememberCoroutineScope()

        var trackerWindowShown by remember { mutableStateOf(false) }
        if (trackerWindowShown) {
            TrackerScaffold({
                it.let {
                    trackerScreenModel.addTrackingToNovel(
                        it.id,
                        AniListRepository.AlNovelStatus.CURRENT,
                    )
                    coroutine.launch {
                        trackerScreenModel.insertAlTracker(
                            AlTrackerEntity(
                                it.id,
                                it.title,
                                novelName,
                                AniListRepository.AlNovelStatus.CURRENT,
                            ),
                        )
                    }
                    restart()
                }
            }, { trackerWindowShown = false })
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
        ) {
            if (alTrackerEntity != null) {
                Text(
                    text = "Current Status: ${selectedStatus?.name}",
                    modifier =
                        Modifier
                            .padding(8.dp),
                )
                LazyItemPicker(
                    items = AniListRepository.AlNovelStatus.entries.toList(),
                    selectedItem = selectedStatus,
                    onItemSelected = { status ->
                        selectedStatus = status
                        alTrackerEntity.selectedTracking = status
                        onChange(alTrackerEntity)
                    },
                )
            } else {
                ErrorFragment(
                    "Tracker nie został dodany - dodaj go.",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Button(
                onClick = {
                    trackerWindowShown = true
                },
                modifier =
                    Modifier
                        .align(Alignment.End)
                        .padding(6.dp),
            ) {
                Text("Edytuj status")
            }
        }
    }

    @Composable
    fun TrackerScaffold(
        onClick: (AlSearchResponseImpl) -> Unit,
        onDismiss: () -> Unit,
    ) {
        val trackerScreenModel = getScreenModel<TrackerScreenModel>()
        val coroutine = rememberCoroutineScope()
        var textField by remember { mutableStateOf(novelName) }
        var selectedNovel by remember { mutableStateOf<AlSearchResponseImpl?>(null) }
        var page by remember { mutableIntStateOf(0) }
        val listState = rememberLazyGridState()
        var fetchedNovels by remember { mutableStateOf<List<AlSearchResponseImpl>>(emptyList()) }

        val fetchNovels = {
            coroutine.launch {
                page = 0
                fetchedNovels = trackerScreenModel.fetchNovelData(textField, page++)
            }
        }
        LaunchedEffect(Unit) {
            fetchedNovels = trackerScreenModel.fetchNovelData(textField, page++)
        }

        LaunchedEffect(listState) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { index ->
                    if (index == fetchedNovels.size - 1) {
                        page++
                        val newNovels = trackerScreenModel.fetchNovelData(novelName, page)
                        fetchedNovels = fetchedNovels + newNovels
                    }
                }
        }

        Dialog(onDismissRequest = { onDismiss() }) {
            val keyboardController = LocalSoftwareKeyboardController.current
            Scaffold(
                topBar = {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                    ) {
                        TextField(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .onKeyEvent { keyEvent ->
                                        if (keyEvent.type == KeyEventType.KeyUp &&
                                            (keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter)
                                        ) {
                                            fetchNovels()
                                            keyboardController?.hide()
                                            true
                                        } else {
                                            false
                                        }
                                    },
                            textStyle = MaterialTheme.typography.bodySmall,
                            value = textField,
                            onValueChange = { textField = it },
                            colors =
                                TextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.secondary,
                                    unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                                    focusedContainerColor = MaterialTheme.colorScheme.background,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.background,
                                    focusedPlaceholderColor = MaterialTheme.colorScheme.background,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                            keyboardOptions =
                                KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done,
                                    keyboardType = KeyboardType.Text,
                                ),
                            keyboardActions =
                                KeyboardActions(
                                    onDone = {
                                        fetchNovels()
                                        keyboardController?.hide()
                                    },
                                ),
                        )
                        HorizontalDivider(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(2.dp),
                        )
                    }
                },
                bottomBar = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        val coroutineScope = rememberCoroutineScope()
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (selectedNovel != null) {
                                onClick(selectedNovel!!)
                            }
                            onDismiss()
                        }) {
                            Text("Confirm")
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = listState,
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(it),
                ) {
                    items(fetchedNovels) { novel ->
                        NovelLibraryItem(
                            novel = novel,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        if (novel == selectedNovel) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                    ),
                            onClick = { selectedNovel = novel },
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (fetchedNovels.isEmpty()) {
                        ErrorFragment(
                            "Nie znaleziono pozycji o podanej nazwie w usłudze \n" +
                                "Upewnij się że jest to podstawowa nazwa, bez żadnych zbędnych trekstów",
                            Modifier.fillMaxWidth().scale(0.8f),
                        )
                    }
                }
            }
        }
    }
}
