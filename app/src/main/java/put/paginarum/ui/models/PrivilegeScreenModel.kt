package put.paginarum.ui.models

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import put.paginarum.data.repository.local.SettingsRepository
import put.paginarum.domain.SettingCategory
import javax.inject.Inject

class PrivilegeScreenModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) : ScreenModel {
        private val _isAuthenticated = MutableStateFlow(false)
        val isAuthenticated = _isAuthenticated.asStateFlow()
        private val password: MutableStateFlow<String?> = MutableStateFlow(null)

        private fun authenticate() {
            _isAuthenticated.value = true
        }

        private fun logout() {
            _isAuthenticated.value = false
        }

        fun disablePassword() {
            _isAuthenticated.value = true
            screenModelScope.launch {
                settingsRepository.deleteSetting("PASSWORD")
            }
        }

        fun isPasswordSet(): Boolean = password.value != null

        private fun login(passwordCheck: String): Boolean {
            if (passwordCheck == password.value) {
                authenticate()
                return true
            }
            return false
        }

        private val _loginAttempts = MutableStateFlow(0)
        val loginAttempts: StateFlow<Int> get() = _loginAttempts

        private val _isLocked = MutableStateFlow(false)
        val isLocked: StateFlow<Boolean> get() = _isLocked

        private val _lockedCountDown = MutableStateFlow(0)
        val lockedCountDown = _lockedCountDown.asStateFlow()

        private var lockJob: Job? = null

        init {
            checkLockState()
            screenModelScope.launch {
                settingsRepository.getSetting("PASSWORD").collect {
                    if (it == null) {
                        _isAuthenticated.value = true
                    } else {
                        password.value = it.settingValue.toString()
                    }
                }
            }
        }

        private fun checkLockState() {
            screenModelScope.launch {
                settingsRepository.getSetting("device_locked").collect { setting ->
                    if (setting != null) {
                        val remainingTime = setting.settingValue as Int
                        if (remainingTime > 0) {
                            _isLocked.value = true
                            _lockedCountDown.value = remainingTime
                            continueLock(remainingTime)
                        }
                    }
                }
            }
        }

        private fun continueLock(remainingTime: Int) {
            lockJob?.cancel()
            lockJob =
                screenModelScope.launch {
                    while (_lockedCountDown.value > 0) {
                        delay(1000)
                        _lockedCountDown.value -= 1
                        settingsRepository.insertSetting(
                            "device_locked",
                            SettingCategory.Hidden,
                            _lockedCountDown
                                .value,
                        )
                    }
                    settingsRepository.deleteSetting("device_locked")
                    _isLocked.value = false
                    _loginAttempts.value = 0
                }
        }

        fun loginWithPin(password: String): Boolean {
            if (login(password)) {
                _loginAttempts.value = 0
                return true
            } else {
                _loginAttempts.value += 1
                if (_loginAttempts.value >= 3) {
                    lock()
                }
            }
            return false
        }

        private fun lock() {
            _isLocked.value = true
            lockJob?.cancel()
            lockJob =
                screenModelScope.launch {
                    var remainingTime = 30
                    settingsRepository.insertSetting(
                        "device_locked",
                        SettingCategory.Hidden,
                        remainingTime,
                    )
                    while (remainingTime > 0) {
                        delay(1000)
                        remainingTime -= 1
                        _lockedCountDown.value = remainingTime
                        settingsRepository.insertSetting(
                            "device_locked",
                            SettingCategory.Hidden,
                            remainingTime,
                        )
                    }
                    settingsRepository.deleteSetting("device_locked")
                    _isLocked.value = false
                    _loginAttempts.value = 0
                }
        }

        fun setPassword(password: String) {
            screenModelScope.launch {
                settingsRepository.insertSetting("PASSWORD", SettingCategory.Hidden, password)
                logout()
            }
        }

        fun loginWithBiometrics() {
            authenticate()
        }
    }
