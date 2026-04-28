package com.ecosense.repository

import com.ecosense.model.RewardItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RewardsRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getRewards(): Result<List<RewardItem>> = runCatching {
        val result = db.collection("rewards").get().await()
        result.map { document ->
            RewardItem(
                title = document.getString("title") ?: "",
                pointsRequired = document.getLong("pointsRequired")?.toInt() ?: 0
            )
        }
    }

    fun getUserPointsFlow(userId: String): Flow<Int> = callbackFlow {
        val listener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                trySend((snapshot?.getLong("points") ?: 0).toInt())
            }
        awaitClose { listener.remove() }
    }
}

