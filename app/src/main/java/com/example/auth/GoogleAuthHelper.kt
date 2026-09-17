package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed class GoogleAuthResult {
    data class Success(
        val email: String,
        val displayName: String?,
        val photoUrl: String?,
        val idToken: String?,
        val firebaseUser: FirebaseUser?
    ) : GoogleAuthResult()

    data object Cancelled : GoogleAuthResult()

    data class Error(val message: String, val cause: Throwable? = null) : GoogleAuthResult()
}

class GoogleAuthHelper(private val context: Context) {

    private val credentialManager: CredentialManager by lazy {
        CredentialManager.create(context)
    }

    private fun getSafeFirebaseAuth(): FirebaseAuth? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(context.packageName)
                    .setApiKey("AIzaSyFakeKeyForLocalScaffoldOnly12345")
                    .setProjectId("veda-ayur-pharma")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            FirebaseAuth.getInstance()
        } catch (t: Throwable) {
            Log.w("GoogleAuthHelper", "FirebaseAuth initialization notice: ${t.message}")
            null
        }
    }

    suspend fun launchGoogleSignIn(
        activityContext: Context,
        serverClientId: String? = null
    ): GoogleAuthResult = withContext(Dispatchers.IO) {
        val effectiveClientId = serverClientId?.ifBlank { null }
            ?: try {
                activityContext.getString(R.string.default_web_client_id)
            } catch (e: Exception) {
                "869328322719-veda-ayur-pharma-erp.apps.googleusercontent.com"
            }

        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(effectiveClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName
                val photoUrl = googleIdTokenCredential.profilePictureUri?.toString()

                // Authenticate with Firebase Auth using ID Token
                val firebaseUser = tryFirebaseSignIn(idToken)

                GoogleAuthResult.Success(
                    email = email,
                    displayName = displayName,
                    photoUrl = photoUrl,
                    idToken = idToken,
                    firebaseUser = firebaseUser
                )
            } else {
                GoogleAuthResult.Error("Unsupported credential type returned: ${credential.javaClass.simpleName}")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d("GoogleAuthHelper", "User cancelled Google Sign-In")
            GoogleAuthResult.Cancelled
        } catch (e: GetCredentialException) {
            Log.w("GoogleAuthHelper", "CredentialManager failed: ${e.message}", e)
            GoogleAuthResult.Error(
                message = "Credential Manager Error: ${e.message ?: "Sign-in request failed"}",
                cause = e
            )
        } catch (e: Throwable) {
            Log.e("GoogleAuthHelper", "Google Sign-In exception: ${e.message}", e)
            GoogleAuthResult.Error(
                message = "Sign-In Exception: ${e.message ?: "Unexpected error"}",
                cause = e
            )
        }
    }

    private suspend fun tryFirebaseSignIn(idToken: String): FirebaseUser? {
        val auth = getSafeFirebaseAuth() ?: return null
        return try {
            suspendCoroutine { continuation ->
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { authResult ->
                        continuation.resume(authResult.user)
                    }
                    .addOnFailureListener { failure ->
                        Log.w("GoogleAuthHelper", "Firebase Auth sign-in notice: ${failure.message}")
                        // Still continue even if Firebase backend project has not completed setup
                        continuation.resume(auth.currentUser)
                    }
            }
        } catch (t: Throwable) {
            Log.w("GoogleAuthHelper", "Firebase Auth sign-in caught exception: ${t.message}")
            auth.currentUser
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w("GoogleAuthHelper", "Failed to clear credential state: ${e.message}")
        }
        try {
            getSafeFirebaseAuth()?.signOut()
        } catch (e: Exception) {
            Log.w("GoogleAuthHelper", "Failed to signOut from Firebase: ${e.message}")
        }
    }
}
