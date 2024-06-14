package put.paginarum.data.repository.network

import android.content.Context
import android.webkit.CookieManager
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Interceptor
import okhttp3.Response
import put.paginarum.ui.models.CacheScreenModel
import javax.inject.Inject

class CloudFlareException : Exception(
    "Niektóre strony wymagają wykonania captchy do załadowania zawartości.\n" +
        " Kliknij poniższy przycisk aby, ją wykonać, następnie wróć do aplikacji.",
)

class CloudFlareKiller
    @Inject
    constructor(
        @ActivityContext private val context: Context,
        private val cacheScreenModel: CacheScreenModel,
    ) : Interceptor {
        private var cookies: Map<String, String> = emptyMap()

        init {
            CookieManager.getInstance().removeAllCookies(null)

            CoroutineScope(Dispatchers.IO).launch {
                cacheScreenModel.cookies.collect { cookieMap ->
                    cookies = cookieMap
                }
            }
        }

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val cookieHeader = cookies["https://" + request.url.host + '/']
            val parsedCookies = cookieHeader?.let { parseCookieMap(it) } ?: emptyMap()

            val headers = getHeaders(request.headers.toMap(), "https://" + request.url.host + '/', parsedCookies)

            val newRequest =
                request.newBuilder()
                    .headers(headers)
                    .addHeader("User-Agent", cacheScreenModel.userAgent.value.toString())
                    .build()

            val response = chain.proceed(newRequest)

            if (response.code == 403) {
                throw CloudFlareException()
            }

            return response
        }

        private fun getHeaders(
            headers: Map<String, String>,
            referer: String?,
            cookie: Map<String, String>,
        ): Headers {
            val refererMap = referer?.let { mapOf("referer" to it) } ?: mapOf()
            val cookieMap =
                if (cookie.isNotEmpty()) {
                    mapOf(
                        "Cookie" to
                            cookie.entries.joinToString(" ") {
                                "${it.key}=${it.value};"
                            },
                    )
                } else {
                    mapOf()
                }
            val tempHeaders = (headers + cookieMap + refererMap)
            return tempHeaders.toHeaders()
        }

        private fun parseCookieMap(cookie: String): Map<String, String> {
            return cookie.split(";").associate {
                val split = it.split("=")
                (split.getOrNull(0)?.trim() ?: "") to (split.getOrNull(1)?.trim() ?: "")
            }.filter { it.key.isNotBlank() && it.value.isNotBlank() }
        }
    }
