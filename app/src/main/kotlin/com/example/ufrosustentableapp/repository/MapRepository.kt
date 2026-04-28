package com.example.ufrosustentableapp.repository

import com.example.ufrosustentableapp.RecyclingPoint
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await

class MapRepository {
    private val db = Firebase.firestore

    suspend fun getRecyclingPoints(): Result<List<RecyclingPoint>> = runCatching {
        val result = db.collection("recycling_points").get().await()
        result.map { document ->
            RecyclingPoint(
                latitude = document.getDouble("latitude") ?: 0.0,
                longitude = document.getDouble("longitude") ?: 0.0,
                description = document.getString("description") ?: ""
            )
        }
    }
}

