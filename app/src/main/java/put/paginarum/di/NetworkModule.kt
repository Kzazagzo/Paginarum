package put.paginarum.di

import android.content.Context
import androidx.fragment.app.FragmentActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import put.paginarum.data.repository.network.CloudFlareKiller
import put.paginarum.network.TokenProvider
import put.paginarum.network.TokenProviderImpl
import put.paginarum.network.al.GraphQLService
import put.paginarum.ui.models.CacheScreenModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SingletonOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ActivityScopedOkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object SingletonNetworkModule {
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenProvider: TokenProvider): Interceptor {
        return Interceptor { chain ->
            val original = chain.request()
            val token = runBlocking { tokenProvider.getToken() }
            val requestBuilder: Request.Builder =
                original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
            val request: Request = requestBuilder.build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @SingletonOkHttpClient
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @SingletonOkHttpClient okHttpClient: OkHttpClient,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://graphql.anilist.co/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAlApi(retrofit: Retrofit): GraphQLService {
        return retrofit.create(GraphQLService::class.java)
    }
}

@Module
@InstallIn(ActivityComponent::class)
object ActivityScopedNetworkModule {
    @Provides
    @ActivityScoped
    fun provideCloudFlareKiller(
        @ActivityContext context: Context,
        activity: FragmentActivity,
        cacheScreenModel: CacheScreenModel,
    ): CloudFlareKiller {
        return CloudFlareKiller(activity, cacheScreenModel)
    }

    @ActivityScopedOkHttpClient
    @Provides
    @ActivityScoped
    fun provideOkHttpClientWithCloudFlareKiller(
        loggingInterceptor: HttpLoggingInterceptor,
        cloudFlareKiller: CloudFlareKiller,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(cloudFlareKiller)
            .build()
    }
}

// NIE USUWAĆ
@Module
@InstallIn(SingletonComponent::class)
abstract class TokenModule {
    @Binds
    @Singleton
    abstract fun bindTokenProvider(tokenProviderImpl: TokenProviderImpl): TokenProvider
}
