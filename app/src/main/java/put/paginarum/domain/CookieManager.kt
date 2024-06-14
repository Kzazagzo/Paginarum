package put.paginarum.domain

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CookieManager
    @Inject
    constructor(context: Context) {
        private val _cookies = MutableStateFlow<Map<String, String>>(emptyMap())
        val cookies = _cookies.asStateFlow()

        private val _userAgent = MutableStateFlow<String?>(null)
        val userAgent = _userAgent.asStateFlow()

        fun addCookie(
            providerName: String,
            cookie: String,
        ) {
            _cookies.value =
                _cookies.value.toMutableMap().apply {
                    this[providerName] = cookie
                }
        }

        fun getCookie(providerName: String): String? {
            return _cookies.value[providerName]
        }

        fun setUserAgent(userAgent: String) {
            _userAgent.value = userAgent
        }

        fun getUserAgent(): String? {
            return _userAgent.value
        }
    }
