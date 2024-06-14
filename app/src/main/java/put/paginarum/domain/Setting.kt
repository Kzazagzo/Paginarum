package put.paginarum.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import put.paginarum.database.settings.SettingsConverter
import put.paginarum.database.settings.SettingsEntity

@Parcelize
data class Setting<T>(
    val settingName: String,
    val settingCategory: SettingCategory,
    val settingValue: @RawValue T,
) : Parcelable

@Parcelize
enum class SettingCategory : Parcelable {
    General,
    Tracking,
    Security,
    Library,
    Hidden, // MUSI BYĆ OSTATNI
}

fun Setting<*>.asDatabaseModel(): SettingsEntity {
    val converters = SettingsConverter()
    return SettingsEntity(
        settingName = this.settingName,
        settingValue = converters.fromSettingValue(this.settingValue),
        typeDetails = this.settingValue!!::class.java.toString(),
        settingCategory = this.settingCategory,
    )
}
