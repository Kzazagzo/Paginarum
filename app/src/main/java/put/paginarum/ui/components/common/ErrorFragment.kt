package put.paginarum.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ErrorFragment(
    error: String? = null,
    modifier: Modifier = Modifier,
) {
    val emoticons =
        listOf(
            "(·_·)",
            "(◕‿◕)",
            "(¬‿¬)",
            "(˘︹˘)",
            "(ಠ_ಠ)",
            "(ಠ‿ಠ)",
            "(¬‿¬)",
            "(⌐■_■)",
            "(•_•)",
            "(≧◡≦)",
            "(￣^￣)",
            "(＠_＠)",
            "(︶︹︺)",
            "(T_T)",
            "(>_<)",
            "(⊙_◎)",
            "(ಠ‿↼)",
            "(°ロ°)☝",
            "(҂⌣̀_⌣́)",
            "(ʘ‿ʘ)",
            "(ಠ⌣ಠ)",
            "(✧ω✧)",
            "(ಥ﹏ಥ)",
            "(ಥ‿ಥ)",
            "(ಥ_ಥ)",
            "(ʘ‿ʘ)",
            "(╥_╥)",
            "(ʘ╭╮ʘ)",
            "(ಠ‿↼)",
            "(´･_･`)",
            "(⌒_⌒;)",
            "(¬‿¬)",
            "(¬_¬)",
            "(ಠ◡ಠ)",
            "(⊙_⊙)",
            "(≧ω≦)",
            "(≧∇≦)/",
            "(≧▽≦)",
            "(^_^)",
            "(✧_✧)",
            "(¬‿¬ )",
            "(ง°ل͜°)ง",
            "(◕‿◕✿)",
            "(ᵔᴥᵔ)",
            "(￣︿￣)",
            "( ‾ʖ̫‾)",
            "(¬_¬)",
            "(ﾟヮﾟ)",
            "(✿´‿`)",
            "(╥﹏╥)",
            "(･_･)",
            "(≧◡≦)",
            "( ≧Д≦)",
            "(╯︵╰,)",
            "(≖_≖ )",
            "(≖‿≖)",
            "(⊙ω⊙)",
            "(⊙︿⊙)",
            "(⊙_☉)",
            "(☉_☉)",
            "(⌐■_■)",
            "(◉‿◉)",
            "(╥_╥)",
        )

    val randomEmoticon = remember { emoticons.random() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = randomEmoticon,
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 72.sp,
                        fontWeight =
                            FontWeight.Bold,
                    ),
                modifier = Modifier.padding(bottom = 16.dp),
            )
            if (error?.isNotEmpty() == true) {
                Text(
                    text = "$error",
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
