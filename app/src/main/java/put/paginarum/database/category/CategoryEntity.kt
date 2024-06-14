package put.paginarum.database.category

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import put.paginarum.database.novel.NovelData
import put.paginarum.domain.CategoryName

@Entity(tableName = "categories", indices = [Index(value = ["categoryName"], unique = true)])
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryName: String,
    val deviantState: Boolean,
)

@Entity(
    tableName = "novel_category_join",
    primaryKeys = ["novelUrl", "categoryName"],
    foreignKeys = [
        ForeignKey(
            entity = NovelData::class,
            parentColumns = ["novelUrl"],
            childColumns = ["novelUrl"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["categoryName"],
            childColumns = ["categoryName"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["novelUrl", "categoryName"], unique = true),
        Index(value = ["categoryName"]),
    ],
)
data class NovelCategoryJoin(
    val novelUrl: String,
    val categoryName: String,
)

// fun <T> SzlakCategoryJoin.asDomainModel(elements: List<T>): Category<T> {
//    return Category(
//        categoryName = this.categoryName,
//        elementsInCategory = elements,
//    )
// }

fun CategoryEntity.asDomainModel(): CategoryName {
    return CategoryName(
        categoryName = this.categoryName,
        position = this.id,
        deviantState,
    )
}
