package put.paginarum.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
data class ChapterText(
    val elements: List<ChapterElement>,
) : Parcelable

@Parcelize
@Serializable
@Polymorphic
sealed class ChapterElement : Parcelable {
    @Parcelize
    @Serializable
    @SerialName("txt")
    data class Text(val content: String) : ChapterElement()

    @Parcelize
    @Serializable
    @SerialName("sep")
    data class Separator(val separatorType: SeparatorType) : ChapterElement()
}

@Serializable
enum class SeparatorType {
    NONE,
    SMALL,
    LARGE,
}
