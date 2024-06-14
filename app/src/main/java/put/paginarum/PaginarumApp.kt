package put.paginarum

import android.app.Application
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.fragment.app.FragmentActivity
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.LocalNavigatorSaver
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.parcelableNavigatorSaver
import cafe.adriel.voyager.transitions.FadeTransition
import com.example.compose.PaginarumTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import org.jetbrains.annotations.VisibleForTesting
import put.paginarum.ui.screens.LoadingScreen

@HiltAndroidApp
class PaginarumApp : Application()

@AndroidEntryPoint
@VisibleForTesting
class MainActivity : FragmentActivity() {
    @OptIn(ExperimentalVoyagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        super.onCreate(savedInstanceState)

        setContent {
            PaginarumTheme {
                CompositionLocalProvider(
                    LocalNavigatorSaver provides parcelableNavigatorSaver(),
                ) {
                    Navigator(
                        screen = LoadingScreen,
                    ) {
                        FadeTransition(navigator = it)
                    }
                }
            }
        }
    }
}
