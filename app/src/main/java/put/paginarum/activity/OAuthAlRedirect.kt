package put.paginarum.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.ResponseTypeValues
import put.paginarum.BuildConfig
import put.paginarum.ui.models.TrackerScreenModel
import javax.inject.Inject

@AndroidEntryPoint
class OAuthAlRedirect : AppCompatActivity() {
    @Inject
    lateinit var trackerScreenModel: TrackerScreenModel

    lateinit var service: AuthorizationService
    private lateinit var authResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        service = AuthorizationService(this)

        supportActionBar?.hide()

        authResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                resolveAuth(result)
            }

        val config =
            AuthorizationServiceConfiguration(
                Uri.parse("https://anilist.co/api/v2/oauth/authorize"),
                Uri.parse("https://anilist.co/api/v2/oauth/token"),
            )

        val request =
            AuthorizationRequest.Builder(
                config,
                BuildConfig.AL_ID,
                ResponseTypeValues.CODE,
                Uri.parse("put.paginarum://callback"),
            ).build()

        val intent = service.getAuthorizationRequestIntent(request)
        authResultLauncher.launch(intent)
    }

    private fun resolveAuth(result: androidx.activity.result.ActivityResult) {
        val intent = result.data ?: return
        val authResponse = AuthorizationResponse.fromIntent(intent)
        val authException = AuthorizationException.fromIntent(intent)
        if (authException != null) {
            Log.d("authError", authException.message.toString())
        } else {
            if (authResponse != null) {
                val code = authResponse.authorizationCode
                val redirectUri = authResponse.request.redirectUri

                val tokenRequest = authResponse.createTokenExchangeRequest()
                val secret = ClientSecretBasic(BuildConfig.AL_KEY)

                service.performTokenRequest(tokenRequest, secret) { res, exception ->
                    if (exception == null) {
                        val token = res?.accessToken
                        trackerScreenModel.addAlKeyToDb(token.toString())
                        finish()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        service.dispose()
    }
}
