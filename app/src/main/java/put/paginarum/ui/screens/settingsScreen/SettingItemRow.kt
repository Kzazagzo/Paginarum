package put.paginarum.ui.screens.settingsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import put.paginarum.domain.Setting

@Composable
fun SettingItemRow(setting: Setting<*>) {
    var value by remember { mutableStateOf(setting.settingValue) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = setting.settingName)
        when (setting::class.java) {
            Int::class.java -> {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = value.toString(),
                    onValueChange = { value = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            String::class.java -> {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = value as String,
                    onValueChange = { value = it },
                )
            }
            Boolean::class.java -> {
                Switch(checked = value as Boolean, onCheckedChange = { value = it })
            }
        }
    }
}
