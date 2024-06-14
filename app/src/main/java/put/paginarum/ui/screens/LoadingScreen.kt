package put.paginarum.ui.screens

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.parcelize.Parcelize
import put.paginarum.ui.components.common.LoadingFragment
import put.paginarum.ui.models.LibraryScreenModel
import put.paginarum.util.DataStatus

@Parcelize
object LoadingScreen : Screen, Parcelable {
    private fun readResolve(): Any = LoadingScreen

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val contentLoaded by getScreenModel<LibraryScreenModel>().novelsInCategories.collectAsState(
            initial = DataStatus.Loading,
        )

        LaunchedEffect(contentLoaded) {
            when (contentLoaded) {
                is DataStatus.Error -> DataStatus.Error((contentLoaded as DataStatus.Error).msg)
                DataStatus.Loading -> DataStatus.Loading
                is DataStatus.Success -> {
//                    navigator?.replace(
//                        PrivilegeScreen {
                    navigator?.replace(MainScreen)
//                        },
//                    )
                }
            }
        }

        LoadingFragment()
    }
}
