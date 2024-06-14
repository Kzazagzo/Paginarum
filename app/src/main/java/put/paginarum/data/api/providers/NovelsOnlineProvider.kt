package put.paginarum.data.api.providers

import android.content.Context
import androidx.annotation.WorkerThread
import com.example.ui.theme.provider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import put.paginarum.R
import put.paginarum.data.api.providerApi.ProviderApi
import put.paginarum.data.api.providerApi.SearchResponse
import put.paginarum.data.api.providerApi.SearchResponseImpl
import put.paginarum.data.repository.local.CacheRepository
import put.paginarum.database.novel.ChapterData
import put.paginarum.database.novel.NovelData
import put.paginarum.di.ActivityScopedOkHttpClient
import put.paginarum.domain.ChapterElement
import put.paginarum.domain.ChapterText
import put.paginarum.domain.NovelFilters
import put.paginarum.domain.SeparatorType
import put.paginarum.ui.components.common.SelectionState
import javax.inject.Inject

class NovelsOnlineProvider
    @Inject
    constructor(
        private var cacheRepository: CacheRepository,
        @ActivityScopedOkHttpClient private val okHttpClient: OkHttpClient,
        @ApplicationContext context: Context,
    ) : ProviderApi() {
        override val name: String = "Novels Online"
        override val mainUrl: String = "https://novelsonline.net"
        override val iconUrl: String = "android.resource://${context.packageName}/${R.drawable.no}"
        override val supportAdditionalOrdering: Boolean = false
        override val usesCloudFlareKiller = true

        @WorkerThread
        override suspend fun loadMainPage(
            pageNumber: Int,
            selectedFilters: NovelFilters,
        ): HashSet<SearchResponse> {
            return withContext(Dispatchers.IO) {
                val selectedTags = selectedFilters.tag.filter { it.value == SelectionState.SELECTED }
                var url =
                    if (selectedTags.isEmpty()) {
                        if (selectedFilters.orderBy != "") {
                            "$mainUrl/${ordersBy[selectedFilters.orderBy]}"
                        } else {
                            "$mainUrl/top-novel"
                        }
                    } else {
                        "$mainUrl/category/${tags[selectedTags.keys.toString()]}"
                    }
                url += "/$pageNumber"

                val returnList = HashSet<SearchResponse>()

                val request =
                    okhttp3.Request.Builder()
                        .url(url)
                        .build()

                try {
                    val response = okHttpClient.newCall(request).execute()
                    val document: Document = Jsoup.parse(response.body.string())

                    val elements: List<Element> = document.select("div.top-novel-block")

                    for (element in elements) {
                        val linkElement: Element? = element.selectFirst("div.top-novel-header > h2 > a")
                        val titleElement: Element? = element.selectFirst("div.top-novel-header > h2 > a")
                        val imageElement: Element? = element.selectFirst("div.top-novel-cover > a > img")

                        if (linkElement != null && titleElement != null && imageElement != null) {
                            val link = linkElement.attr("href")
                            val title = titleElement.text()
                            val image = imageElement.attr("src")

                            returnList.add(SearchResponseImpl(title, link, image))
                        }
                    }
                } catch (e: Exception) {
                    throw e
                }

                returnList
            }
        }

        override suspend fun loadNovelData(novelUrl: String): NovelData {
            return withContext(Dispatchers.IO) {
                val request =
                    okhttp3.Request.Builder()
                        .url(novelUrl)
                        .build()
                try {
                    val response = okHttpClient.newCall(request).execute()
                    val document: Document = Jsoup.parse(response.body.string())

                    val title = document.selectFirst("div.block-title > h1")?.text()
                    val description =
                        document.selectFirst("div.novel-detail-body > p[style]")?.text()
                    val imageUrl = document.selectFirst("div.novel-cover > a > img")?.attr("src")
                    val rating = document.selectFirst("div.novel-rating .rating-container")?.text()
                    val author =
                        document.selectFirst("div.novel-detail-item:contains(Author(s)) .novel-detail-body > ul > li > a")
                            ?.text()

                    val genres =
                        document.select("div.novel-detail-item:contains(Genre) .novel-detail-body > ul > li > a")
                            .map { it.text() }

                    val type =
                        document.selectFirst("div.novel-detail-item:contains(Type) .novel-detail-body > ul > li")
                            ?.text()

                    val tags =
                        document.select("div.novel-detail-item:contains(Tag(s)) .novel-detail-body > ul > li > a")
                            .map { it.text() }
                    val status =
                        document.selectFirst("div.novel-detail-item:contains(Status) .novel-detail-body > ul > li")
                            ?.text()

                    val chapters = loadNovelChapters(novelUrl)

                    NovelData(
                        title = title,
                        description = description,
                        imageUrl = imageUrl,
                        rating = rating,
                        author = author,
                        genres = genres,
                        type = type,
                        tags = tags,
                        chapters = chapters,
                        provider = name,
                        status = status,
                        novelUrl = novelUrl,
                    )
                } catch (e: Exception) {
                    throw e
                }
            }
        }

        override suspend fun loadNovelChapters(novelUrl: String): List<ChapterData> {
            return withContext(Dispatchers.IO) {
                val request =
                    okhttp3.Request.Builder()
                        .url(novelUrl)
                        .build()
                try {
                    val response = okHttpClient.newCall(request).execute()
                    val document: Document = Jsoup.parse(response.body.string())

                    document.select("div.tab-content div.tab-pane ul.chapter-chs > li > a")
                        .map { element ->
                            val rawTitle = element.text().orEmpty()
                            val chapterUrl = element.attr("href").orEmpty()
                            ChapterData(
                                name = rawTitle,
                                chapterUrl = chapterUrl,
                                releaseDate = null,
                                novelUrl = novelUrl,
                            )
                        }
                } catch (e: Exception) {
                    throw e
                }
            }
        }

        override suspend fun loadChapterText(chapterUrl: String): ChapterText {
            return withContext(Dispatchers.IO) {
                val request =
                    okhttp3.Request.Builder()
                        .url(chapterUrl)
                        .build()
                try {
                    val response = okHttpClient.newCall(request).execute()
                    val document: Document = Jsoup.parse(response.body.string())

                    val elements = document.select("div#contentall").first()?.children() ?: emptyList()

                    val contentList = mutableListOf<ChapterElement>()

                    for (element in elements) {
                        when (element.tagName()) {
                            "p" -> {
                                if (element.text().isNotBlank() && element.selectFirst("noscript")?.hasText() != true) {
                                    contentList.add(ChapterElement.Text(element.text()))
                                }
                            }

                            "div" -> {
                                if (element.text().isNotBlank()) {
                                    contentList.add(ChapterElement.Separator(SeparatorType.LARGE))
                                }
                            }

                            "center" -> {
                                if (element.hasClass("ad1")) {
                                    continue
                                }
                            }
                        }
                    }

                    ChapterText(contentList)
                } catch (e: Exception) {
                    throw e
                }
            }
        }

        override suspend fun search(
            query: String,
            pageNumber: Int,
            selectedFilters: NovelFilters,
        ): HashSet<SearchResponse> {
            return withContext(Dispatchers.IO) {
                val selectedTags =
                    selectedFilters.tag.filter { it.value == SelectionState.SELECTED }
                val url =
                    if (selectedTags.isNotEmpty()) {
                        "$mainUrl/sResults.php?${selectedTags.keys.joinToString("&")}&q=$query&page=$pageNumber"
                    } else {
                        "$mainUrl/sResults.php?q=$query&page=$pageNumber"
                    }
                try {
                    val request =
                        okhttp3.Request.Builder()
                            .url(url)
                            .post(
                                okhttp3.FormBody.Builder()
                                    .add("q", query)
                                    .build(),
                            )
                            .build()

                    val response = okHttpClient.newCall(request).execute()
                    val document = Jsoup.parse(response.body.string())

                    document.select("li").mapNotNull { h ->
                        SearchResponseImpl(
                            title = h.text(),
                            novelUrl = h.selectFirst("a")?.attr("href") ?: return@mapNotNull null,
                            imageUrl = h.selectFirst("img")?.attr("src") ?: "",
                        )
                    }.toHashSet()
                } catch (e: Exception) {
                    throw e
                }
            }
        }

        override val tags: Map<String, String> =
            mapOf(
                "Any" to "",
                "Action" to "action",
                "Adventure" to "adventure",
                "Celebrity" to "celebrity",
                "Comedy" to "comedy",
                "Drama" to "drama",
                "Ecchi" to "ecchi",
                "Fantasy" to "fantasy",
                "Gender Bender" to "gender-bender",
                "Harem" to "harem",
                "Historical" to "historical",
                "Horror" to "horror",
                "Josei" to "josei",
                "Martial Arts" to "martial-arts",
                "Mature" to "mature",
                "Mecha" to "mecha",
                "Mystery" to "mystery",
                "Psychological" to "psychological",
                "Romance" to "romance",
                "School Life" to "school-life",
                "Sci-fi" to "sci-fi",
                "Seinen" to "seinen",
                "Shotacon" to "shotacon",
                "Shoujo" to "shoujo",
                "Shoujo Ai" to "shoujo-ai",
                "Shounen" to "shounen",
                "Shounen Ai" to "shounen-ai",
                "Slice of Life" to "slice-of-life",
                "Sports" to "sports",
                "Supernatural" to "supernatural",
                "Tragedy" to "tragedy",
                "Wuxia" to "wuxia",
                "Xianxia" to "xianxia",
                "Xuanhuan" to "xuanhuan",
                "Yaoi" to "yaoi",
                "Yuri" to "yuri",
            )
        override val ordersBy: Map<String, String> =
            mapOf(
                "Latest" to "top-novel",
                "A-Z" to "top-novel",
                "Rating" to "top-novel",
                "Trending" to "top-novel",
                "Most Views" to "top-novel",
                "New" to "top-novel",
            )
    }
