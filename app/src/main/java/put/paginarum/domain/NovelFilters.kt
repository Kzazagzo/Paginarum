package put.paginarum.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import put.paginarum.ui.components.common.SelectionState
import put.paginarum.ui.components.providerMainScreen.OrderDirection

@Parcelize
data class NovelFilters(
    val tag: Map<String, SelectionState>,
    val orderBy: String?,
    val orderDirection: OrderDirection?,
) : Parcelable
