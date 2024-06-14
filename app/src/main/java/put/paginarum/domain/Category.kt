package put.paginarum.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Category<T>(
    val categoryName: String,
    val position: Long,
    val elementsInCategory: @RawValue List<T>?,
    val isDeviant: Boolean,
) : Parcelable

@Parcelize
data class CategoryName(
    var categoryName: String,
    val position: Long,
    var isDeviant: Boolean,
) : Parcelable
