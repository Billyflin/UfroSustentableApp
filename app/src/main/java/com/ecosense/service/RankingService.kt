package com.ecosense.service

import com.ecosense.model.Grupo
import com.ecosense.model.Usuario

data class PosicionUsuario(
    val posicion: Int,
    val usuario: Usuario,
    val posicionGlobal: Int? = null
)

data class PosicionGrupo(val posicion: Int, val grupo: Grupo)

sealed class ResultadoRankingGrupo {
    data class Exitoso(val ranking: List<PosicionUsuario>) : ResultadoRankingGrupo()
    data class Error(val mensaje: String) : ResultadoRankingGrupo()
}

class RankingService {

    fun obtenerRankingGlobal(usuarios: List<Usuario>): List<PosicionUsuario> =
        usuarios
            .sortedByDescending { it.puntos }
            .mapIndexed { index, usuario ->
                PosicionUsuario(posicion = index + 1, usuario = usuario)
            }

    fun obtenerPosicionGlobal(usuarioId: String, usuarios: List<Usuario>): Int? =
        obtenerRankingGlobal(usuarios).find { it.usuario.id == usuarioId }?.posicion

    fun obtenerRankingInterno(grupo: Grupo, todosLosUsuarios: List<Usuario>): ResultadoRankingGrupo {
        val miembrosIds = grupo.miembros.map { it.usuarioId }.toSet()
        val miembros = todosLosUsuarios.filter { it.id in miembrosIds }
        if (miembros.isEmpty()) return ResultadoRankingGrupo.Error("El grupo no tiene miembros")
        val posicionesGlobales = obtenerRankingGlobal(todosLosUsuarios).associate { it.usuario.id to it.posicion }
        return ResultadoRankingGrupo.Exitoso(
            miembros.sortedByDescending { it.puntos }
                .mapIndexed { index, usuario ->
                    PosicionUsuario(
                        posicion = index + 1,
                        usuario = usuario,
                        posicionGlobal = posicionesGlobales[usuario.id]
                    )
                }
        )
    }

    fun obtenerRankingGrupal(grupos: List<Grupo>): List<PosicionGrupo> =
        grupos
            .sortedByDescending { it.puntajeTotal }
            .mapIndexed { index, grupo -> PosicionGrupo(posicion = index + 1, grupo = grupo) }

    fun obtenerPosicionGrupo(grupoId: String, grupos: List<Grupo>): Int? =
        obtenerRankingGrupal(grupos).find { it.grupo.id == grupoId }?.posicion
}

