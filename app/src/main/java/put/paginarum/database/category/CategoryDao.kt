package put.paginarum.database.category

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("UPDATE categories SET categoryName = :categoryName WHERE id = :categoryId")
    suspend fun updateCategory(
        categoryId: Long,
        categoryName: String,
    )

    @Query("SELECT * FROM novel_category_join WHERE novelUrl = :novelUrl")
    fun getCategoriesForNovel(novelUrl: String): Flow<List<NovelCategoryJoin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNovelCategoryJoin(novelCategoryJoin: NovelCategoryJoin)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: Long)

    @Query("UPDATE categories SET deviantState = :deviantState WHERE categoryName=:categoryName")
    suspend fun changeCategoryDeviantState(
        categoryName: String,
        deviantState: Boolean,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(categoryEntity: CategoryEntity)

    @Query("SELECT * FROM novel_category_join WHERE categoryName = :categoryName")
    fun getCategoryContents(categoryName: String): Flow<List<NovelCategoryJoin>>

    @Query("UPDATE categories SET id = :newPosition WHERE id = :oldPosition")
    suspend fun updateCategoryPosition(
        oldPosition: Long,
        newPosition: Long,
    )

    @Transaction
    suspend fun swapCategoriesIds(
        categoryId1: Long,
        categoryId2: Long,
    ) {
        val tempPosition: Long = -1L
        updateCategoryPosition(categoryId1, tempPosition)
        updateCategoryPosition(categoryId2, categoryId1)
        updateCategoryPosition(tempPosition, categoryId2)
    }

    @Delete
    suspend fun deleteNovelCategoryJoin(novelCategoryJoin: NovelCategoryJoin)

    @Query("UPDATE categories SET id = :newId WHERE id = :oldId")
    suspend fun updateCategoryId(
        oldId: Long,
        newId: Long,
    )
}
