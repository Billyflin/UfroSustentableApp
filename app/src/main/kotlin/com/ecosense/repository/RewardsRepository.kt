package com.ecosense.repository

import com.ecosense.model.RewardItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RewardsRepository {
    private val db = FirebaseFirestore.getInstance()

    // In-memory cache so rewards don't reload on every back-navigation
    private var cachedRewards: List<RewardItem>? = null

    suspend fun getRewards(): Result<List<RewardItem>> {
        cachedRewards?.let { return Result.success(it) }
        return runCatching {
            db.collection("rewards").get().await().map { doc ->
                RewardItem(
                    title          = doc.getString("title") ?: "",
                    pointsRequired = doc.getLong("pointsRequired")?.toInt() ?: 0
                )
            }.also { cachedRewards = it }
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
