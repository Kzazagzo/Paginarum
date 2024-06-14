package put.paginarum.data.api.providerApi

import androidx.annotation.WorkerThread
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import put.paginarum.data.repository.local.CacheRepository
import put.paginarum.database.novel.ChapterData
import put.paginarum.database.novel.NovelData
import put.paginarum.domain.ChapterText
import put.paginarum.domain.NovelFilters

interface SearchResponse {
    val title: String?
    val novelUrl: String
    val imageUrl: String?
}

data class SearchResponseImpl(
    override val title: String,
    override val novelUrl: String,
    override val imageUrl: String,
) : SearchResponse

data class AlSearchResponseImpl(
    val id: String,
    override val title: String,
    override val novelUrl: String,
    override val imageUrl: String,
) : SearchResponse

abstract class ProviderApi {
    abstract val name: String
    abstract val mainUrl: String
    abstract val iconUrl: String

    abstract val tags: Map<String, String>
    abstract val ordersBy: Map<String, String>

    abstract val supportAdditionalOrdering: Boolean
    abstract val usesCloudFlareKiller: Boolean

    @WorkerThread
    abstract suspend fun loadMainPage(
        pageNumber: Int,
        selectedFilters: NovelFilters,
    ): HashSet<SearchResponse>

    @WorkerThread
    abstract suspend fun loadNovelData(novelUrl: String): NovelData

    @WorkerThread
    abstract suspend fun loadNovelChapters(novelUrl: String): List<ChapterData>

    @WorkerThread
    abstract suspend fun loadChapterText(chapterUrl: String): ChapterText

    @WorkerThread
    open suspend fun downloadHtml(
        url: String,
        cacheRepository: CacheRepository,
    ): Document {
        var doc = cacheRepository.getPageHtml(url)
        return if (doc != null) {
            doc
        } else {
            doc = Jsoup.connect(url).get()
            cacheRepository.insertPage(url, doc)
            doc
        }
    }

    abstract suspend fun search(
        query: String,
        pageNumber: Int,
        selectedFilters: NovelFilters,
    ): HashSet<SearchResponse>
}
