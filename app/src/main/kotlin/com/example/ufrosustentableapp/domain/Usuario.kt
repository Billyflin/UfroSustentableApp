package com.example.ufrosustentableapp.domain

data class Usuario(
    val id: String,
    val email: String,
    var grupoId: String? = null,
    var puntos: Int = 0,
    val recompensasReclamadas: MutableList<String> = mutableListOf()
)
