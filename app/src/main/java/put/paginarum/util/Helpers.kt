package put.paginarum.util

import android.content.res.Resources.getSystem
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
* Change Dp value to Int (pixel value) using Screen Density
 **/
val Dp.px: Int get() = (this * getSystem().displayMetrics.density).value.toInt()

/**
 * Change Int (pixel value) to Dp using Screen Density
 **/
val Int.toDp: Dp get() = (this / getSystem().displayMetrics.density).toInt().dp

fun cookieDomainTransformer(provider: String) = "https://$provider/"
