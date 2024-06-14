package put.paginarum.ui.screens

import android.os.Parcelable
import androidx.browser.trusted.ScreenOrientation.PORTRAIT_SECONDARY
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mutualmobile.composesensors.SensorDelay
import com.mutualmobile.composesensors.rememberGravitySensorState
import com.mutualmobile.composesensors.rememberHingeAngleSensorState
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import put.paginarum.database.novel.ChapterData
import put.paginarum.domain.ChapterElement
import put.paginarum.domain.ChapterText
import put.paginarum.domain.SeparatorType
import put.paginarum.ui.components.common.ErrorFragment
import put.paginarum.ui.models.LibraryScreenModel
import put.paginarum.util.DataStatus
import put.paginarum.util.px

@Parcelize
class ReaderScreen(
    private val providerName: String,
    private val chapterList: List<ChapterData>,
    private val chapterIndex: Int,
) :
    Parcelable, Screen {
    @Composable
    fun Sensors(
        textScrew: Animatable<Float, AnimationVector1D>,
        spacerBetween: MutableState<Boolean>,
    ) {
        val gravitySensor = rememberGravitySensorState(sensorDelay = SensorDelay.Game)
        val gravity = arrayOf(gravitySensor.xForce, gravitySensor.yForce, gravitySensor.zForce)

        if (gravity[2] > 8) {
            LaunchedEffect(Unit) {
                textScrew.animateTo(-15f, tween(3000))
            }
        } else {
            LaunchedEffect(Unit) {
                textScrew.animateTo(0f, tween(3000))
            }
        }
        val hingeSensor = rememberHingeAngleSensorState()
        spacerBetween.value = hingeSensor.angle in 70f..120f && LocalConfiguration.current.orientation == PORTRAIT_SECONDARY
    }

    // Bo się powtarza...
    @Composable
    fun TextColumn(
        chapterText: ChapterText,
        textScrew: Animatable<Float, AnimationVector1D>,
        modifier: Modifier,
        state: LazyListState,
    ) {
        LazyColumn(modifier, state = state) {
            items(chapterText.elements) { element ->
                when (element) {
                    is ChapterElement.Text -> {
                        Text(
                            text = element.content,
                            style = MaterialTheme.typography.labelMedium,
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .graphicsLayer {
                                        rotationX = textScrew.value
                                    },
                        )
                    }

                    is ChapterElement.Separator -> {
                        when (element.separatorType) {
                            SeparatorType.SMALL ->
                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            8.dp,
                                        ),
                                )

                            SeparatorType.LARGE ->
                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            16.dp,
                                        ),
                                )

                            SeparatorType.NONE ->
                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            0.dp,
                                        ),
                                )
                        }
                    }
                }
            }

            item {
                val navigator = LocalNavigator.currentOrThrow
                val nextChapterExists = 0 < chapterIndex
                val prevChapterExists = chapterIndex < chapterList.size - 1
                Column {
                    TextButton(
                        colors =
                            if (nextChapterExists) {
                                ButtonDefaults.textButtonColors().copy(
                                    MaterialTheme.colorScheme
                                        .primary,
                                )
                            } else {
                                ButtonDefaults.textButtonColors()
                            },
                        onClick = {
                            navigator.replace(
                                ReaderScreen(
                                    providerName,
                                    chapterList,
                                    chapterIndex - 1,
                                ),
                            )
                        },
                        enabled = nextChapterExists,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp),
                    ) {
                        if (nextChapterExists) {
                            Text(
                                "Next: ${chapterList[chapterIndex - 1].name}",
                                color =
                                    MaterialTheme
                                        .colorScheme.surface,
                            )
                        } else {
                            Text(text = "There are no more chapters!")
                        }
                    }
                    TextButton(
                        colors =
                            if (prevChapterExists) {
                                ButtonDefaults.textButtonColors().copy(
                                    MaterialTheme.colorScheme
                                        .primary,
                                )
                            } else {
                                ButtonDefaults.textButtonColors()
                            },
                        onClick = {
                            navigator.replace(
                                ReaderScreen(
                                    providerName,
                                    chapterList,
                                    chapterIndex + 1,
                                ),
                            )
                        },
                        enabled = prevChapterExists,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp),
                    ) {
                        if (prevChapterExists) {
                            Text(
                                "Prev: ${chapterList[chapterIndex + 1].name}",
                                color =
                                    MaterialTheme
                                        .colorScheme.surface,
                            )
                        } else {
                            Text(text = "This is the first chapter")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Chapter(
        textScrew: Animatable<Float, AnimationVector1D>,
        spacerBetween: MutableState<Boolean>,
        chapterText: ChapterText,
    ) {
        Scaffold {
            Sensors(textScrew, spacerBetween)
            if (chapterText.elements.size < 10) {
                Box(modifier = Modifier.fillMaxSize(), Alignment.Center) {
                    ErrorFragment(
                        "Prawdopodobnie ten chapter jest pusty \n\n " +
                            "problem występuje po stronie dostawcy więc," +
                            " żeby odczytać to musisz znaleźć innego dostawce.",
                    )
                }
            }
            if (spacerBetween.value) {
                BoxWithConstraints(
                    Modifier
                        .padding(it)
                        .fillMaxSize(),
                ) {
                    val firstListState = rememberLazyListState()
                    val secondListState = rememberLazyListState()
                    val coroutineScope = rememberCoroutineScope()

                    val firstColumnLastElement =
                        remember {
                            derivedStateOf {
                                firstListState.layoutInfo.visibleItemsInfo.last().index
                            }
                        }

                    val firstColumnLastElementOffset =
                        remember {
                            derivedStateOf {
                                (this@BoxWithConstraints.maxHeight / 2 - 40.dp).px -
                                    firstListState.layoutInfo.visibleItemsInfo.last().offset
                            }
                        }

                    val secondColumnFirstElement =
                        remember {
                            derivedStateOf {
                                secondListState.firstVisibleItemIndex
                            }
                        }
                    val secondColumnLastElementOffset =
                        remember {
                            derivedStateOf {
                                secondListState.firstVisibleItemScrollOffset -
                                    (this@BoxWithConstraints.maxHeight / 2 - 40.dp).px
                            }
                        }

                    val lastItemFullyVisible =
                        remember {
                            derivedStateOf {
                                if (secondListState.layoutInfo.visibleItemsInfo.lastOrNull() == null) {
                                    true
                                } else {
                                    val item = secondListState.layoutInfo.visibleItemsInfo.last()
                                    (
                                        item.index == chapterText.elements.size &&
                                            item.offset + item.size <= secondListState.layoutInfo.viewportSize.height
                                    )
                                }
                            }
                        }

                    val firstItemFullyVisible =
                        remember {
                            derivedStateOf {
                                firstListState.firstVisibleItemIndex == 0 && firstListState.firstVisibleItemScrollOffset == 0
                            }
                        }

                    val nestedScrollConnectionFirst =
                        remember {
                            object : NestedScrollConnection {
                                override fun onPreScroll(
                                    available: Offset,
                                    source: NestedScrollSource,
                                ): Offset {
                                    // Nawet mnie nie pytaj czemu to tak działa... to jest dziwne
                                    // AAAAA DOBRA BO TO ZWRACA ILE ZJADŁO XDDDD ALE KURWA BEZ SENSU
                                    // the amount this connection consumed ...
                                    return if (lastItemFullyVisible.value && available.y < 0) {
                                        available
                                    } else {
                                        Offset.Zero
                                    }
                                }

                                override fun onPostScroll(
                                    consumed: Offset,
                                    available: Offset,
                                    source: NestedScrollSource,
                                ): Offset {
                                    coroutineScope.launch {
                                        if (!lastItemFullyVisible.value ||
                                            (
                                                lastItemFullyVisible.value &&
                                                    consumed.y > 0
                                            )
                                        ) { // ... dziwne rzeczy
                                            secondListState.scrollToItem(
                                                firstColumnLastElement.value,
                                                firstColumnLastElementOffset.value,
                                            )
                                        }
                                    }
                                    return Offset.Zero
                                }
                            }
                        }

                    val nestedScrollConnectionSecond =
                        remember {
                            object : NestedScrollConnection {
                                override fun onPreScroll(
                                    available: Offset,
                                    source: NestedScrollSource,
                                ): Offset {
                                    // the amount this connection consumed ...
                                    return if (firstItemFullyVisible.value && available.y > 0) {
                                        available
                                    } else {
                                        Offset.Zero
                                    }
                                }

                                override fun onPostScroll(
                                    consumed: Offset,
                                    available: Offset,
                                    source: NestedScrollSource,
                                ): Offset {
                                    if (!firstItemFullyVisible.value ||
                                        (
                                            firstItemFullyVisible.value &&
                                                consumed.y < 0
                                        )
                                    ) { // ... dziwne rzeczy
                                        coroutineScope.launch {
                                            firstListState.scrollToItem(
                                                secondColumnFirstElement.value,
                                                secondColumnLastElementOffset.value,
                                            )
                                        }
                                    }
                                    return Offset.Zero
                                }
                            }
                        }
                    LaunchedEffect(Unit) {
                        secondListState.scrollToItem(
                            firstColumnLastElement.value,
                            firstColumnLastElementOffset.value,
                        )
                    }
                    // JEJ KONIEC SYNCHRONIZACJI 2 LIST kurwa 106 linijek '_'
                    Column(
                        Modifier
                            .fillMaxSize(),
                    ) {
                        TextColumn(
                            chapterText,
                            textScrew,
                            Modifier
                                .height(this@BoxWithConstraints.maxHeight / 2 - 40.dp)
                                .nestedScroll(nestedScrollConnectionFirst),
                            firstListState,
                        )
                        Spacer(Modifier.height(40.dp))
                        TextColumn(
                            chapterText,
                            textScrew,
                            Modifier
                                .nestedScroll(nestedScrollConnectionSecond),
                            secondListState,
                        )
                    }
                }
            } else {
                val listState = rememberLazyListState()
                TextColumn(chapterText, textScrew, Modifier.padding(it), listState)
            }
        }
    }

    @Composable
    override fun Content() {
        val textScrew = remember { Animatable(0f) }
        val spacerBetween = remember { mutableStateOf(false) }
        var chapterTextStatus by remember {
            mutableStateOf<DataStatus<ChapterText>>(
                DataStatus
                    .Loading,
            )
        }
        val library = getScreenModel<LibraryScreenModel>()
        val provider = library.providers[providerName]!!
        LaunchedEffect(Unit) {
            chapterTextStatus =
                try {
                    val indb = library.chapterTextGet(chapterList[chapterIndex].chapterUrl)
                    if (indb != null) {
                        DataStatus.Success(ChapterText(indb.elements))
                    }
                    DataStatus.Success(provider.loadChapterText(chapterList[chapterIndex].chapterUrl))
                } catch (e: Exception) {
                    DataStatus.Error(e.message)
                }
        }
        when (chapterTextStatus) {
            is DataStatus.Error -> ErrorFragment((chapterTextStatus as DataStatus.Error).msg)
            DataStatus.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is DataStatus.Success -> {
                val chapterText = (chapterTextStatus as DataStatus.Success<ChapterText>).data
                Chapter(textScrew, spacerBetween, chapterText)
            }
        }
    }
}
