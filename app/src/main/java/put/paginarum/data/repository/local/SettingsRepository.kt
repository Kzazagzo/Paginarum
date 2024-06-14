package put.paginarum.data.repository.local

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.parcelize.RawValue
import put.paginarum.database.AppDatabase
import put.paginarum.database.settings.asDomainModel
import put.paginarum.domain.Setting
import put.paginarum.domain.SettingCategory
import put.paginarum.domain.asDatabaseModel
import javax.inject.Inject

class SettingsRepository
    @Inject
    constructor(
        private val appDatabase: AppDatabase,
    ) {
        val settings: Flow<Map<String, List<Setting<*>>>> =
            appDatabase.settingsDao.getAllSettings()
                .map {
                    it.groupBy { it.settingCategory.toString() }
                        .mapValues { entry ->
                            entry.value.map { it.asDomainModel() }
                        }
                }
        val password: Flow<String?> =
            appDatabase.settingsDao.getPassword()

        @WorkerThread
        suspend fun <T> insertSetting(
            settingName: String,
            settingCategory: SettingCategory,
            settingValue: @RawValue T,
        ) {
            appDatabase.settingsDao.insertSettings(
                Setting(settingName, settingCategory, settingValue).asDatabaseModel(),
            )
        }

        @WorkerThread
        suspend fun deleteSetting(settingName: String) {
            appDatabase.settingsDao.deleteSetting(
                settingName,
            )
        }

        @WorkerThread
        fun getSetting(key: String): Flow<Setting<*>?> {
            return appDatabase.settingsDao.getSetting(key).map { entity ->
                entity?.asDomainModel()
            }
        }
    }
