package com.example.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        auth?.let { firebaseAuth ->
            _user.value = firebaseAuth.currentUser
            firebaseAuth.addAuthStateListener {
                _user.value = it.currentUser
            }
        } ?: run {
            _uiState.value = AuthUiState.Error("Firebase não inicializado. Verifique o arquivo google-services.json")
        }
    }

    fun loginWithEmail(email: String, password: String) {
        val currentAuth = auth
        if (currentAuth == null) {
            _uiState.value = AuthUiState.Error("Firebase não disponível")
            return
        }
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Preencha todos os campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                currentAuth.signInWithEmailAndPassword(email, password).await()
                _uiState.value = AuthUiState.Authenticated
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(truncateError(e.message))
            }
        }
    }

    fun registerWithEmail(email: String, password: String, name: String) {
        val currentAuth = auth
        if (currentAuth == null) {
            _uiState.value = AuthUiState.Error("Firebase não disponível")
            return
        }
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _uiState.value = AuthUiState.Error("Preencha todos os campos")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("A senha deve ter pelo menos 6 caracteres")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = currentAuth.createUserWithEmailAndPassword(email, password).await()
                result.user?.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                )?.await()
                _uiState.value = AuthUiState.Authenticated
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(truncateError(e.message))
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        val currentAuth = auth
        if (currentAuth == null) {
            _uiState.value = AuthUiState.Error("Firebase não disponível")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                currentAuth.signInWithCredential(credential).await()
                _uiState.value = AuthUiState.Authenticated
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(truncateError(e.message))
            }
        }
    }

    fun logout() {
        auth?.signOut()
        _uiState.value = AuthUiState.Idle
    }

    fun clearError() {
        _uiState.value = AuthUiState.Idle
    }

    private fun truncateError(message: String?): String {
        return if (message != null && message.length > 100) {
            message.take(100) + "..."
        } else {
            message ?: "Erro desconhecido"
        }
    }
}

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object Authenticated : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
