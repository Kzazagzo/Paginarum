package put.paginarum.network

import kotlinx.coroutines.runBlocking
import put.paginarum.ui.models.TrackerScreenModel
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

interface TokenProvider {
    fun getToken(): String
}

@Singleton
class TokenProviderImpl
    @Inject
    constructor(
        private val trackerScreenModelProvider: Provider<TrackerScreenModel>,
    ) : TokenProvider {
        override fun getToken(): String {
            return runBlocking {
                val trackerScreenModel = trackerScreenModelProvider.get()
                trackerScreenModel.getToken()
            }
        }
    }
