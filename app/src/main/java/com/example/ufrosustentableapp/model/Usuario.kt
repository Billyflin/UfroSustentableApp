package com.example.ufrosustentableapp.model

data class Usuario(
    val id: String,
    val email: String,
    val nombre: String,
    var puntos: Int = 0,
    var grupoId: String? = null
)
