package com.example.ufrosustentableapp.repository

import android.graphics.Bitmap
import com.example.ufrosustentableapp.model.RecyclingRequest
import com.example.ufrosustentableapp.model.RequestStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.UUID

class RecyclingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun getRequestsForUser(userId: String): Result<List<RecyclingRequest>> = runCatching {
        val result = db.collection("recycling_requests")
            .whereEqualTo("userId", userId)
            .get().await()
        result.map { document ->
            val status = try {
                RequestStatus.valueOf(
                    document.getString("status")?.uppercase(Locale.ROOT) ?: "PROCESSING"
                )
            } catch (_: IllegalArgumentException) {
                RequestStatus.PROCESSING
            }
            RecyclingRequest(
                id = document.id,
                userId = document.getString("userId") ?: "",
                materialType = document.getString("materialType") ?: "",
                quantityKg = document.getDouble("quantityKg") ?: 0.0,
                photoUrl = document.getString("photoUrl") ?: "",
                status = status,
                requestTime = document.getTimestamp("timestamp")?.toDate()!!,
                updateTime = document.getTimestamp("updateTime")?.toDate(),
                description = document.getString("description") ?: "",
                reward = document.getLong("reward")?.toInt() ?: 0
            )
        }
    }

    suspend fun getRequestById(requestId: String): Result<RecyclingRequest?> = runCatching {
        val document = db.collection("recycling_requests").document(requestId).get().await()
        if (!document.exists()) return@runCatching null
        val status = try {
            RequestStatus.valueOf(
                document.getString("status")?.uppercase(Locale.ROOT) ?: "PROCESSING"
            )
        } catch (_: IllegalArgumentException) {
            RequestStatus.PROCESSING
        }
        RecyclingRequest(
            id = document.id,
            userId = document.getString("userId") ?: "",
            materialType = document.getString("materialType") ?: "",
            quantityKg = document.getDouble("quantityKg") ?: 0.0,
            photoUrl = document.getString("photoUrl") ?: "",
            status = status,
            requestTime = document.getTimestamp("timestamp")?.toDate()!!,
            updateTime = document.getTimestamp("updateTime")?.toDate(),
            description = document.getString("description") ?: "",
            reward = document.getLong("reward")?.toInt() ?: 0
        )
    }

    suspend fun redeemReward(requestId: String, userId: String, rewardPoints: Int): Result<Unit> = runCatching {
        val requestRef = db.collection("recycling_requests").document(requestId)
        val userRef = db.collection("users").document(userId)
        db.runTransaction { transaction ->
            val requestSnap = transaction.get(requestRef)
            val userSnap = transaction.get(userRef)
            if (!requestSnap.exists() || !userSnap.exists()) {
                throw Exception("Solicitud o usuario no encontrado")
            }
            transaction.update(requestRef, "status", RequestStatus.REEDEMED.name)
            val currentPoints = userSnap.getLong("points") ?: 0
            transaction.update(userRef, "points", currentPoints + rewardPoints)
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
                "description" to description,
                "userId" to userId,
                "materialType" to materialType,
                "quantityKg" to quantityKg,
                "photoUrl" to photoUrl,
                "status" to RequestStatus.PROCESSING,
                "timestamp" to FieldValue.serverTimestamp(),
                "updateTime" to FieldValue.serverTimestamp(),
                "reward" to 0
            )
        ).await()
        db.collection("users").document(userId)
            .update("recyclingHistory", FieldValue.arrayUnion(newRef.id)).await()
    }

    suspend fun uploadImage(bitmap: Bitmap): Result<String> = runCatching {
        val ref = storage.reference.child("images/${UUID.randomUUID()}.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        ref.putBytes(baos.toByteArray()).await()
        ref.downloadUrl.await().toString()
    }
}
