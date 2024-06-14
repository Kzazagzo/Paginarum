package put.paginarum.ui.screens.settingsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.hilt.getScreenModel
import pl.put.szlaki.ui.components.screen.settingsScreen.SettingItem
import pl.put.szlaki.ui.components.screen.settingsScreen.SettingOptions
import put.paginarum.R
import put.paginarum.ui.models.CategoryScreenModel

class LibraryCategoriesManagerInSettings(
    private val screenModel: CategoryScreenModel,
) : SettingItem("categoryManager") {
    override val options: SettingOptions
        @Composable get() {
            val categories = screenModel.categoriesNames.collectAsState()
            return SettingOptions(
                title = "Edytuj kategorie",
                description =
                    "${categories.value.size} kategorii",
                icon = R.drawable.baseline_category_24,
            )
        }

    @Composable
    override fun Content() {
        Scaffold(
            Modifier.fillMaxSize(),
            bottomBar = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(WindowInsets.navigationBars.asPaddingValues())
                            .padding
                            (bottom = 30.dp),
                ) {
                    CategoryFab(
                        { screenModel.addNewCategory(it) },
                        "Dodaj nową kategorię",
                    )
                }
            },
        ) {
            val properScreenModel = getScreenModel<CategoryScreenModel>()
            val categories by properScreenModel.categoriesNames.collectAsState()

            LazyColumn(Modifier.padding(it)) {
                itemsIndexed(categories) { index, category ->
                    var isDeviant by remember { mutableStateOf(category.isDeviant) }

                    ElevatedCard(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors =
                            CardDefaults.cardColors(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(end = 10.dp, start = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_label_important_24),
                                        contentDescription = "category",
                                    )
                                    Text(text = category.categoryName)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isDeviant,
                                        onCheckedChange = {
                                            isDeviant = it
                                            category.isDeviant = it
                                            properScreenModel.changeCategoryDeviantState(
                                                category.categoryName,
                                                category.isDeviant,
                                            )
                                        },
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.baseline_lock_24),
                                        contentDescription = "dev",
                                    )
                                }
                            }

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(
                                    Modifier.padding(start = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = category.position.toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.padding(end = 8.dp),
                                    )
                                    IconButton(
                                        modifier = Modifier.width(32.dp),
                                        onClick = { screenModel.swapCategoriesIds(categories[index - 1], category) },
                                        enabled = category.position != 1L,
                                    ) {
                                        Icon(
                                            painterResource(id = R.drawable.baseline_arrow_upward_24),
                                            "up",
                                        )
                                    }
                                    IconButton(
                                        modifier = Modifier.width(32.dp),
                                        onClick = { screenModel.swapCategoriesIds(categories[index + 1], category) },
                                        enabled = category.position.toInt() != categories.size,
                                    ) {
                                        Icon(
                                            painterResource(id = R.drawable.baseline_arrow_downward_24),
                                            "down",
                                        )
                                    }
                                }
                                var openAlertEditDialog by remember { mutableStateOf(false) }
                                if (openAlertEditDialog) {
                                    CategoryDialog(
                                        onDismiss = { openAlertEditDialog = false },
                                        onConfirm = { text ->
                                            category.categoryName = text
                                            screenModel.editCategoryName(category)
                                            openAlertEditDialog = false
                                        },
                                        entryText = category.categoryName,
                                        dialogTitle = "Edituj kategorię",
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            openAlertEditDialog = true
                                        },
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_mode_edit_24),
                                            "",
                                        )
                                    }
                                    var openAlertDeleteDialog by remember { mutableStateOf(false) }
                                    if (openAlertDeleteDialog) {
                                        DeleteDialog(
                                            onDismiss = { openAlertDeleteDialog = false },
                                            onConfirm = {
                                                screenModel.deleteCategory(category)
                                                openAlertDeleteDialog = false
                                            },
                                        )
                                    }
                                    IconButton(
                                        onClick = { openAlertDeleteDialog = true },
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_delete_24),
                                            "",
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CategoryFab(
        action: (String) -> Unit,
        dialogTitle: String,
        entryText: String = "",
    ) {
        var openAlertDialog by remember { mutableStateOf(false) }

        ExtendedFloatingActionButton(
            modifier = Modifier.clip(RoundedCornerShape(20.dp)),
            onClick = {
                openAlertDialog = true
            },
        ) {
            Icon(
                painterResource(id = R.drawable.sharp_add_24),
                contentDescription = "Dodaj kategorę",
            )
            Text(text = "      Dodaj kategorę")
        }

        if (openAlertDialog) {
            CategoryDialog(
                onDismiss = { openAlertDialog = false },
                onConfirm = { text ->
                    action(text)
                    openAlertDialog = false
                },
                entryText = entryText,
                dialogTitle = dialogTitle,
            )
        }
    }

    @Composable
    private fun CategoryDialog(
        onDismiss: () -> Unit,
        onConfirm: (String) -> Unit,
        entryText: String,
        dialogTitle: String,
    ) {
        val text = remember { mutableStateOf(entryText) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(dialogTitle) },
            text = {
                TextField(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(20.dp)),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    value = text.value,
                    onValueChange = { text.value = it },
                    placeholder = { Text(text = "Podaj nazwę kategorii...") },
                    colors =
                        TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.secondary,
                            unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                            focusedContainerColor = MaterialTheme.colorScheme.onSecondary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.onSecondary,
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.secondary,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.secondary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                )
            },
            confirmButton = {
                IconButton(onClick = { onConfirm(text.value) }) {
                    Icon(painter = painterResource(R.drawable.baseline_check_24), contentDescription = "yes")
                }
            },
            dismissButton = {
                IconButton(onClick = onDismiss) {
                    Icon(painter = painterResource(R.drawable.baseline_cancel_24), contentDescription = "no")
                }
            },
        )
    }

    @Composable
    private fun DeleteDialog(
        onDismiss: () -> Unit,
        onConfirm: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Usuń kategorię") },
            text = {
                Text("Czy napewno chcesz usunąć tę kategorię")
            },
            confirmButton = {
                IconButton(onClick = { onConfirm() }) {
                    Icon(painter = painterResource(R.drawable.baseline_check_24), contentDescription = "yes")
                }
            },
            dismissButton = {
                IconButton(onClick = onDismiss) {
                    Icon(painter = painterResource(R.drawable.baseline_cancel_24), contentDescription = "no")
                }
            },
        )
    }
}
