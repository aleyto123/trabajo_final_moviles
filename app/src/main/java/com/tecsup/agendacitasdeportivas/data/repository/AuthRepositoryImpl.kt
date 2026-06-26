package com.tecsup.agendacitasdeportivas.data.repository

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _currentUser = MutableStateFlow(auth.currentUser)
    override val currentUser: StateFlow<FirebaseUser?> = _currentUser

    init {
        auth.addAuthStateListener {
            _currentUser.value = it.currentUser
        }
    }

    override suspend fun signUpWithEmail(email: String, pass: String): Result<AuthResult> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user
            if (user != null) {
                // Actualizar perfil local con nombre por defecto
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName("Usuario")
                    .build()
                user.updateProfile(profileUpdates).await()

                val userData = hashMapOf(
                    "uid" to user.uid,
                    "email" to email,
                    "displayName" to "Usuario",
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("users").document(user.uid).set(userData).await()
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmail(email: String, pass: String): Result<AuthResult> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithCredential(credential: AuthCredential): Result<AuthResult> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                val userData = hashMapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "displayName" to (user.displayName ?: "Usuario"),
                    "lastLogin" to System.currentTimeMillis()
                )
                db.collection("users").document(user.uid).set(userData, com.google.firebase.firestore.SetOptions.merge()).await()
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDisplayName(name: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
            
            // 1. Actualizar en Firebase Auth
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdates).await()

            // 2. Actualizar en Firestore
            db.collection("users").document(user.uid)
                .update("displayName", name)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null
}
