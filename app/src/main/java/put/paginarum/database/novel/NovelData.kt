package put.paginarum.database.novel

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import put.paginarum.data.api.providerApi.SearchResponse

@Entity(tableName = "novels")
@TypeConverters(Converters::class)
@Parcelize
data class NovelData(
    @PrimaryKey override val novelUrl: String,
    val provider: String,
    override val title: String?,
    val description: String?,
    override val imageUrl: String?,
    val rating: String?,
    val author: String?,
    val genres: List<String>,
    val type: String?,
    val tags: List<String>,
    val status: String?,
    val chapters: @RawValue List<ChapterData>,
) : Parcelable, SearchResponse

// @Entity(tableName = "novels")
// data class NovelEntity(
//
// ) {
//    fun fileExist(): Boolean {
//        val file = File(path)
//        return file.exists()
//    }
//
//    fun getFileSize(): String {
//        val file = File(path)
//        var bytes = file.length()
//        if (-1000 < bytes && bytes < 1000) {
//            return "$bytes B"
//        }
//        val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
//        while (bytes <= -999950 || bytes >= 999950) {
//            bytes /= 1000
//            ci.next()
//        }
//        return java.lang.String.format(Locale.US, "%.1f %cB", bytes / 1000.0, ci.current())
//    }
//
//    fun deleteFile(): Boolean {
//        val file = File(path)
//        return try {
//            file.delete()
//        } catch (exc: IOException) {
//            false
//        }
//    }
// }
//
// @Entity(tableName = "reader")
// data class ReaderEntity(
//    @PrimaryKey
//    val novelId: Int,
//    val lastChapterIndex: Int, //to na pewno
//    val lastChapterOffset: Int, //to nwm
// ) {
//    fun getProgressPercent(totalChapters: Int) =
//        String.format("%.2f", ((lastChapterIndex + 1).toFloat() / totalChapters.toFloat()) * 100f)
// }
