package com.ecosense.model

// ── Enums ────────────────────────────────────────────────────────────────────

enum class TipoGrupo { PUBLICO, PRIVADO }

enum class RolMiembro { ADMINISTRADOR, MIEMBRO }

// ── Modelos ──────────────────────────────────────────────────────────────────

data class MiembroGrupo(
    val usuarioId: String,
    val rol: RolMiembro = RolMiembro.MIEMBRO
)

data class Grupo(
    val id: String,
    val nombre: String,
    val tipo: TipoGrupo,
    var puntajeTotal: Int = 0,
    /** Puntaje mínimo para desbloquear la recompensa grupal. */
    val metaPuntaje: Int = Int.MAX_VALUE,
    val miembros: MutableList<MiembroGrupo> = mutableListOf()
)

data class Usuario(
    val id: String,
    val email: String,
    val nombre: String,
    var puntos: Int = 0,
    var grupoId: String? = null
)
