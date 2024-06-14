package put.paginarum.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.OkHttpClient
import put.paginarum.data.api.providerApi.ProviderApi
import put.paginarum.data.api.providers.BoxNovelProvider
import put.paginarum.data.api.providers.NovelsOnlineProvider
import put.paginarum.data.repository.local.CacheRepository

@Module
@InstallIn(ActivityComponent::class)
object ProviderModule {
    @Provides
    @ActivityScoped
    fun provideProviderMap(
        cacheRepository: CacheRepository,
        @ActivityScopedOkHttpClient okHttpClient: OkHttpClient,
        @ApplicationContext context: Context,
    ): Map<String, ProviderApi> {
        return mapOf(
            "Box Novel" to BoxNovelProvider(cacheRepository),
            "Novels Online" to NovelsOnlineProvider(cacheRepository, okHttpClient, context),
        )
    }
}
