package put.paginarum.ui.screens.homeTabs

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import coil.compose.AsyncImagePainter.State.Empty.painter
import kotlinx.parcelize.Parcelize
import put.paginarum.R
import put.paginarum.domain.SettingCategory
import put.paginarum.ui.models.PrivilegeScreenModel
import put.paginarum.ui.screens.PriviledgeLoginFragment
import put.paginarum.ui.screens.settingsScreen.SettingScreenImpl

@Parcelize
object SettingsTab : Tab, Parcelable {
    private fun readResolve(): Any = SettingsTab

    override val options: TabOptions
        @Composable
        get() {
            val title = "Ustawienia"
            val icon = painterResource(id = R.drawable.outline_settings_24)

            return remember {
                TabOptions(
                    index = 4u,
                    title = title,
                    icon = icon,
                )
            }
        }

    @Composable
    override fun Content() {
        val privilegeScreenModel = getScreenModel<PrivilegeScreenModel>()
        PriviledgeLoginFragment(screenModel = privilegeScreenModel)

        Surface {
            LazyColumn {
                item {
                    Box(Modifier.fillMaxWidth(), Alignment.Center) {
                        Icon(
                            modifier =
                                Modifier.size(196.dp),
                            painter = painterResource(id = R.drawable.baseline_menu_book_24),
                            contentDescription = "MainIcon",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    HorizontalDivider(
                        Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        2.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                    )
                }
                items(SettingCategory.entries.toTypedArray().dropLast(1)) { category ->
                    SettingScreenImpl(category).Facade()
                }
            }
        }
    }
}
