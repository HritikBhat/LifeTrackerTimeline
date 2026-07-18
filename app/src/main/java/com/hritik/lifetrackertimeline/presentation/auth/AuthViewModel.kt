package com.hritik.lifetrackertimeline.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseUser
import com.hritik.lifetrackertimeline.data.local.entity.UserEntity
import com.hritik.lifetrackertimeline.data.repository.AuthRepository
import com.hritik.lifetrackertimeline.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val currentUser = authRepository.getCurrentUser()
            val localUser = userRepository.user.first()

            if (currentUser != null && localUser != null) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    fun signIn(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.signInWithGoogle(context).fold(
                onSuccess = { credential ->
                    handleFirebaseSignIn(credential)
                },
                onFailure = { e ->
                    _authState.value = AuthState.Error(e.message ?: "Sign in failed")
                }
            )
        }
    }

    private suspend fun handleFirebaseSignIn(credential: GoogleIdTokenCredential) {
        authRepository.firebaseSignInWithGoogle(credential.idToken).fold(
            onSuccess = { firebaseUser ->
                saveUserToFirestoreAndRoom(firebaseUser)
            },
            onFailure = { e ->
                _authState.value = AuthState.Error(e.message ?: "Firebase authentication failed")
            }
        )
    }

    private suspend fun saveUserToFirestoreAndRoom(firebaseUser: FirebaseUser) {
        authRepository.saveOrUpdateUserInFirestore(firebaseUser).fold(
            onSuccess = { user ->
                val userEntity = UserEntity(
                    uid = user.uid,
                    displayName = user.displayName,
                    email = user.email,
                    photoUrl = user.photoUrl,
                    isPremium = user.isPremium
                )
                userRepository.saveUser(userEntity)
                _authState.value = AuthState.Authenticated
            },
            onFailure = { e ->
                _authState.value = AuthState.Error(e.message ?: "Failed to save user data")
            }
        )
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            userRepository.clearUser()
            _authState.value = AuthState.Unauthenticated
        }
    }
}