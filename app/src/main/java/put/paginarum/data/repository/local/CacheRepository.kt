package put.paginarum.data.repository.local

import androidx.annotation.WorkerThread
import org.jsoup.nodes.Document
import put.paginarum.database.AppDatabase
import put.paginarum.database.CachedPage
import javax.inject.Inject

class CacheRepository
    @Inject
    constructor(
        private val appDatabase: AppDatabase,
    ) {
        @WorkerThread
        suspend fun getPageHtml(url: String): Document? {
            return appDatabase.cacheDao.getCachedPage(url)
        }

        suspend fun insertPage(
            url: String,
            page: Document,
        ) {
            appDatabase.cacheDao.insertCachedPage(CachedPage(url, page))
        }
    }
