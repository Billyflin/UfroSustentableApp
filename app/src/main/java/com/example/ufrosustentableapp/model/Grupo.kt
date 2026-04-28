package com.example.ufrosustentableapp.model

enum class TipoGrupo { PUBLICO, PRIVADO }
enum class RolMiembro { MIEMBRO, ADMINISTRADOR }

data class MiembroGrupo(
    val usuarioId: String,
    val rol: RolMiembro = RolMiembro.MIEMBRO
)

data class Grupo(
    val id: String,
    val nombre: String,
    val descripcion: String = "",
    val tipo: TipoGrupo,
    val miembros: MutableList<MiembroGrupo> = mutableListOf(),
    var puntajeTotal: Int = 0,
    val metaPuntaje: Int = 1000
)
