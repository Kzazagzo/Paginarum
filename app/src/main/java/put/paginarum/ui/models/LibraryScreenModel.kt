package put.paginarum.ui.models

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import put.paginarum.data.api.providerApi.ProviderApi
import put.paginarum.data.repository.local.CategoryRepository
import put.paginarum.data.repository.local.NovelRepository
import put.paginarum.database.novel.ChapterTextEntity
import put.paginarum.database.novel.NovelData
import put.paginarum.domain.Category
import put.paginarum.domain.ChapterText
import put.paginarum.util.DataStatus
import javax.inject.Inject

class LibraryScreenModel
    @Inject
    constructor(
        private val novelRepository: NovelRepository,
        private val categoryRepository: CategoryRepository,
        val providers: Map<String, @JvmSuppressWildcards ProviderApi>,
    ) : ScreenModel {
        private val _novelsInCategories =
            MutableStateFlow<DataStatus<List<Category<NovelData>>>>(DataStatus.Loading)
        val novelsInCategories: StateFlow<DataStatus<List<Category<NovelData>>>> =
            _novelsInCategories.asStateFlow()

        private val _searchText = MutableStateFlow("")

        init {
            novelLoadLibrary()
        }

        private fun novelLoadLibrary() {
            screenModelScope.launch {
                categoryRepository.categoriesWithNovels.collect { categoryData ->
                    var index = 0
                    val newMap =
                        categoryData.map { category ->
                            val novelsList =
                                try {
                                    category.elementsInCategory?.map {
                                        it
                                    } ?: emptyList()
                                } catch (e: Exception) {
                                    emptyList()
                                }
                            Category(
                                category.categoryName,
                                position = (index).toLong(),
                                novelsList,
                                category.isDeviant,
                            ).also {
                                index++
                            }
                        }
                    _novelsInCategories.value = DataStatus.Success(newMap)
                }
            }
        }

        fun novelInLibrary(novelUrl: String): Boolean {
            // TODO? Co tutaj?
            val dataStatus =
                novelsInCategories.value as? DataStatus.Success<List<Category<NovelData>>>
                    ?: return false

            return dataStatus.data.any { category ->
                category.elementsInCategory?.any { novel ->
                    novel.novelUrl == novelUrl
                } == true
            }
        }

        fun deleteNovelFromLibrary(url: String) {
            screenModelScope.launch {
                novelRepository.novelDelete(url)
                novelRepository.chapterListDelete(url)
            }
        }

        fun addNovelToLibrary(novel: NovelData) {
            screenModelScope.launch {
                novelRepository.novelInsert(novel)
                novelRepository.chapterListInsert(novel.chapters)
            }
        }

        fun downloadChapterText(
            provider: ProviderApi,
            novelUrl: String,
            chapterUrl: String,
        ) {
            screenModelScope.launch {
                val chapterTextStatus =
                    try {
                        DataStatus.Success(provider.loadChapterText(chapterUrl))
                    } catch (e: Exception) {
                        DataStatus.Error(e.message)
                    }
                val chapterText = (chapterTextStatus as DataStatus.Success<ChapterText>).data
                novelRepository.chapterTextInsert(chapterUrl, novelUrl, chapterText)
            }
        }

        fun chapterTextDelete(chapterUrl: String) {
            screenModelScope.launch {
                novelRepository.chapterTextDelete(chapterUrl)
            }
        }

        suspend fun chapterTextGetForNovel(novelUrl: String): List<ChapterTextEntity> {
            return novelRepository.chapterTextGetForNovel(novelUrl)
        }

        suspend fun chapterTextGet(chapterUrl: String): ChapterTextEntity? {
            return novelRepository.chapterTextGet(chapterUrl)
        }

        val searchText: StateFlow<String> = _searchText.asStateFlow()

        fun onSearchTextChange(text: String) {
            _searchText.value = text
        }
    }
