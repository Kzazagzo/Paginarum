package put.paginarum.database.novel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import put.paginarum.database.category.CategoryEntity
import put.paginarum.database.category.NovelCategoryJoin

@Dao
interface NovelDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun novelInsert(novelData: NovelData)

    @Query("DELETE FROM novels WHERE novelUrl = :novelUrl")
    suspend fun novelDelete(novelUrl: String)

    @Query("SELECT * FROM novels WHERE novelUrl = :novelUrl")
    suspend fun novelGet(novelUrl: String): NovelData?
    @Query("SELECT * FROM novels")
    fun getAllNovels(): Flow<List<NovelData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun chapterListInsert(chaptersData: List<ChapterData>)

    @Query("DELETE FROM chapters WHERE novelUrl = :novelUrl")
    suspend fun chapterListDelete(novelUrl: String)

    @Query("SELECT title FROM novels")
    fun getNovelNames(): Flow<List<String>>

    @Query("SELECT * FROM novels WHERE novelUrl = :novelUrl")
    suspend fun getNovelByUrl(novelUrl: String): NovelData


    @Query(
        "SELECT * FROM novels n WHERE NOT " +
                "EXISTS (SELECT * FROM novel_category_join ncj WHERE " +
                "ncj.novelUrl = n.novelUrl)",
    )
    fun getNovelsWithoutCategory(): Flow<List<NovelData>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategoryIfNotExists(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNovelCategoryJoin(join: NovelCategoryJoin)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun chapterTextInsert(chapterTextEntity: ChapterTextEntity)

    @Query("DELETE FROM chapter_texts WHERE chapterUrl=:chapterUrl")
    suspend fun chapterTextDelete(chapterUrl: String)
    @Query("SELECT * FROM chapter_texts WHERE novelUrl=:novelUrl")
    suspend fun chapterTextGetForNovel(novelUrl: String): List<ChapterTextEntity>

    @Query("SELECT * FROM chapter_texts WHERE chapterUrl=:chapterUrl")
    suspend fun chapterTextGet(chapterUrl: String): ChapterTextEntity?
}
