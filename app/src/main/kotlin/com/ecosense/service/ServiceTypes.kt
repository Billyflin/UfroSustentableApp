package com.ecosense.service

import com.ecosense.model.Grupo
import com.ecosense.model.Usuario

// ── Acciones de gestión ───────────────────────────────────────────────────────

enum class AccionGestion { AGREGAR, ELIMINAR }

// ── Resultados de GrupoService ────────────────────────────────────────────────

sealed class ResultadoUnion {
    data class Exitoso(val grupo: Grupo) : ResultadoUnion()
    data class Pendiente(val grupoId: String) : ResultadoUnion()
    data class Error(val mensaje: String) : ResultadoUnion()
}

sealed class ResultadoCreacion {
    data class Exitoso(val grupo: Grupo) : ResultadoCreacion()
    data class Error(val mensaje: String) : ResultadoCreacion()
}

sealed class ResultadoGestion {
    data object Exitoso : ResultadoGestion()
    data class Error(val mensaje: String) : ResultadoGestion()
}

// ── Resultados de RankingService ──────────────────────────────────────────────

sealed class ResultadoRankingGrupo {
    data class Exitoso(val ranking: List<EntradaRankingInterno>) : ResultadoRankingGrupo()
    data class Error(val mensaje: String) : ResultadoRankingGrupo()
}

// ── Data classes de ranking ───────────────────────────────────────────────────

data class EntradaRanking(val usuario: Usuario, val posicion: Int)

data class EntradaRankingGrupal(val grupo: Grupo, val posicion: Int)

data class EntradaRankingInterno(
    val usuario: Usuario,
    val posicion: Int,
    val posicionGlobal: Int
)

// ── Recompensas grupales ──────────────────────────────────────────────────────

data class RecompensaGrupal(val grupoId: String)

data class ProgresoRecompensa(val puntajeActual: Int, val metaPuntaje: Int) {
    val puntajeRestante: Int get() = metaPuntaje - puntajeActual
}
