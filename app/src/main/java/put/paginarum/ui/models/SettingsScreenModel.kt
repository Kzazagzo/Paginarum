package put.paginarum.ui.models

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import put.paginarum.data.repository.local.SettingsRepository
import put.paginarum.domain.Setting
import put.paginarum.util.DataStatus
import javax.inject.Inject

class SettingsScreenModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) : ScreenModel {
        private val _settings =
            MutableStateFlow<DataStatus<Map<String, List<Setting<*>>>>>(
                DataStatus
                    .Loading,
            )
        val settings = _settings.asStateFlow()

        init {
            loadSettings()
        }

        private fun loadSettings() {
            screenModelScope.launch {
                settingsRepository.settings.collect {
                    try {
                        _settings.value = DataStatus.Success(it)
                    } catch (e: Exception) {
                        _settings.value = DataStatus.Error(e.message)
                    }
                }
            }
        }
    }
