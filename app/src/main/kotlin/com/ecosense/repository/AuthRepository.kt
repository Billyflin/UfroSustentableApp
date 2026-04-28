package com.ecosense.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUser() = auth.currentUser

    suspend fun signInWithGoogle(idToken: String): Result<String> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw Exception("Autenticación fallida")
        ensureUserDocument(user.uid, user.displayName ?: "", user.email ?: "")
        user.uid
    }

    suspend fun signInAnonymously(): Result<String> = runCatching {
        val result = auth.signInAnonymously().await()
        val user = result.user ?: throw Exception("Autenticación fallida")
        ensureUserDocument(user.uid, "Invitado", "")
        user.uid
    }

    private suspend fun ensureUserDocument(uid: String, name: String, email: String) {
        val docRef = db.collection("users").document(uid)
        val doc = docRef.get().await()
        when {
            !doc.exists() -> docRef.set(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "points" to 0,
                    "recyclingHistory" to emptyList<String>()
                )
            ).await()
            !doc.contains("points") -> docRef.update("points", 0).await()
        }
    }

    fun signOut() = auth.signOut()
}


