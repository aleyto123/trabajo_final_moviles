package com.tecsup.agendacitasdeportivas.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<FirebaseUser?>
    suspend fun signUpWithEmail(email: String, pass: String): Result<AuthResult>
    suspend fun signInWithEmail(email: String, pass: String): Result<AuthResult>
    suspend fun signInWithCredential(credential: AuthCredential): Result<AuthResult>
    fun signOut()
    fun isUserLoggedIn(): Boolean
}
