package put.paginarum.ui.screens.homeTabs

import android.os.Parcelable
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import kotlinx.parcelize.Parcelize
import put.paginarum.R

@Parcelize
object HistoryTab : Tab, Parcelable {
    private fun readResolve(): Any = HistoryTab

    override val options: TabOptions
        @Composable
        get() {
            val title = "Historia"
            val icon = painterResource(id = R.drawable.baseline_history_24)

            return remember {
                TabOptions(
                    index = 2u,
                    title = title,
                    icon = icon,
                )
            }
        }

    @Composable
    override fun Content() {
        Surface {
        }
    }
}
