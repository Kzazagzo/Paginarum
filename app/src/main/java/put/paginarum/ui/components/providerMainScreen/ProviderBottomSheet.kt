package put.paginarum.ui.components.providerMainScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import put.paginarum.data.api.providerApi.ProviderApi
import put.paginarum.domain.NovelFilters
import put.paginarum.ui.components.common.ExpandableDrawer
import put.paginarum.ui.components.common.SelectableSortButton
import put.paginarum.ui.components.common.SelectionState
import put.paginarum.ui.components.common.TagedTriStateCheckBox

@Composable
fun ProviderBottomSheet(
    provider: ProviderApi,
    onFilter: (NovelFilters) -> Unit,
) {
    var selectedSort by remember { mutableStateOf<Pair<String, OrderDirection>?>(null) }
    val chosenTags =
        remember {
            mutableStateMapOf<String, SelectionState>().apply {
                provider.tags.forEach { tag ->
                    this[tag.key] = SelectionState.UNSELECTED
                }
            }
        }

    Column(
        Modifier.padding(
            WindowInsets.systemBars.asPaddingValues(),
        ),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { onFilter(NovelFilters(chosenTags, "", null)) }) {
                Text("Reset")
            }

            OutlinedButton(onClick = {
                onFilter(
                    NovelFilters(chosenTags, selectedSort?.first, selectedSort?.second),
                )
            }) {
                Text("Filter")
            }
        }
        HorizontalDivider(
            Modifier
                .fillMaxWidth()
                .height(5.dp),
        )
        ExpandableDrawer(title = "Sort") {
            LazyColumn(Modifier.fillMaxWidth()) {
                items(provider.ordersBy.keys.toList()) {
                    val isSelected = selectedSort?.first == it
                    val orderDirectionSanitized = selectedSort?.second ?: OrderDirection.NONE
                    SelectableSortButton(it, isSelected, orderDirectionSanitized) { sortHow ->
                        selectedSort = Pair(it, sortHow)
                    }
                }
            }
        }
        ExpandableDrawer(title = "Tags") {
            LazyColumn(Modifier.fillMaxWidth()) {
                items(provider.tags.keys.toList()) {
                    TagedTriStateCheckBox(it, chosenTags[it]!!) { selected ->
                        chosenTags[it] = selected
                    }
                }
            }
        }
        Spacer(
            modifier =
                Modifier
                    .height(150.dp)
                    .fillMaxWidth(),
        )
    }
}

enum class OrderDirection {
    NONE,
    ASC,
    DESC,
}
