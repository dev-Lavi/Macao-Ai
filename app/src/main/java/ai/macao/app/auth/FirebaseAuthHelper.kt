package ai.macao.app.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ai.macao.app.R

object FirebaseAuthHelper {

    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (email.isBlank() || password.isBlank()) {
            onError("Please enter both email and password.")
            return
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    val msg = task.exception?.localizedMessage ?: "Authentication failed."
                    onError(msg)
                }
            }
    }

    fun signUpWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (email.isBlank() || password.isBlank()) {
            onError("Please enter both email and password.")
            return
        }
        if (password.length < 6) {
            onError("Password must be at least 6 characters.")
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    val msg = task.exception?.localizedMessage ?: "Registration failed."
                    onError(msg)
                }
            }
    }

    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (email.isBlank()) {
            onError("Please enter your email address first.")
            return
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    val msg = task.exception?.localizedMessage ?: "Failed to send reset email."
                    onError(msg)
                }
            }
    }

    fun launchGoogleSignIn(
        context: Context,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val credentialManager = CredentialManager.create(context)
        val webClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            "784673029122-09hbsn1g3o57cudl89lfdchl0k086tbe.apps.googleusercontent.com"
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(authCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onSuccess()
                            } else {
                                onError(task.exception?.localizedMessage ?: "Firebase Google sign in failed.")
                            }
                        }
                } else {
                    onError("Unexpected credential format received.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d("GoogleSignIn", "User cancelled Google sign in")
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Error signing in with Google", e)
                onError(e.localizedMessage ?: "Google Sign-In failed.")
            }
        }
    }
}
