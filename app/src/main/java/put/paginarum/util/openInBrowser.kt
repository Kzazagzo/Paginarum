package put.paginarum.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object BrowserUtils {
    fun openInBrowser(
        url: String,
        context: Context,
    ) {
        val intent =
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
        context.startActivity(intent)
    }
}
