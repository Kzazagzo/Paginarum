package put.paginarum.database.settings

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize
import put.paginarum.domain.Setting
import put.paginarum.domain.SettingCategory
import java.lang.reflect.Type

@Entity(tableName = "settings")
@Parcelize
data class SettingsEntity(
    @PrimaryKey val settingName: String,
    val settingCategory: SettingCategory,
    val settingValue: String,
    val typeDetails: String,
) : Parcelable

class SettingsConverter {
    private val gson: Gson = Gson()

    @TypeConverter
    fun fromSettingValue(settingValue: Any?): String {
        return gson.toJson(settingValue)
    }

    fun toSettingValue(
        value: String,
        type: String,
    ): Any? {
        return when (type) {
            Int::class.java.toString() -> gson.fromJson(value, Int::class.java)
            Integer::class.java.toString() -> gson.fromJson(value, Integer::class.java)
            // Bo chyba stringi są zwracane w ""
            String::class.java.toString() -> gson.fromJson(value, String::class.java).replace(Regex("^\"|\"$"), "")
            List::class.java.toString() + "<" + String::class.java.toString() + ">" -> {
                val listType: Type = object : TypeToken<List<String>>() {}.type
                gson.fromJson<List<String>>(value, listType)
            }
            IntRange::class.java.toString() -> gson.fromJson(value, IntRange::class.java)
            else -> throw IllegalArgumentException("Unsupported setting value type")
        }
    }

    fun getClassFromTypeDetails(typeDetails: String): Class<*> {
        return Class.forName(typeDetails)
    }

    fun getTypeDetailsFromClass(clazz: Class<*>): String {
        return clazz.name
    }
}

fun SettingsEntity.asDomainModel(): Setting<Any> {
    val converters = SettingsConverter()
    val typeDetails = this.typeDetails
    return Setting(
        settingName = this.settingName,
        settingValue = converters.toSettingValue(this.settingValue, typeDetails)!!,
        settingCategory = this.settingCategory,
    )
}
