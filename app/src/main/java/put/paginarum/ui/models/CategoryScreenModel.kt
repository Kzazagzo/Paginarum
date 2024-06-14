package put.paginarum.ui.models

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import put.paginarum.data.repository.local.CategoryRepository
import put.paginarum.database.novel.NovelData
import put.paginarum.domain.CategoryName
import javax.inject.Inject

class CategoryScreenModel
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
    ) : ScreenModel {
        private val _categoriesNames = MutableStateFlow<List<CategoryName>>(emptyList())
        val categoriesNames: StateFlow<List<CategoryName>> = _categoriesNames.asStateFlow()

        init {
            screenModelScope.launch {
                categoryRepository.getAlLCategoriesNames().collect {
                    _categoriesNames.value = it
                }
            }
        }

        suspend fun getMapOfBelongingCategories(novel: NovelData): MutableMap<CategoryName, Boolean> {
            val belongingCategories = categoryRepository.getCategoriesForNovel(novel)
            val belongingCategoryNames = belongingCategories.map { it.categoryName }
            return categoriesNames.value.associateWith {
                it.categoryName in
                    belongingCategoryNames
            }.toMutableMap()
        }

        fun addNewCategory(categoryName: String) =
            screenModelScope.launch {
                categoryRepository.insertCategory(categoryName)
            }

        fun insertNovelCategoryJoin(
            novel: NovelData,
            categoryName: CategoryName,
        ) = screenModelScope.launch {
            categoryRepository.insertNovelCategoryJoin(novel, categoryName)
        }

        fun deleteNovelCategoryJoin(
            novel: NovelData,
            categoryName: CategoryName,
        ) = screenModelScope.launch {
            categoryRepository.deleteNovelCategoryJoin(novel, categoryName)
        }

        fun editCategoryName(categoryName: CategoryName) {
            screenModelScope.launch {
                categoryRepository.updateCategory(categoryName)
            }
        }

        fun swapCategoriesIds(
            categoryName1: CategoryName,
            categoryName2: CategoryName,
        ) = screenModelScope.launch {
            categoryRepository.swapCategoriesIds(categoryName1, categoryName2)
        }

        fun deleteCategory(categoryName: CategoryName) {
            screenModelScope.launch {
                categoryRepository.deleteCategory(categoryName)
            }
        }

        fun changeCategoryDeviantState(
            categoryName: String,
            deviantState: Boolean,
        ) {
            screenModelScope.launch {
                categoryRepository.changeCategoryDeviantState(categoryName, deviantState)
            }
        }
    }
