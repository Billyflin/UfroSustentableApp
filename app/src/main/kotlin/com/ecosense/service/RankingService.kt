package com.ecosense.service

import com.ecosense.model.Grupo
import com.ecosense.model.Usuario

/**
 * Servicio de dominio puro para ranking de usuarios y grupos (RF-10, RF-13, RF-17).
 * Sin estado interno: opera sobre las listas que recibe como parámetro.
 */
class RankingService {

    // RF-10: Ranking global de usuarios
    fun obtenerRankingGlobal(usuarios: List<Usuario>): List<EntradaRanking> =
        usuarios
            .sortedByDescending { it.puntos }
            .mapIndexed { idx, u -> EntradaRanking(u, idx + 1) }

    fun obtenerPosicionGlobal(usuarioId: String, usuarios: List<Usuario>): Int {
        val ranking = obtenerRankingGlobal(usuarios)
        return ranking.indexOfFirst { it.usuario.id == usuarioId } + 1
    }

    // RF-13: Ranking interno de un grupo con posición global
    fun obtenerRankingInterno(grupo: Grupo, todos: List<Usuario>): ResultadoRankingGrupo {
        val ids      = grupo.miembros.map { it.usuarioId }.toSet()
        val miembros = todos.filter { it.id in ids }
        if (miembros.isEmpty())
            return ResultadoRankingGrupo.Error("El grupo no tiene miembros")

        val globalMap = obtenerRankingGlobal(todos).associate { it.usuario.id to it.posicion }

        val interno = miembros
            .sortedByDescending { it.puntos }
            .mapIndexed { idx, u ->
                EntradaRankingInterno(
                    usuario        = u,
                    posicion       = idx + 1,
                    posicionGlobal = globalMap[u.id] ?: 0
                )
            }
        return ResultadoRankingGrupo.Exitoso(interno)
    }

    // RF-17: Ranking grupal
    fun obtenerRankingGrupal(grupos: List<Grupo>): List<EntradaRankingGrupal> =
        grupos
            .sortedByDescending { it.puntajeTotal }
            .mapIndexed { idx, g -> EntradaRankingGrupal(g, idx + 1) }

    fun obtenerPosicionGrupo(grupoId: String, grupos: List<Grupo>): Int? =
        obtenerRankingGrupal(grupos).find { it.grupo.id == grupoId }?.posicion
}
