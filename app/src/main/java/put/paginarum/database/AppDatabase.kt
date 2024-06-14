package put.paginarum.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import put.paginarum.database.al.AlDao
import put.paginarum.database.al.AlTrackerEntity
import put.paginarum.database.category.CategoryDao
import put.paginarum.database.category.CategoryEntity
import put.paginarum.database.category.NovelCategoryJoin
import put.paginarum.database.novel.ChapterData
import put.paginarum.database.novel.ChapterTextEntity
import put.paginarum.database.novel.Converters
import put.paginarum.database.novel.NovelData
import put.paginarum.database.novel.NovelDataDao
import put.paginarum.database.settings.SettingsConverter
import put.paginarum.database.settings.SettingsDao
import put.paginarum.database.settings.SettingsEntity

@Database(
    entities = [
        NovelData::class,
        ChapterData::class,
        CategoryEntity::class,
        NovelCategoryJoin::class,
        SettingsEntity::class,
        ChapterTextEntity::class,
        CachedPage::class,
        AlTrackerEntity::class,
    ],
    version = 2143, // :<<<
    exportSchema = false,
)
@TypeConverters(Converters::class, CachedPage.CacheConverter::class, SettingsConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val novelDataDao: NovelDataDao
    abstract val categoryDao: CategoryDao
    abstract val settingsDao: SettingsDao
    abstract val cacheDao: CacheDao
    abstract val alTrackerDao: AlDao
}
