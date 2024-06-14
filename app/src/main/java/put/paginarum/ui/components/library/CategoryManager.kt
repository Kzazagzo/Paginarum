package put.paginarum.ui.components.library

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import put.paginarum.R
import put.paginarum.database.novel.NovelData
import put.paginarum.domain.CategoryName
import put.paginarum.ui.components.common.ErrorFragment
import put.paginarum.ui.models.CacheScreenModel
import put.paginarum.ui.models.CategoryScreenModel
import put.paginarum.ui.models.LibraryScreenModel
import put.paginarum.ui.screens.NovelScreen
import put.paginarum.ui.screens.settingsScreen.LibraryCategoriesManagerInSettings
import put.paginarum.util.DataStatus

@Composable
fun CategoryManager(
    novels: List<NovelData>,
    screenModel: LibraryScreenModel,
    categoryScreenModel: CategoryScreenModel,
    selectedNovels: MutableState<List<NovelData>>,
    cacheScreenModel: CacheScreenModel,
) {
    var categoryChangeWindow by remember { mutableStateOf(false) }
    val parentNavigator = LocalNavigator.currentOrThrow.parent

    if (categoryChangeWindow) {
        CategoryChangeDialog(
            categoryScreenModel,
            selectedNovels.value,
            { categoryChangeWindow = false },
            { parentNavigator?.push(LibraryCategoriesManagerInSettings(categoryScreenModel)) },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = selectedNovels.value.isNotEmpty(),
                enter =
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 300),
                    ),
                exit =
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(durationMillis = 300),
                    ),
            ) {
                if (selectedNovels.value.isNotEmpty()) {
                    BottomBar(
                        onDeleteClick = {
                            selectedNovels.value.forEach {
                                screenModel.deleteNovelFromLibrary(it.novelUrl)
                            }
                            selectedNovels.value = emptyList()
                        },
                        onChangeCategoryClick = { categoryChangeWindow = true },
                    )
                }
            }
        },
    ) { innerPadding ->
        val searchText = screenModel.searchText.collectAsState()

        if (novels.isEmpty()) {
            if (searchText.value.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    ErrorFragment("Kategoria jest pusta, dodaj najpierw pozcyję!")
                }
            } else {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    ErrorFragment("Nie znaleziono pozycji o takiej nazwie!")
                }
            }
        } else {
            val cookies by cacheScreenModel.cookies.collectAsState()

            val configuration = LocalConfiguration.current
            val columns =
                if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    5
                } else {
                    3
                }
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                val userAgent = cacheScreenModel.userAgent.value
                items(novels) { novel ->
                    val isSelected = novel in selectedNovels.value
                    NovelLibraryItem(
                        novel = novel,
                        onClick = {
                            if (selectedNovels.value.isNotEmpty()) {
                                selectedNovels.value =
                                    if (isSelected) {
                                        selectedNovels.value - novel
                                    } else {
                                        selectedNovels.value + novel
                                    }
                            } else {
                                parentNavigator?.push(
                                    NovelScreen(
                                        novel.provider,
                                        it.novelUrl,
                                        it.title.toString(),
                                    ),
                                )
                            }
                        },
                        onLongClick = {
                            selectedNovels.value =
                                if (isSelected) {
                                    selectedNovels.value - novel
                                } else {
                                    selectedNovels.value + novel
                                }
                        },
                        userAgent,
                        cookies[novel.provider + '/'],
                        isNovelSelected = selectedNovels.value.contains(novel)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChangeDialog(
    categoryScreenModel: CategoryScreenModel,
    szlaki: List<NovelData>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val categoryMap =
        remember {
            mutableStateOf<DataStatus<MutableMap<CategoryName, Boolean>>>(DataStatus.Loading)
        }

    LaunchedEffect(szlaki) {
        coroutineScope.launch {
            var combinedMap = mutableMapOf<CategoryName, Boolean>()
            if (szlaki.isNotEmpty()) {
                combinedMap = categoryScreenModel.getMapOfBelongingCategories(szlaki.first())
                combinedMap.map { (category, _) ->
                    szlaki.all { szlak ->
                        categoryScreenModel.getMapOfBelongingCategories(szlak)[category] == true
                    }
                }
            }
            categoryMap.value = DataStatus.Success(combinedMap)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        val categoryNames = categoryScreenModel.categoriesNames.collectAsState()
        Surface(
            modifier = Modifier.heightIn(min = 150.dp),
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 24.dp,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Zmień kategorię",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                if (categoryNames.value.isEmpty()) {
                    ErrorFragment("Dodaj kategorie aby przydzielić ten szlak")
                }
                when (categoryMap.value) {
                    is DataStatus.Error -> ErrorFragment((categoryMap.value as DataStatus.Error).msg)
                    DataStatus.Loading -> CircularProgressIndicator()
                    is DataStatus.Success -> {
                        val loadedCategoryMap = (categoryMap.value as DataStatus.Success).data
                        loadedCategoryMap.forEach { (category, selected) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            coroutineScope.launch {
                                                szlaki.forEach {
                                                    val newCheckedState = !selected
                                                    if (newCheckedState) {
                                                        categoryScreenModel.insertNovelCategoryJoin(
                                                            it,
                                                            category,
                                                        )
                                                    } else {
                                                        categoryScreenModel.deleteNovelCategoryJoin(
                                                            it,
                                                            category,
                                                        )
                                                    }
                                                    loadedCategoryMap[category] =
                                                        newCheckedState
                                                }
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                            ) {
                                Checkbox(
                                    modifier = Modifier.padding(end = 8.dp),
                                    checked = selected,
                                    onCheckedChange = null,
                                )
                                Text(text = category.categoryName)
                            }
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                ) {
                    IconButton(modifier = Modifier.width(130.dp), onClick = onEdit) {
                        Text(text = "Edytuj kategorie", color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painterResource(id = R.drawable.baseline_cancel_24),
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    onDeleteClick: () -> Unit,
    onChangeCategoryClick: () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick()
                    showDialog = false
                }) {
                    Text("Tak")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Anuluj")
                }
            },
            title = {
                Text("Potwierdź operację")
            },
            text = {
                Text("Czy na pewno chcesz usunąć te pozycje?")
            },
        )
    }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier =
                Modifier
                    .shadow(8.dp, RoundedCornerShape(50), clip = false)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(50),
                    )
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(50),
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .clip(RoundedCornerShape(50)),
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onChangeCategoryClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_label_important_24),
                        contentDescription = "category",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = "delete",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}
