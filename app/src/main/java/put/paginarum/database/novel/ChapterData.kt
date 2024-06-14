package put.paginarum.database.novel

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize
import put.paginarum.domain.ChapterElement
import put.paginarum.domain.ChapterText

@Entity(tableName = "chapters", indices = [Index(value = ["novelUrl"])])
@Parcelize
data class ChapterData(
    val novelUrl: String,
    @PrimaryKey val chapterUrl: String,
    val name: String,
    val releaseDate: String?,
) : Parcelable

@TypeConverters(Converters::class)
@Entity(tableName = "chapter_texts")
data class ChapterTextEntity(
    val novelUrl: String,
    @PrimaryKey val chapterUrl: String,
    val elements: List<ChapterElement>,
)

@JvmName("ChapterTextEntityasDomainModel")
fun List<ChapterTextEntity>.asDomainModel(): List<ChapterText> {
    return map {
        ChapterText(it.elements)
    }
}
