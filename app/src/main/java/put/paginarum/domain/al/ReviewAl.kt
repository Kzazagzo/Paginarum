package put.paginarum.domain.al

data class ReviewAl(
    val reviewer: UserAl,
    val summary: String,
    val reviweBody: String,
    val ratingByRewier: String,
    val reviewScore: String,
)
