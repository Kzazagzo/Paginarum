package put.paginarum.data.repository.network

import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class DataConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *>? {
        return try {
            val dataType = TypeToken.getParameterized(Data::class.java, type).type
            val converter: Converter<ResponseBody, Data<Any>>? = retrofit.nextResponseBodyConverter(this, dataType, annotations)
            DataConverter(converter)
        } catch (e: Exception) {
            null
        }
    }
}
