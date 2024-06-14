package put.paginarum.domain.al

import com.google.gson.annotations.SerializedName

data class UserAl(
    val id: Int,
    val name: String,
    @SerializedName("avatar") val avatar: Avatar,
    @SerializedName("bannerImage") val bannerImage: String,
    val about: String?,
) {
    data class Avatar(
        val large: String,
    )
}
