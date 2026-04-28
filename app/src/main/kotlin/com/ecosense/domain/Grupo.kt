package com.ecosense.domain

data class Grupo(
    val id: String,
    val nombre: String,
    val tipo: TipoGrupo,
    val descripcion: String = "",
    var puntajeTotal: Int = 0,
    val capacidad: Int = 100,
    val miembros: MutableList<String> = mutableListOf(),
    val admins: MutableList<String> = mutableListOf(),
    val recompensasDisponibles: MutableList<String> = mutableListOf()
)


