package put.paginarum.data.api.providers

import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import put.paginarum.data.api.providerApi.ProviderApi
import put.paginarum.data.api.providerApi.SearchResponse
import put.paginarum.data.api.providerApi.SearchResponseImpl
import put.paginarum.data.repository.local.CacheRepository
import put.paginarum.database.novel.ChapterData
import put.paginarum.database.novel.NovelData
import put.paginarum.domain.ChapterElement
import put.paginarum.domain.ChapterText
import put.paginarum.domain.NovelFilters
import put.paginarum.domain.SeparatorType
import put.paginarum.ui.components.common.SelectionState
import javax.inject.Inject

class BoxNovelProvider
    @Inject
    constructor(
        var cacheRepository: CacheRepository,
    ) :
    ProviderApi() {
        override val name = "Box Novel"
        override val mainUrl = "https://boxnovel.com"
        override val iconUrl = "https://boxnovel.com/wp-content/uploads/2018/04/BoxNovelNEW.png"
        override val supportAdditionalOrdering = false
        override val usesCloudFlareKiller = false

        @WorkerThread
        override suspend fun loadMainPage(
            pageNumber: Int,
            selectedFilters: NovelFilters,
        ): HashSet<SearchResponse> {
            return withContext(Dispatchers.IO) {
                val selectedTags = selectedFilters.tag.filter { it.value == SelectionState.SELECTED }
                var url =
                    if (selectedTags.isNotEmpty()) {
                        "$mainUrl/manga-genre/${tags[selectedTags.keys.first()]}/"
                    } else {
                        "$mainUrl/novel/"
                    }
                url += "page/$pageNumber"
                if (selectedFilters.orderBy != "") {
                    url += "?m_orderby=${ordersBy[selectedFilters.orderBy]}"
                }
                val returnList = HashSet<SearchResponse>()

                val document: Document = Jsoup.connect(url).get()

                val elements: List<Element> = document.select("div[id^=manga-item-]")

                for (element in elements) {
                    val linkElement: Element? = element.selectFirst("a[href]")
                    val titleElement: Element? = element.selectFirst("a[title]")
                    val imageElement: Element? = element.selectFirst("img")

                    if (linkElement != null && titleElement != null && imageElement != null) {
                        val link = linkElement.attr("href")
                        val title = titleElement.attr("title")
                        val image = imageElement.attr("data-src")

                        returnList.add(SearchResponseImpl(title, link, image))
                    }
                }

                returnList
            }
        }

        override suspend fun loadNovelData(novelUrl: String): NovelData {
            return withContext(Dispatchers.IO) {
                val document: Document = downloadHtml(novelUrl, cacheRepository)

                val title = document.selectFirst("div.post-title > h1")?.text()
                val description = document.selectFirst("div.j_synopsis > p.c_000")?.text()
                val imageUrl = document.selectFirst("div.summary_image > a > img")?.attr("data-src")
                val rating = document.selectFirst("div.post-total-rating > span.score")?.text()
                val author =
                    document.selectFirst(
                        "div.summary-heading:contains(Author(s)) + div.summary-content > div.author-content > a",
                    )?.text()

                val genres =
                    document.select("div.summary-heading:contains(Genre(s)) + div.summary-content > div.genres-content > a")
                        .map { it.text() }

                val type = document.selectFirst("div.summary-heading:contains(Type) + div.summary-content")?.text()

                val tags =
                    document.select("div.summary-heading:contains(Tag(s)) + div.summary-content > div.tags-content > a")
                        .map { it.text() }
                val status =
                    document.selectFirst(
                        "div.post-status > div.post-content_item > div" +
                            ".summary-content",
                    )?.text()

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
            }
        }

        override suspend fun loadNovelChapters(novelUrl: String): List<ChapterData> {
            return withContext(Dispatchers.IO) {
                val response: Document =
                    Jsoup.connect("${novelUrl}ajax/chapters/")
                        .method(Connection.Method.POST)
                        .execute()
                        .parse()

                response.select("ul.main > li.wp-manga-chapter")
                    .map { element ->
                        val rawTitle = element.selectFirst("a")?.text().orEmpty()
                        val chapterTitle = rawTitle.replace(Regex("Chapter \\d+ - "), "Chapter ")
                        val chapterUrl = element.selectFirst("a")?.attr("href").orEmpty()
                        val chapterReleaseDate = element.selectFirst("span.chapter-release-date > i")?.text()
                        ChapterData(name = chapterTitle, chapterUrl = chapterUrl, releaseDate = chapterReleaseDate, novelUrl = novelUrl)
                    }
            }
        }

        override suspend fun loadChapterText(chapterUrl: String): ChapterText {
            return withContext(Dispatchers.IO) {
                val document: Document = downloadHtml(chapterUrl, cacheRepository)
                val elements = document.select("div.text-left").first()?.children() ?: emptyList()

                val contentList = mutableListOf<ChapterElement>()

                for (element in elements) {
                    when (element.tagName()) {
                        "p" -> {
                            contentList.add(ChapterElement.Text(element.text()))
                        }
                        "div" -> {
                            if (element.text().isNotBlank()) {
                                contentList.add(ChapterElement.Separator(SeparatorType.LARGE))
                            }
                        }
                    }
                }

                ChapterText(contentList)
            }
        }

        override suspend fun search(
            query: String,
            pageNumber: Int,
            selectedFilters: NovelFilters,
        ): HashSet<SearchResponse> {
            return withContext(Dispatchers.IO) {
                var url = "$mainUrl/page/$pageNumber/?s=$query&post_type=wp-manga"
                if (selectedFilters.orderBy != "") {
                    url += "&m_orderby=${ordersBy[selectedFilters.orderBy]}"
                }
                val returnList = HashSet<SearchResponse>()

                val document: Document = Jsoup.connect(url).get()

                val elements: List<Element> = document.select(".row.c-tabs-item__content")

                for (element in elements) {
                    val titleElement: Element? = element.selectFirst("h3.h4 a")
                    val linkElement: Element? = element.selectFirst("a[href]")
                    val imageElement: Element? = element.selectFirst("img")

                    if (linkElement != null && titleElement != null && imageElement != null) {
                        val link = linkElement.attr("href")
                        val title = titleElement.text()
                        val image = imageElement.attr("data-src")

                        returnList.add(SearchResponseImpl(title, link, image))
                    }
                }

                returnList
            }
        }

        override val ordersBy: Map<String, String> =
            mapOf(
                "Latest" to "latest",
                "A-Z" to "alphabet",
                "Rating" to "rating",
                "Trending" to "trending",
                "Most Views" to "views",
                "New" to "new-manga",
            )

        override val tags: Map<String, String> =
            mapOf(
                "Any" to "",
                "Action" to "action",
                "Adventure" to "adventure",
                "Comedy" to "comedy",
                "Drama" to "drama",
                "Eastern" to "eastern",
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
                "Shoujo" to "shoujo",
                "Shounen" to "shounen",
                "Slice of Life" to "slice-of-life",
                "Smut" to "smut",
                "Sports" to "sports",
                "Supernatural" to "supernatural",
                "Tragedy" to "tragedy",
                "Wuxia" to "wuxia",
                "Xianxia" to "xianxia",
                "Xuanhuan" to "xuanhuan",
                "Yaoi" to "yaoi",
            )
    }
