package put.paginarum.ui.screens

import android.content.res.Configuration
import android.os.Parcelable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import kotlinx.parcelize.Parcelize
import put.paginarum.ui.screens.homeTabs.ExploreTab
import put.paginarum.ui.screens.homeTabs.LibraryTab
import put.paginarum.ui.screens.homeTabs.SettingsTab

@Parcelize
object MainScreen : Screen, Parcelable {
    private fun readResolve(): Any = MainScreen

    @Composable
    fun RowScope.TabNavigationItem(tab: Tab) {
        val tabNavigator = LocalTabNavigator.current

        NavigationBarItem(
            selected = tabNavigator.current.key == tab.key,
            onClick = { tabNavigator.current = tab },
            icon = { tab.options.icon?.let { Icon(painter = it, contentDescription = tab.options.title) } },
            label = { Text(tab.options.title) },
        )
    }

    fun LazyListScope.tabNavigationItem(
        tabs: List<Tab>,
        tabNavigator: TabNavigator,
        itemWidth: Dp,
    ) {
        items(tabs) { tab ->
            Row(Modifier.width(itemWidth)) {
                TabNavigationItem(
                    modifier = Modifier.height(20.dp),
                    tab = tab,
                    isSelected = tabNavigator.current.key == tab.key,
                    onClick = { tabNavigator.current = tab },
                )
            }
        }
    }

    @Composable
    private fun RowScope.TabNavigationItem(
        tab: Tab,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        NavigationBarItem(
            modifier = modifier,
            selected = isSelected,
            onClick = onClick,
            icon = { tab.options.icon?.let { Icon(painter = it, contentDescription = tab.options.title) } },
            label = { Text(tab.options.title) },
        )
    }

    @Composable
    private fun ColumnScope.TabNavigationItem(tab: Tab) {
        val tabNavigator = LocalTabNavigator.current

        NavigationRailItem(
            selected = tabNavigator.current.key == tab.key,
            onClick = { tabNavigator.current = tab },
            icon = { Icon(painter = tab.options.icon!!, contentDescription = tab.options.title) },
            label = { Text(tab.options.title) },
        )
    }

    @Composable
    override fun Content() {
        val configuration = LocalConfiguration.current

        TabNavigator(ExploreTab) {
            Scaffold(
                modifier =
                    Modifier.windowInsetsPadding(
                        WindowInsets.navigationBars.only(WindowInsetsSides.Start + WindowInsetsSides.End),
                    ),
                bottomBar = {
                    when (configuration.orientation) {
                        Configuration.ORIENTATION_PORTRAIT -> {
                            NavigationBar {
                                TabNavigationItem(LibraryTab)
                                TabNavigationItem(ExploreTab)
                                TabNavigationItem(SettingsTab)
                            }
                        }
                    }
                },
            ) {
                Row(modifier = Modifier.padding(it)) {
                    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        NavigationRail {
                            Column(
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxHeight().padding(bottom = 16.dp),
                            ) {
                                TabNavigationItem(LibraryTab)
                                TabNavigationItem(ExploreTab)
                                TabNavigationItem(SettingsTab)
                            }
                        }
                    }
                    CurrentTab()
                }
            }
        }
    }
}
