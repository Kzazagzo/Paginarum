package put.paginarum.ui.screens.settingsScreen

import android.os.Looper
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.hilt.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.put.szlaki.ui.components.screen.settingsScreen.SettingItem
import pl.put.szlaki.ui.components.screen.settingsScreen.SettingOptions
import put.paginarum.R
import put.paginarum.ui.models.CategoryScreenModel
import put.paginarum.ui.models.PrivilegeScreenModel
import put.paginarum.ui.screens.PinIndicator
import put.paginarum.ui.screens.PinKeyboard

class SetPasswordSetting(
    private val screenModel: CategoryScreenModel,
) : SettingItem("passwordManager") {
    override val options: SettingOptions
        @Composable get() {
            val categories = screenModel.categoriesNames.collectAsState()
            return SettingOptions(
                title = "Zarządzaj hasłem aplikacji",
                description = "",
                icon = R.drawable.baseline_password_24,
            )
        }

    @Composable
    override fun Content() {
        var firstPassword by remember { mutableStateOf<String?>(null) }
        var secondPassword by remember { mutableStateOf<String?>(null) }
        var passwordsMatch by remember { mutableStateOf<Boolean?>(null) }
        val context = LocalContext.current

        LaunchedEffect(Unit) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Toast.makeText(context, "Wprowadź nowe hasło", Toast.LENGTH_SHORT).show()
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (firstPassword == null) {
                PasswordSetUi { pin ->
                    firstPassword = pin
                }
            } else if (secondPassword == null) {
                PasswordSetUi { pin ->
                    secondPassword = pin
                    passwordsMatch = firstPassword == secondPassword

                    if (passwordsMatch == false) {
                        Toast.makeText(context, "Hasła się nie zgadzają. Spróbuj ponownie.", Toast.LENGTH_SHORT).show()
                        firstPassword = null
                        secondPassword = null
                    }
                }
            } else {
                if (passwordsMatch == true) {
                    val privilegeScreenModel = getScreenModel<PrivilegeScreenModel>()
                    privilegeScreenModel.setPassword(firstPassword.toString())
                    LocalNavigator.current?.pop()
                }
            }
        }
    }

    @Composable
    private fun PasswordSetUi(onEnteredPit: (String) -> Unit) {
        var pin by remember { mutableStateOf("") }
        val shakeAnim = remember { Animatable(0f) }
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PinIndicator(pinLength = pin.length)

            Spacer(modifier = Modifier.height(32.dp))
            val coroutine = rememberCoroutineScope()

            PinKeyboard(
                onNumberClick = { number ->
                    if (pin.length < 6) {
                        pin += number
                        if (pin.length == 6) {
                            coroutine.launch {
                                delay(200)
                                onEnteredPit(pin)
                                shakeAnim.animateTo(
                                    targetValue = 20f,
                                    animationSpec = tween(durationMillis = 50),
                                )
                                shakeAnim.animateTo(
                                    targetValue = -20f,
                                    animationSpec = tween(durationMillis = 50),
                                )
                                shakeAnim.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = 50),
                                )
                            }
                        }
                    }
                },
                {
                    if (pin.isNotEmpty()) {
                        pin = pin.dropLast(1)
                    }
                },
                {},
            )
        }
    }
}
