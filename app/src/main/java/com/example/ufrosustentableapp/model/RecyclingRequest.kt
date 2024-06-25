package com.example.ufrosustentableapp.model

data class RecyclingRequest(
    val id: String,
    val userId: String,
    val materialType: String,
    val quantityKg: Double,
    val photoUrl: String,
    val status: RequestStatus,
    val requestTime: String,
    val updateTime: String
)