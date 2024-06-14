package put.paginarum.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CloudBypasser : AppCompatActivity() {
    @Inject
    lateinit var cookieManager: put.paginarum.domain.CookieManager

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_COOKIE = "extra_cookie"

        fun createIntent(
            context: Context,
            url: String,
        ): Intent {
            return Intent(context, CloudBypasser::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
        }
    }

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        webView = WebView(this)
        setContentView(webView)

        val url = intent.getStringExtra(EXTRA_URL) ?: return

        webView.settings.javaScriptEnabled = true

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        val userAgent = webView.settings.userAgentString
        cookieManager.setUserAgent(userAgent)

        webView.loadUrl(url)
    }

    override fun onDestroy() {
        super.onDestroy()
        val url = webView.url
        val cookie = CookieManager.getInstance().getCookie(url)
        cookieManager.addCookie(url.toString(), cookie)
        val resultIntent =
            Intent().apply {
                putExtra(EXTRA_COOKIE, cookie)
            }
        setResult(RESULT_OK, resultIntent)
    }
}
