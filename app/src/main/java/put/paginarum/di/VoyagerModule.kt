package put.paginarum.di

import android.content.Context
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.hilt.ScreenModelKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import put.paginarum.ui.models.CacheScreenModel
import put.paginarum.ui.models.CategoryScreenModel
import put.paginarum.ui.models.LibraryScreenModel
import put.paginarum.ui.models.PrivilegeScreenModel
import put.paginarum.ui.models.SettingsScreenModel
import put.paginarum.ui.models.TrackerScreenModel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppContext {
    @Provides
    @Singleton
    fun provideAppContext(
        @ApplicationContext appContext: Context,
    ): Context {
        return appContext
    }
}

@Module
@InstallIn(ActivityComponent::class)
abstract class VoyagerModule {
    @Binds
    @IntoMap
    @ScreenModelKey(LibraryScreenModel::class)
    abstract fun 零(libraryScreenModel: LibraryScreenModel): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelKey(CacheScreenModel::class)
    abstract fun 一(cacheScreenModel: CacheScreenModel): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelKey(PrivilegeScreenModel::class)
    abstract fun 二(privilegeScreenModel: PrivilegeScreenModel): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelKey(SettingsScreenModel::class)
    abstract fun 为什么我们他妈的有这么多(settingsScreenModel: SettingsScreenModel): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelKey(CategoryScreenModel::class)
    abstract fun 我正在重击(categoryScreenModel: CategoryScreenModel): ScreenModel

    @Binds
    @IntoMap
    @ScreenModelKey(TrackerScreenModel::class)
    abstract fun クソ母親(trackerScreenModel: TrackerScreenModel): ScreenModel
}
