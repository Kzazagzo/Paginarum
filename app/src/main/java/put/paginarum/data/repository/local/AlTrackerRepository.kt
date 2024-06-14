package put.paginarum.data.repository.local

import androidx.annotation.WorkerThread
import put.paginarum.database.AppDatabase
import put.paginarum.database.al.AlTrackerEntity
import javax.inject.Inject

class AlTrackerRepository
    @Inject
    constructor(
        private val appDatabase: AppDatabase,
    ) {
        @WorkerThread
        suspend fun getAlTrackerByProviderNovelName(providerNovelName: String): AlTrackerEntity? {
            return appDatabase.alTrackerDao.getAlTrackerByProviderNovelName(providerNovelName)
        }

        @WorkerThread
        suspend fun insertAlTracker(alTrackerEntity: AlTrackerEntity) {
            appDatabase.alTrackerDao.insertAlTracker(alTrackerEntity)
        }

        @WorkerThread
        suspend fun deleteAlTracker(alTrackerEntity: AlTrackerEntity) {
            appDatabase.alTrackerDao.deleteAlTracker(alTrackerEntity)
        }

        @WorkerThread
        suspend fun deleteAllTrackers() {
            appDatabase.alTrackerDao.deleteAllTrackers()
        }
    }
