package com.worksd.blanc.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class LoginRepository @Inject constructor() {

    private val TAG = LoginRepository::class.java.simpleName

    suspend fun kakaoLogin(context: Context) = suspendCancellableCoroutine { continuation ->
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                continuation.cancel(error)
            } else if (token != null) {
                continuation.resume(token.accessToken)
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                } else if (token != null) {
                    continuation.resume(token.accessToken)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    suspend fun googleLogin(context: Context): String {

        Log.d("WebAppInterface", "googleLogin")
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("230694049526-u6kguehnbo2bqmbvqh7l25quqfe6aihj.apps.googleusercontent.com")
            .setAutoSelectEnabled(true)
            .setNonce("sdlfkjsdlkfj")
            .build()

        Log.d("WebAppInterface", "googleIdOption: $googleIdOption")

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        Log.d("WebAppInterface", "request: $request")
        val credentialManager = CredentialManager.create(context)
        Log.d("WebAppInterface", "credentialManager: $credentialManager")
        val result = credentialManager.getCredential(
            request = request,
            context = context,
        )
        Log.d("WebAppInterface", "result: $result")
        return handleSignIn(result)

    }

    private fun handleSignIn(result: GetCredentialResponse): String {
        // Handle the successfully returned credential.
        when (val credential = result.credential) {

            // Passkey credential
            is PublicKeyCredential -> {
                // Share responseJson such as a GetCredentialResponse on your server to
                // validate and authenticate
                val responseJson = credential.authenticationResponseJson
                Log.d("WebAppInterface", "responseJson: $responseJson")
            }

            // Password credential
            is PasswordCredential -> {
                // Send ID and password to your server to validate and authenticate.
                val username = credential.id
                val password = credential.password
                Log.d("WebAppInterface", "username: $username, password: $password")
            }

             is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                        Log.d("WebAppInterface", "googleIdToken: ${googleIdToken.idToken}")
                        Log.d("WebAppInterface", "Google ID: ${googleIdToken.id}")
                        Log.d("WebAppInterface", "Display Name: ${googleIdToken.displayName}")
                        Log.d("WebAppInterface", "Profile Picture URI: ${googleIdToken.profilePictureUri}")
                        Log.d("WebAppInterface", "Given Name: ${googleIdToken.givenName}")
                        Log.d("WebAppInterface", "Family Name: ${googleIdToken.familyName}")

                        // 추가 디버깅을 위한 전체 데이터 로깅
                        Log.d("WebAppInterface", "Full Credential Data: ${credential.data}")

                        return googleIdToken.idToken

                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("WebAppInterface", "Received an invalid google id token response", e)
                        Log.e("WebAppInterface", "Error details: ${e.message}")
                        Log.e("WebAppInterface", "Raw credential data: ${credential.data}")
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e("WebAppInterface", "Unexpected type of credential: ${credential.type}")
                    Log.e("WebAppInterface", "Credential data: ${credential.data}")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e("WebAppInterface", "Unexpected type of credential")
            }
        }
        return ""
    }
}