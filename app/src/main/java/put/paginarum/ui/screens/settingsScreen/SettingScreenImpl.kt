package put.paginarum.ui.screens.settingsScreen

import android.os.Parcelable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import pl.put.szlaki.ui.components.screen.settingsScreen.SettingItem
import pl.put.szlaki.ui.components.screen.settingsScreen.SettingOptions
import put.paginarum.R
import put.paginarum.domain.Setting
import put.paginarum.domain.SettingCategory
import put.paginarum.domain.al.UserAl
import put.paginarum.ui.components.common.BackButton
import put.paginarum.ui.components.common.ErrorFragment
import put.paginarum.ui.components.common.LoadingFragment
import put.paginarum.ui.components.common.SelectableRow
import put.paginarum.ui.components.settings.TrackerUserRow
import put.paginarum.ui.models.CategoryScreenModel
import put.paginarum.ui.models.PrivilegeScreenModel
import put.paginarum.ui.models.SettingsScreenModel
import put.paginarum.ui.models.TrackerScreenModel
import put.paginarum.util.DataStatus

@Parcelize
class SettingScreenImpl(private val category: SettingCategory) :
    SettingItem(category.toString()),
    Parcelable {
    override val options: SettingOptions
        @Composable
        get() =
            SettingOptions(
                title = category.toString(),
                description = "",
                when (category) {
                    SettingCategory.General -> R.drawable.outline_tune_24
                    SettingCategory.Tracking -> R.drawable.outline_polyline_24
                    SettingCategory.Security -> R.drawable.baseline_security_24
                    SettingCategory.Library -> R.drawable.baseline_book_24
                    SettingCategory.Hidden -> R.drawable.baseline_empty_24
                },
            )

    @Composable
    override fun Content() {
        val settingScreenModel = getScreenModel<SettingsScreenModel>()
        val settingsCategory = settingScreenModel.settings.collectAsState()
        val trackerScreenModel = getScreenModel<TrackerScreenModel>()
        val scope = rememberCoroutineScope()
        when (settingsCategory.value) {
            is DataStatus.Error -> ErrorFragment()
            DataStatus.Loading -> LoadingFragment()
            is DataStatus.Success -> {
                var logOutDialog by remember { mutableStateOf(false) }
                if (logOutDialog) {
                    LogOutDialog(onLogOut = {
                        scope.launch {
                            trackerScreenModel.logOut()
                        }
                    }) {
                        logOutDialog = false
                    }
                }

                val categories =
                    (
                        settingsCategory.value as DataStatus.Success<
                            Map<
                                String,
                                List<Setting<*>>,
                            >,
                        >
                    ).data

                Surface {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp), // nie wiem co tu się dzieje
                    ) {
                        BackButton(navigator = LocalNavigator.currentOrThrow)
                        Spacer(
                            Modifier
                                .fillMaxWidth()
                                .height(12.dp),
                        )
                        Text(
                            category.toString(),
                            style =
                                MaterialTheme
                                    .typography.displayMedium,
                            modifier = Modifier.padding(16.dp),
                        )
                        Spacer(
                            Modifier
                                .fillMaxWidth()
                                .height(24.dp),
                        )
                        when (category) {
                            SettingCategory.General -> {
                            }

                            SettingCategory.Tracking -> {
                                trackerScreenModel.fetchCurrentUserData()
                                val trackingStatus =
                                    trackerScreenModel.alTrackingStatus.collectAsState()
                                val context = LocalContext.current

                                SelectableRow(
                                    rowText = "AniList",
                                    action = { trackerScreenModel.alAuth(context) },
                                    onLongClick = { logOutDialog = true },
                                )

                                if (trackingStatus.value) {
                                    val userStatus =
                                        trackerScreenModel.currentUserData
                                            .collectAsState()
                                    when (userStatus.value) {
                                        is DataStatus.Error ->
                                            ErrorFragment((userStatus.value as DataStatus.Error).msg)

                                        DataStatus.Loading -> LoadingFragment()
                                        is DataStatus.Success -> {
                                            val userAl =
                                                (userStatus.value as DataStatus.Success<UserAl>).data
                                            TrackerUserRow(userAl)
                                        }
                                    }
                                }
                            }

                            SettingCategory.Security -> {
                                val privilegeScreenModel = getScreenModel<PrivilegeScreenModel>()
                                var paswordResetDialogShown by remember { mutableStateOf(false) }
                                if (paswordResetDialogShown) {
                                    Dialog(onDismissRequest = { paswordResetDialogShown = true }) {
                                        Column {
                                            Text(text = "Czy na pewno chcesz usunąć hasło?")
                                            Row(
                                                Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceAround,
                                            ) {
                                                OutlinedButton(onClick = { paswordResetDialogShown = false }) {
                                                    Text(text = "Nie")
                                                }
                                                OutlinedButton(onClick = {
                                                    privilegeScreenModel.disablePassword()
                                                    paswordResetDialogShown = false
                                                }) {
                                                    Text(text = "Tak")
                                                }
                                            }
                                        }
                                    }
                                }

                                SetPasswordSetting(getScreenModel<CategoryScreenModel>()).Facade()
                                if (privilegeScreenModel.isPasswordSet()) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth().padding(16.dp)
                                            .clickable {
                                                paswordResetDialogShown = true
                                            },
                                    ) {
                                        Icon(
                                            modifier = Modifier.padding(end = 8.dp),
                                            painter =
                                                painterResource(
                                                    id =
                                                        R.drawable
                                                            .baseline_lock_open_24,
                                                ),
                                            contentDescription = "",
                                            tint =
                                                MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = "Wyłącz hasło",
                                        )
                                    }
                                }
                            }

                            SettingCategory.Library -> {
                                LibraryCategoriesManagerInSettings(getScreenModel<CategoryScreenModel>()).Facade()
                            }

                            SettingCategory.Hidden -> {
                                throw Exception("Niepowinno tu tego być")
                            }
                        }
                        categories[category.toString()]?.let { settings ->
                            LazyColumn {
                                items(settings) { setting ->
                                    SettingItemRow(setting = setting)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun LogOutDialog(
        onLogOut: () -> Unit,
        onDispose: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = { onDispose() },
            confirmButton = {
                TextButton(onClick = {
                    onLogOut()
                    onDispose()
                }) {
                    Text("Tak")
                }
            },
            dismissButton = {
                TextButton(onClick = { onDispose() }) {
                    Text("Anuluj")
                }
            },
            title = {
                Text("Potwierdź operację")
            },
            text = {
                Text("Czy na pewno chcesz się wylogować?")
            },
        )
    }
}
