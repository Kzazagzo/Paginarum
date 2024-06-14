package put.paginarum.network.al

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GraphQLService {
    @Headers("Content-Type: application/json")
    @POST("graphql")
    fun getData(
        @Body body: RequestBody,
    ): Call<ResponseBody>
}
