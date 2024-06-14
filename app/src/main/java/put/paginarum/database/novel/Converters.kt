package put.paginarum.database.novel
import androidx.room.TypeConverter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import put.paginarum.domain.ChapterElement
import put.paginarum.domain.ChapterText
import put.paginarum.domain.SeparatorType

class Converters {
    private val json = Json

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromChapterList(value: List<ChapterData>?): String { //eeeeeeee ważne że jest jedna klasa xD
        return ""
    }


    @TypeConverter
    fun toChapterList(value: String?): List<ChapterData> {  //yup yup a potem będzie z jednej nowelki
        return listOf()
    }


    private val jsonText = Json {
        classDiscriminator = "type"
    }
    @TypeConverter
    fun fromChapterText(value: List<ChapterElement>): String {
        return jsonText.encodeToString(value)
    }

    @TypeConverter
    fun toChapterText(value: String): List<ChapterElement> {
        return jsonText.decodeFromString(value)
    }



}

