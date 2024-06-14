package put.paginarum.data.repository.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import put.paginarum.data.api.providerApi.AlSearchResponseImpl
import put.paginarum.domain.al.ReviewAl
import put.paginarum.domain.al.UserAl
import put.paginarum.network.al.GraphQLService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AniListRepository
    @Inject
    constructor(
        private val api: GraphQLService,
    ) {
        private fun createRequestBody(
            query: String,
            variables: Map<String, Any>,
        ): RequestBody {
            val requestBodyJson =
                """
                {
                    "query": "${query.trimIndent()}",
                    "variables": ${Gson().toJson(variables)}
                }
                """.trimIndent()
            return requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull())
        }

        private suspend inline fun <reified T> executeApiCall(
            query: String,
            variables: Map<String, Any> = emptyMap(),
        ): T? {
            val requestBody = createRequestBody(query, variables)
            val apiCall = api.getData(requestBody)
            return suspendCoroutine { continuation ->
                apiCall.enqueue(
                    object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>,
                        ) {
                            if (response.isSuccessful) {
                                if (T::class != Any::class) {
                                    val responseBody =
                                        response.body() // response.body().let się psuje
                                    if (responseBody != null) {
                                        val responseBodyString = responseBody.string()
                                        val jsonObject =
                                            JsonParser.parseString(responseBodyString).asJsonObject
                                        val dataObject = jsonObject.getAsJsonObject("data")

                                        val apiResponse =
                                            when {
                                                dataObject.has("Viewer") -> {
                                                    val viewerObject =
                                                        dataObject.getAsJsonObject("Viewer")
                                                    Gson().fromJson(viewerObject, T::class.java)
                                                }

                                                dataObject.has("Page") -> {
                                                    val pageObject =
                                                        dataObject.getAsJsonObject("Page")
                                                    Gson().fromJson(pageObject, T::class.java)
                                                }

                                                dataObject.has("MediaList") ->
                                                    AniListRepository.AlNovelStatus.valueOf(
                                                        dataObject.getAsJsonObject("MediaList").get("status").asString,
                                                    ) as T

                                                else -> throw IllegalStateException("Unexpected response format")
                                            }

                                        continuation.resume(apiResponse)
                                    }
                                } else {
                                    continuation.resume(null)
                                }
                            } else {
                                if (response.code() == 401) {
                                    continuation.resumeWithException(Throwable("Unauthorized"))
                                } else {
                                    continuation.resumeWithException(Throwable("API Error: ${response.code()}"))
                                }
                            }
                        }

                        override fun onFailure(
                            call: Call<ResponseBody>,
                            t: Throwable,
                        ) {
                            continuation.resumeWithException(t)
                        }
                    },
                )
            }
        }

        suspend fun getUserData(): UserAl {
            val query = """query { Viewer { id name avatar { large } bannerImage about } }"""
            return executeApiCall(query)!!
        }

        suspend fun getNovelData(
            page: Int,
            search: String,
        ): List<AlSearchResponseImpl> {
            val format = "NOVEL"
            val perPage = 100

            val query =
                """
                query (${"$"}page: Int, ${"$"}perPage: Int, ${"$"}search: String, ${"$"}format: MediaFormat) {Page(page: ${"$"}page, perPage: ${"$"}perPage) { media(search: ${"$"}search, format: ${"$"}format) { id title { english } siteUrl coverImage { large } } } }
                """.trimIndent()

            val variables =
                mapOf(
                    "page" to page,
                    "perPage" to perPage,
                    "search" to search,
                    "format" to format,
                )

            val response = executeApiCall<JsonObject>(query, variables)
            return parseNovelData(response!!)
        }

        suspend fun getNovelTrackerStatus(novelId: String): AlNovelStatus? {
            val query =
                """
                query (${'$'}mediaId: Int!, ${'$'}userId: Int!) { MediaList(mediaId: ${'$'}mediaId, userId: ${'$'}userId) { status } }
                """.trimIndent()
            val variables =
                mapOf(
                    "mediaId" to novelId,
                    "userId" to getUserData().id,
                )
            return executeApiCall<AlNovelStatus>(query, variables)
        }

        enum class AlNovelStatus {
            CURRENT,
            PLANNING,
            COMPLETED,
            DROPPED,
            PAUSED,
            REPEATING,
        }

        suspend fun updateUserNovelStatus(
            novelId: String,
            status: AlNovelStatus,
        ) {
            val query =
                """
                mutation (${"$"}mediaId: Int, ${"$"}status: MediaListStatus) { SaveMediaListEntry (mediaId: ${"$"}mediaId, status: ${"$"}status) { id status } }
                """.trimIndent()

            val variables =
                mapOf(
                    "mediaId" to novelId,
                    "status" to status.toString(),
                )

            executeApiCall<Any>(query, variables)
        }

        suspend fun getNovelReviews(
            page: Int,
            mediaId: String,
        ): List<ReviewAl> {
            val perPage = 10

            val query =
                """
                query (${"$"}mediaId: Int, ${"$"}perPage: Int, ${"$"}page: Int) { Page(page: ${"$"}page, perPage: ${"$"}perPage) { reviews(mediaId: ${"$"}mediaId) { summary body rating userRating score user { id name avatar { large } bannerImage about } } } }
                """.trimIndent()

            val variables =
                mapOf(
                    "mediaId" to mediaId,
                    "page" to page,
                    "perPage" to perPage,
                )

            val response = executeApiCall<JsonObject>(query, variables)!!
            return parseReview(response)
        }

        private fun parseNovelData(response: JsonObject): List<AlSearchResponseImpl> {
            return if (response.has("media") && response.get("media").isJsonArray) {
                val mediaArray = response.getAsJsonArray("media")

                mediaArray.mapNotNull { mediaElement ->
                    val mediaObject = mediaElement.asJsonObject
                    val titleElement = mediaObject.getAsJsonObject("title").get("english")
                    if (titleElement == null || titleElement.isJsonNull) {
                        null
                    } else {
                        val id = mediaObject.get("id")?.asString ?: ""
                        val title = titleElement.asString
                        val novelUrl = mediaObject.get("siteUrl").asString
                        val imageUrl = mediaObject.getAsJsonObject("coverImage").get("large").asString

                        AlSearchResponseImpl(id, title, novelUrl, imageUrl)
                    }
                }
            } else {
                emptyList()
            }
        }

        private fun parseReview(response: JsonObject): List<ReviewAl> {
            return if (response.has(
                    "data",
                ) &&
                response.getAsJsonObject(
                    "data",
                ).has("Page") && response.getAsJsonObject("data").getAsJsonObject("Page").has("reviews")
            ) {
                val reviewsArray = response.getAsJsonObject("data").getAsJsonObject("Page").getAsJsonArray("reviews")

                reviewsArray.map { reviewElement ->
                    val reviewObject = reviewElement.asJsonObject
                    val summary = reviewObject.get("summary")?.asString ?: "No summary"
                    val body = reviewObject.get("body")?.asString ?: "No body"
                    val rating = reviewObject.get("rating")?.asInt ?: 0
                    val score = reviewObject.get("score")?.asInt ?: 0

                    val userObject = reviewObject.getAsJsonObject("user")
                    val userId = userObject.get("id").asInt
                    val userName = userObject.get("name").asString
                    val userAvatar = userObject.getAsJsonObject("avatar").get("large").asString
                    val userBannerImage = userObject.get("bannerImage")?.asString ?: ""
                    val userAbout = userObject.get("about")?.asString ?: ""

                    val reviewer =
                        UserAl(
                            id = userId,
                            name = userName,
                            avatar = UserAl.Avatar(large = userAvatar),
                            bannerImage = userBannerImage,
                            about = userAbout,
                        )

                    ReviewAl(
                        reviewer = reviewer,
                        summary = summary,
                        reviweBody = body,
                        ratingByRewier = rating.toString(),
                        reviewScore = score.toString(),
                    )
                }
            } else {
                emptyList()
            }
        }
    }
