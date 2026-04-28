package com.example.ufrosustentableapp.model

import java.util.Date

data class RecyclingRequest(
    val id: String,
    val userId: String,
    val materialType: String,
    val quantityKg: Double,
    val photoUrl: String,
    val status: RequestStatus,
    val requestTime: Date,
    val updateTime: Date?,
    val description: String,
    val reward: Int
)