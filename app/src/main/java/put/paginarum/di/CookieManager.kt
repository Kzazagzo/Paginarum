package put.paginarum.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import put.paginarum.domain.CookieManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CookieManagerModule {
    @Provides
    @Singleton
    fun provideCookieManager(
        @ApplicationContext context: Context,
    ): CookieManager {
        return CookieManager(context)
    }
}
