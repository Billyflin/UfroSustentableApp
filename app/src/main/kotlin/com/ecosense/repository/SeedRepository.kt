package com.ecosense.repository

import com.ecosense.model.RequestStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class SeedRepository {
    private val db = FirebaseFirestore.getInstance()

    data class SeedRequest(
        val materialType: String,
        val quantityKg: Double,
        val description: String,
        val status: RequestStatus,
        val reward: Int,
        val daysAgo: Long
    )

    private val sampleRequests = listOf(
        SeedRequest("Plástico",  3.5,  "Rectoría UFRO",                                   RequestStatus.REEDEMED,   15,  14),
        SeedRequest("Papel",     8.0,  "Biblioteca Central UFRO",                         RequestStatus.REEDEMED,   30,  10),
        SeedRequest("Metal",     2.0,  "Facultad de Ingeniería y Ciencias",               RequestStatus.REWARD,     25,   5),
        SeedRequest("Vidrio",    5.5,  "Casino Norte UFRO",                               RequestStatus.VALIDATING, 0,    3),
        SeedRequest("Electrónicos", 1.0, "Facultad de Ciencias Jurídicas y Empresariales", RequestStatus.PROCESSING, 0,  1),
    )

    suspend fun seedRequestsForUser(userId: String): Result<Int> = runCatching {
        // Verificar si ya tiene datos para no duplicar
        val existing = db.collection("recycling_requests")
            .whereEqualTo("userId", userId)
            .whereEqualTo("seeded", true)
            .get().await()

        if (!existing.isEmpty) return@runCatching 0   // ya fue seeded

        val now = Date()
        var count = 0

        for (req in sampleRequests) {
            val msAgo   = req.daysAgo * 24 * 60 * 60 * 1000L
            val reqTime = Date(now.time - msAgo)
            val updTime = Date(now.time - msAgo + 2 * 60 * 60 * 1000L) // +2h

            db.collection("recycling_requests").add(
                mapOf(
                    "userId"       to userId,
                    "materialType" to req.materialType,
                    "quantityKg"   to req.quantityKg,
                    "description"  to req.description,
                    "status"       to req.status.name,
                    "reward"       to req.reward,
                    "photoUrl"     to "",
                    "timestamp"    to com.google.firebase.Timestamp(reqTime),
                    "updateTime"   to com.google.firebase.Timestamp(updTime),
                    "seeded"       to true
                )
            ).await()
            count++
        }

        // Sumar puntos al usuario (puntos de solicitudes ya canjeadas)
        val pointsEarned = sampleRequests
            .filter { it.status == RequestStatus.REEDEMED }
            .sumOf { it.reward }

        if (pointsEarned > 0) {
            val userRef = db.collection("users").document(userId)
            val snap    = userRef.get().await()
            val current = snap.getLong("points") ?: 0
            userRef.update("points", current + pointsEarned).await()
        }

        count
    }
}
