package put.paginarum.ui.screens

import android.content.Context
import android.os.Build
import android.os.Parcelable
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getScreenModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import put.paginarum.R
import put.paginarum.ui.models.PrivilegeScreenModel
import put.paginarum.util.BiometricPromtUtils

@Composable
fun PriviledgeLoginFragment(screenModel: PrivilegeScreenModel) {
    var pin by remember { mutableStateOf("") }
    val isLocked by screenModel.isLocked.collectAsState()
    val lockedFor by screenModel.lockedCountDown.collectAsState()
    val authenticated by screenModel.isAuthenticated.collectAsState()

    if (!authenticated) {
        val shakeAnim = remember { Animatable(0f) }
        val context = LocalContext.current
        if (isLocked) {
            Surface {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Aplikacja pozostanie zablokowana przez $lockedFor",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        } else {
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
                                val loginResult = screenModel.loginWithPin(pin)
                                if (!loginResult) {
                                    coroutine.launch {
                                        delay(20)
                                        pin = ""
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
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val vibratorManager =
                                            context.getSystemService(
                                                Context.VIBRATOR_MANAGER_SERVICE,
                                            ) as VibratorManager
                                        val vibrator = vibratorManager.defaultVibrator
                                        val vibrationEffect =
                                            VibrationEffect.createOneShot(
                                                100,
                                                VibrationEffect.DEFAULT_AMPLITUDE,
                                            )
                                        vibrator.vibrate(vibrationEffect)
                                    } else {
                                        val vibrator =
                                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                        vibrator.vibrate(
                                            VibrationEffect.createOneShot(
                                                100,
                                                VibrationEffect.DEFAULT_AMPLITUDE,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        if (pin.isNotEmpty()) {
                            pin = pin.dropLast(1)
                        }
                    },
                    onBiometricClick = {
                        val activity = context as FragmentActivity
                        val biometricPrompt =
                            BiometricPromtUtils.createBiometricPrompt(activity) {
                                screenModel.loginWithBiometrics()
                            }
                        val promptInfo = BiometricPromtUtils.createPromptInfo()
                        biometricPrompt.authenticate(promptInfo)
                    },
                )
            }
        }
    }
}

@Parcelize
class PrivilegeScreen(
    val onLoginSuccess: () -> Unit,
) : Parcelable, Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<PrivilegeScreenModel>()
        var pin by remember { mutableStateOf("") }
        val authentucationStatus by screenModel.isAuthenticated.collectAsState()
        val isLocked by screenModel.isLocked.collectAsState()
        val lockedFor by screenModel.lockedCountDown.collectAsState()

        val shakeAnim = remember { Animatable(0f) }
        val context = LocalContext.current
        LaunchedEffect(authentucationStatus) {
            if (authentucationStatus) {
                onLoginSuccess()
            }
        }
        if (isLocked) {
            Surface {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Aplikacja pozostanie zablokowana przez $lockedFor",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        } else {
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
                                val loginResult = screenModel.loginWithPin(pin)
                                if (!loginResult) {
                                    coroutine.launch {
                                        delay(20)
                                        pin = ""
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
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val vibratorManager =
                                            context.getSystemService(
                                                Context.VIBRATOR_MANAGER_SERVICE,
                                            ) as VibratorManager
                                        val vibrator = vibratorManager.defaultVibrator
                                        val vibrationEffect =
                                            VibrationEffect.createOneShot(
                                                100,
                                                VibrationEffect.DEFAULT_AMPLITUDE,
                                            )
                                        vibrator.vibrate(vibrationEffect)
                                    } else {
                                        val vibrator =
                                            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                                        vibrator.vibrate(
                                            VibrationEffect.createOneShot(
                                                100,
                                                VibrationEffect.DEFAULT_AMPLITUDE,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        if (pin.isNotEmpty()) {
                            pin = pin.dropLast(1)
                        }
                    },
                    onBiometricClick = {
                        val activity = context as FragmentActivity
                        val biometricPrompt =
                            BiometricPromtUtils.createBiometricPrompt(activity) {
                                screenModel.loginWithBiometrics()
                            }
                        val promptInfo = BiometricPromtUtils.createPromptInfo()
                        biometricPrompt.authenticate(promptInfo)
                    },
                )
            }
        }
    }
}

@Composable
fun PinIndicator(pinLength: Int) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(6) { index ->
            Box(
                modifier =
                    Modifier
                        .size(20.dp)
                        .background(
                            color =
                                if (index < pinLength) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onPrimary
                                },
                            shape = CircleShape,
                        ),
            )
        }
    }
}

@Composable
fun PinKeyboard(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: () -> Unit,
) {
    val numbers = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "b", "0", "f")
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (row in numbers.chunked(3)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { number ->
                    when (number) {
                        "f" -> {
                            IconButton(
                                onClick = { onDeleteClick() },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_backspace_24),
                                    contentDescription = "back",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        "b" -> {
                            IconButton(
                                onClick = {
                                    onBiometricClick()
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_fingerprint_24),
                                    contentDescription = "back",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        else -> {
                            TextButton(
                                onClick = { onNumberClick(number) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = number,
                                    style = MaterialTheme.typography.displayMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
