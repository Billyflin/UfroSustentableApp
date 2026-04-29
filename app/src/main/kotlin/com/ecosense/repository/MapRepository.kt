package com.ecosense.repository

import com.ecosense.RecyclingPoint
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await

class MapRepository {
    private val db = Firebase.firestore

    // Puntos hardcoded cerca de la UFRO — siempre visibles sin Firestore
    private val ufroPoints = listOf(
        RecyclingPoint(-38.74621, -72.61554, "Rectoría UFRO"),
        RecyclingPoint(-38.74801, -72.61627, "Facultad de Ingeniería y Ciencias"),
        RecyclingPoint(-38.74553, -72.61482, "Biblioteca Central UFRO"),
        RecyclingPoint(-38.74735, -72.61713, "Facultad de Educación, Cs. Sociales y Humanidades"),
        RecyclingPoint(-38.74468, -72.61594, "Casino Norte UFRO"),
        RecyclingPoint(-38.74902, -72.61398, "Facultad de Ciencias Jurídicas y Empresariales"),
        RecyclingPoint(-38.74698, -72.61298, "Estadio y Gimnasio UFRO"),
        RecyclingPoint(-38.74982, -72.61749, "Facultad de Medicina"),
        RecyclingPoint(-38.74493, -72.61703, "Facultad de Ciencias Agropecuarias y Medioambiente"),
        RecyclingPoint(-38.74431, -72.61482, "Acceso Principal – Francisco Salazar 01145"),
    )

    suspend fun getRecyclingPoints(): Result<List<RecyclingPoint>> = runCatching {
        // Intentar traer puntos adicionales de Firestore y fusionar
        val firestorePoints = try {
            db.collection("recycling_points").get().await().map { doc ->
                RecyclingPoint(
                    latitude  = doc.getDouble("latitude")    ?: 0.0,
                    longitude = doc.getDouble("longitude")   ?: 0.0,
                    description = doc.getString("description") ?: ""
                )
            }
        } catch (_: Exception) {
            emptyList()
        }

        // Evitar duplicados exactos por coordenada
        val all = (ufroPoints + firestorePoints)
        all.distinctBy { "${it.latitude},${it.longitude}" }
    }
}
