package com.hritik.lifetrackertimeline.data.repository

import com.hritik.lifetrackertimeline.R
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.hritik.lifetrackertimeline.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun signInWithGoogle(): Result<GoogleIdTokenCredential> {
        return try {
            val googleIdTokenOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdTokenOption)
                .build()

            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential

            when {
                credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {

                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)

                    Result.success(googleIdTokenCredential)
                }

                else -> {
                    Result.failure(
                        Exception("Unexpected credential type: ${credential::class.java.name}")
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun firebaseSignInWithGoogle(googleIdToken: String): Result<com.google.firebase.auth.FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw Exception("Firebase User is null")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveOrUpdateUserInFirestore(user: com.google.firebase.auth.FirebaseUser): Result<User> {
        return try {
            val userRef = firestore.collection("users").document(user.uid)
            val snapshot = userRef.get().await()
            
            val userData = if (snapshot.exists()) {
                val existingUser = snapshot.toObject(User::class.java)!!
                existingUser.copy(lastLoginAt = Timestamp.now())
            } else {
                User(
                    uid = user.uid,
                    displayName = user.displayName ?: "",
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString() ?: "",
                    createdAt = Timestamp.now(),
                    lastLoginAt = Timestamp.now()
                )
            }
            
            userRef.set(userData).await()
            Result.success(userData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        auth.signOut()
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }

    fun getCurrentUser() = auth.currentUser
}