package put.paginarum.data.repository.local

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import put.paginarum.database.AppDatabase
import put.paginarum.database.novel.ChapterData
import put.paginarum.database.novel.ChapterTextEntity
import put.paginarum.database.novel.NovelData
import put.paginarum.domain.ChapterText
import javax.inject.Inject

class NovelRepository
    @Inject
    constructor(
        private val appDatabase: AppDatabase,
    ) {
        val novels: Flow<List<NovelData>> =
            appDatabase.novelDataDao.getAllNovels()

        @WorkerThread
        suspend fun novelInsert(novelData: NovelData) {
            appDatabase.novelDataDao.novelInsert(novelData)
        }

        @WorkerThread
        suspend fun chapterListInsert(chaptersData: List<ChapterData>) {
            appDatabase.novelDataDao.chapterListInsert(chaptersData)
        }

        @WorkerThread
        suspend fun novelDelete(url: String) {
            appDatabase.novelDataDao.novelDelete(url)
        }

        @WorkerThread
        suspend fun chapterListDelete(url: String) {
            appDatabase.novelDataDao.chapterListDelete(url)
        }

        @WorkerThread
        suspend fun novelGet(url: String) {
            appDatabase.novelDataDao.novelGet(url)
        }

        @WorkerThread
        suspend fun chapterTextInsert(
            chapterUrl: String,
            novelUrl: String,
            chapterText: ChapterText,
        ) {
            appDatabase.novelDataDao.chapterTextInsert(
                ChapterTextEntity(
                    novelUrl,
                    chapterUrl,
                    chapterText.elements,
                ),
            )
        }

        @WorkerThread
        suspend fun chapterTextDelete(chapterUrl: String) {
            appDatabase.novelDataDao.chapterTextDelete(chapterUrl)
        }

        @WorkerThread
        suspend fun chapterTextGetForNovel(novelUrl: String): List<ChapterTextEntity> {
            return appDatabase.novelDataDao.chapterTextGetForNovel(novelUrl)
        }

    @WorkerThread
    suspend fun chapterTextGet(chapterUrl: String): ChapterTextEntity? {
        return appDatabase.novelDataDao.chapterTextGet(chapterUrl)
    }

    }
