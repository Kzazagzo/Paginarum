package put.paginarum.ui.models

import android.content.Context
import android.content.Intent
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import put.paginarum.activity.CloudBypasser
import put.paginarum.data.api.providerApi.ProviderApi
import put.paginarum.data.repository.local.CacheRepository
import put.paginarum.domain.CookieManager
import javax.inject.Inject

@ActivityScoped
class CacheScreenModel
    @Inject
    constructor(
        val cacheRepository: CacheRepository,
        private val context: Context,
        private val cookieManager: CookieManager,
    ) : ScreenModel {
        val cookies: StateFlow<Map<String, String>> = cookieManager.cookies
        val userAgent = cookieManager.userAgent

        fun resolveCloudFlare(providerApi: ProviderApi) {
            screenModelScope.launch {
                val intent =
                    CloudBypasser.createIntent(context, providerApi.mainUrl).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                context.startActivity(intent)
            }
        }
    }
