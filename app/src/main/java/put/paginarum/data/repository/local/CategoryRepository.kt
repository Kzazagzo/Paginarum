package put.paginarum.data.repository.local

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import put.paginarum.database.AppDatabase
import put.paginarum.database.category.CategoryEntity
import put.paginarum.database.category.NovelCategoryJoin
import put.paginarum.database.category.asDomainModel
import put.paginarum.database.novel.NovelData
import put.paginarum.domain.Category
import put.paginarum.domain.CategoryName
import javax.inject.Inject

class CategoryRepository
    @Inject
    constructor(
        private val appDatabase: AppDatabase,
    ) {
        val categoriesWithNovels: Flow<List<Category<NovelData>>> =
            flow {
                val categoriesFlow = appDatabase.categoryDao.getAllCategories()
                val novelsWithoutCategoryFlow =
                    appDatabase.novelDataDao.getNovelsWithoutCategory()

                combine(
                    categoriesFlow,
                    novelsWithoutCategoryFlow,
                ) { categories, novelsWithoutCategory ->
                    val result = mutableListOf<Category<NovelData>>()

                    if (novelsWithoutCategory.isNotEmpty() || categoriesFlow.first().isEmpty()) {
                        result.add(
                            Category(
                                "Domyślna kategoria",
                                -1L,
                                novelsWithoutCategory,
                                false,
                            ),
                        )
                    }

                    categories.forEach { category ->
                        val contents =
                            getCategoryContents(category).map {
                                it.map {
                                    appDatabase.novelDataDao.getNovelByUrl(it.novelUrl)
                                }
                            }.first()

                        result.add(
                            Category(
                                category.categoryName,
                                category.id,
                                contents,
                                category.deviantState,
                            ),
                        )
                    }

                    result
                }.collect {
                    emit(it)
                }
            }

        @WorkerThread
        suspend fun changeCategoryDeviantState(
            category: String,
            deviantState: Boolean,
        ) {
            appDatabase.categoryDao.changeCategoryDeviantState(category, deviantState)
        }

        fun getAlLCategoriesNames(): Flow<List<CategoryName>> {
            return appDatabase.categoryDao.getAllCategories().map { entities ->
                entities.map { it.asDomainModel() }
            }
        }

        @WorkerThread
        suspend fun insertNovelCategoryJoin(
            novel: NovelData,
            category: CategoryName,
        ) {
            appDatabase.categoryDao.insertNovelCategoryJoin(
                NovelCategoryJoin(
                    novel.novelUrl,
                    category.categoryName,
                ),
            )
        }

        @WorkerThread
        suspend fun deleteNovelCategoryJoin(
            novel: NovelData,
            category: CategoryName,
        ) {
            appDatabase.categoryDao.deleteNovelCategoryJoin(
                NovelCategoryJoin(
                    novel.novelUrl,
                    category.categoryName,
                ),
            )
        }

        @WorkerThread
        suspend fun getCategoriesForNovel(novel: NovelData): List<NovelCategoryJoin> {
            return appDatabase.categoryDao.getCategoriesForNovel(novel.novelUrl).first()
        }

        @WorkerThread
        suspend fun updateCategory(category: CategoryName) {
            appDatabase.categoryDao.updateCategory(category.position, category.categoryName)
        }

        @WorkerThread
        suspend fun deleteCategory(category: CategoryName) {
            appDatabase.categoryDao.deleteCategory(category.position)
        }

        @WorkerThread
        suspend fun insertCategory(category: String) {
            appDatabase.categoryDao.insertCategory(
                CategoryEntity(
                    categoryName = category,
                    deviantState = false,
                ),
            )
        }

        @WorkerThread
        suspend fun swapCategoriesIds(
            category1: CategoryName,
            category2: CategoryName,
        ) {
            appDatabase.categoryDao.swapCategoriesIds(category1.position, category2.position)
        }

        @WorkerThread
        private fun getCategoryContents(category: CategoryEntity): Flow<List<NovelCategoryJoin>> {
            return appDatabase.categoryDao.getCategoryContents(category.categoryName)
        }
    }
