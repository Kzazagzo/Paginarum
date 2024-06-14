package put.paginarum.ui.models

import android.content.Context
import android.content.Intent
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import put.paginarum.activity.OAuthAlRedirect
import put.paginarum.data.api.providerApi.AlSearchResponseImpl
import put.paginarum.data.repository.local.AlTrackerRepository
import put.paginarum.data.repository.local.SettingsRepository
import put.paginarum.data.repository.network.AniListRepository
import put.paginarum.database.al.AlTrackerEntity
import put.paginarum.domain.SettingCategory
import put.paginarum.domain.al.ReviewAl
import put.paginarum.domain.al.UserAl
import put.paginarum.util.DataStatus
import javax.inject.Inject

class TrackerScreenModel
    @Inject
    constructor(
        private val repository: AniListRepository,
        private val settingsRepository: SettingsRepository,
        private val alTrackerRepository: AlTrackerRepository,
    ) : ScreenModel {
//        private val _data = MutableStateFlow<String?>(null)
//        val data = _data.asStateFlow()
        private val _alTrackingStatus = MutableStateFlow(false)
        val alTrackingStatus = _alTrackingStatus.asStateFlow()

        private val _currentUserData = MutableStateFlow<DataStatus<UserAl>>(DataStatus.Loading)
        val currentUserData = _currentUserData.asStateFlow()

        fun fetchCurrentUserData() {
            screenModelScope.launch {
                val token = getToken()
                if (token.isNotBlank() && !_alTrackingStatus.value) {
                    _alTrackingStatus.value = true
                    fetchCurrentUserData()
                    try {
                        val user = repository.getUserData()
                        _currentUserData.value = DataStatus.Success(user)
                    } catch (e: Exception) {
                        _currentUserData.value = DataStatus.Error(e.toString())
                    }
                }
            }
        }

        suspend fun fetchNovelData(
            novelName: String,
            pageNumber: Int,
        ): List<AlSearchResponseImpl> {
            return withContext(screenModelScope.coroutineContext) {
                repository.getNovelData(pageNumber, novelName)
            }
        }

        suspend fun getNovelReviews(
            page: Int,
            alNovelId: String,
        ): DataStatus<List<ReviewAl>> {
            return withContext(screenModelScope.coroutineContext) {
                try {
                    val reviewsFlow = repository.getNovelReviews(page, alNovelId)
                    DataStatus.Success(reviewsFlow)
                } catch (e: Exception) {
                    DataStatus.Error(e.message)
                }
            }
        }

        fun alAuth(context: Context) {
            val intent = Intent(context, OAuthAlRedirect::class.java)
            context.startActivity(intent)
        }

        suspend fun logOut() {
            _alTrackingStatus.value = false
            _currentUserData.value = DataStatus.Loading
            alTrackerRepository.deleteAllTrackers()
            settingsRepository.deleteSetting("AL_KEY")
        }

        fun addAlKeyToDb(token: String) {
            screenModelScope.launch {
                settingsRepository.insertSetting(
                    "AL_KEY",
                    SettingCategory.Hidden,
                    token.replace(Regex("^\"|\"$"), ""),
                )
            }
        }

        fun addTrackingToNovel(
            novelId: String,
            alStatus: AniListRepository.AlNovelStatus,
        ) {
            screenModelScope.launch {
                repository.updateUserNovelStatus(novelId, alStatus)
            }
        }

        suspend fun getToken(): String {
            return settingsRepository.getSetting("AL_KEY")
                .filterNotNull()
                .map { it.settingValue.toString() }
                .first()
        }

        suspend fun getNovelTrackingStatus(novelName: String): AlTrackerEntity? {
            return withContext(screenModelScope.coroutineContext) {
                val fromDb = alTrackerRepository.getAlTrackerByProviderNovelName(novelName)
                fromDb?.let { entity ->
                    repository.getNovelTrackerStatus(entity.alId)?.let { status ->
                        entity.selectedTracking = status
                        entity
                    }
                }
            }
        }

        suspend fun insertAlTracker(alTrackerEntity: AlTrackerEntity) {
            screenModelScope.launch {
                alTrackerRepository.insertAlTracker(alTrackerEntity)
                repository.updateUserNovelStatus(
                    alTrackerEntity.alId,
                    alTrackerEntity
                        .selectedTracking!!,
                )
            }
        }

        suspend fun deleteAlTracker(alTrackerEntity: AlTrackerEntity) {
            screenModelScope.launch {
                alTrackerRepository.deleteAlTracker(alTrackerEntity)
            }
        }
    }
