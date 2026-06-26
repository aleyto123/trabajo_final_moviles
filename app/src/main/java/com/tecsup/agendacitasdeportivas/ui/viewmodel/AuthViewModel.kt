package com.tecsup.agendacitasdeportivas.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.tecsup.agendacitasdeportivas.R
import com.tecsup.agendacitasdeportivas.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    val currentUser = repository.currentUser

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun setError(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun signUpWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.signUpWithEmail(email, pass)
            result.onSuccess {
                _authState.value = AuthState.Success
            }.onFailure { e ->
                _authState.value = AuthState.Error(translateError(e))
            }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.signInWithEmail(email, pass)
            result.onSuccess {
                _authState.value = AuthState.Success
            }.onFailure { e ->
                _authState.value = AuthState.Error(translateError(e))
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credentialManager = CredentialManager.create(context)
                
                // Jala el ID automáticamente del google-services.json
                val webClientId = context.getString(R.string.default_web_client_id)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false) 
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                
                // Extracción ultra-robusta del token
                val googleIdTokenCredential = when {
                    credential is GoogleIdTokenCredential -> credential
                    credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                        try {
                            GoogleIdTokenCredential.createFrom(credential.data)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    else -> null
                }

                if (googleIdTokenCredential != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    repository.signInWithCredential(firebaseCredential).onSuccess {
                        _authState.value = AuthState.Success
                    }.onFailure { e ->
                        _authState.value = AuthState.Error("Error en Firebase: ${translateError(e)}")
                    }
                } else {
                    _authState.value = AuthState.Error("No se pudo validar la cuenta. Asegúrate de que el SHA-1 en la consola sea el de Debug.")
                }
            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error("Inicio de sesión cancelado.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(translateError(e))
            }
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            repository.signOut()
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("Auth", "Error al limpiar credenciales", e)
            }
            _authState.value = AuthState.Idle
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.updateDisplayName(newName)
                .onSuccess {
                    _authState.value = AuthState.Success
                }
                .onFailure { e ->
                    _authState.value = AuthState.Error(translateError(e))
                }
        }
    }

    private fun translateError(e: Throwable): String {
        val message = e.message ?: ""
        return when {
            message.contains("network error", ignoreCase = true) ||
            message.contains("timeout", ignoreCase = true) ||
            message.contains("unreachable", ignoreCase = true) -> 
                "No se pudo conectar. Revisa tu conexión a internet e inténtalo de nuevo. 📶"
            message.contains("WEAK_PASSWORD") -> "Tu contraseña es demasiado débil. Usa al menos 6 caracteres. 🔒"
            message.contains("INVALID_CREDENTIALS") -> "Los datos ingresados no son correctos. Verifica tu correo o contraseña."
            message.contains("EMAIL_ALREADY_IN_USE") -> "Este correo electrónico ya está registrado con otra cuenta."
            message.contains("10:") -> "Error de conexión con Google. Verifica tu red."
            else -> "Ocurrió un error inesperado: ${e.localizedMessage ?: "inténtalo más tarde"}"
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
