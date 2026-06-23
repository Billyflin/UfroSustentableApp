package com.ecosense.repository

import android.graphics.Bitmap
import com.ecosense.model.RecyclingRequest
import com.ecosense.model.RequestStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Date
import java.util.Locale
import java.util.UUID

class RecyclingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun getRequestsForUser(userId: String): Result<List<RecyclingRequest>> = runCatching {
        db.collection("recycling_requests")
            .whereEqualTo("userId", userId)
            .get().await()
            .map { it.toRecyclingRequest() }
    }

    suspend fun getRequestById(requestId: String): Result<RecyclingRequest?> = runCatching {
        val doc = db.collection("recycling_requests").document(requestId).get().await()
        if (!doc.exists()) null else doc.toRecyclingRequest()
    }

    suspend fun redeemReward(requestId: String, userId: String, rewardPoints: Int): Result<Unit> = runCatching {
        val requestRef = db.collection("recycling_requests").document(requestId)
        val userRef = db.collection("users").document(userId)
        db.runTransaction { tx ->
            val reqSnap = tx.get(requestRef)
            val userSnap = tx.get(userRef)
            if (!reqSnap.exists() || !userSnap.exists()) {
                throw Exception("Solicitud o usuario no encontrado")
            }
            tx.update(requestRef, "status", RequestStatus.REEDEMED.name)
            tx.update(userRef, "points", (userSnap.getLong("points") ?: 0) + rewardPoints)
        }.await()
    }

    suspend fun createRequest(
        userId: String,
        materialType: String,
        quantityKg: Double,
        photoUrl: String,
        description: String?
    ): Result<Unit> = runCatching {
        val newRef = db.collection("recycling_requests").document()
        newRef.set(
            mapOf(
                "userId" to userId,
                "materialType" to materialType,
                "quantityKg" to quantityKg,
                "photoUrl" to photoUrl,
                "description" to (description ?: ""),
                "status" to RequestStatus.PROCESSING.name,
                "reward" to 0,
                "timestamp" to FieldValue.serverTimestamp(),
                "updateTime" to FieldValue.serverTimestamp()
            )
        ).await()

        val userRef = db.collection("users").document(userId)
        try {
            userRef.update("recyclingHistory", FieldValue.arrayUnion(newRef.id)).await()
        } catch (_: Exception) {
            userRef.set(mapOf("recyclingHistory" to listOf(newRef.id)), SetOptions.merge()).await()
        }
    }

    suspend fun uploadImage(bitmap: Bitmap): Result<String> = runCatching {
        val ref = storage.reference.child("images/${UUID.randomUUID()}.jpg")
        val bytes = withContext(Dispatchers.Default) {
            ByteArrayOutputStream().use { baos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                baos.toByteArray()
            }
        }
        ref.putBytes(bytes).await()
        ref.downloadUrl.await().toString()
    }
}

private fun DocumentSnapshot.toRecyclingRequest(): RecyclingRequest {
    val status = try {
        RequestStatus.valueOf(
            getString("status")?.uppercase(Locale.ROOT) ?: "PROCESSING"
        )
    } catch (_: IllegalArgumentException) {
        RequestStatus.PROCESSING
    }
    return RecyclingRequest(
        id = id,
        userId = getString("userId") ?: "",
        materialType = getString("materialType") ?: "",
        quantityKg = getDouble("quantityKg") ?: 0.0,
        photoUrl = getString("photoUrl") ?: "",
        status = status,
        requestTime = getTimestamp("timestamp")?.toDate() ?: Date(),
        updateTime = getTimestamp("updateTime")?.toDate(),
        description = getString("description") ?: "",
        reward = getLong("reward")?.toInt() ?: 0
    )
}
