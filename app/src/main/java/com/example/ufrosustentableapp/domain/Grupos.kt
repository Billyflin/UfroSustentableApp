package com.example.ufrosustentableapp.domain

enum class TipoGrupo {
    PUBLICO, PRIVADO
}

enum class ResultadoUnirse {
    EXITOSO, PENDIENTE, ERROR
}

data class Usuario(
    val id: String,
    val email: String,
    var grupoId: String? = null,
    val puntos: Int = 0
)

data class Grupo(
    val id: String,
    val nombre: String,
    val tipo: TipoGrupo,
    var puntajeTotal: Int = 0
)

class UnirseGrupoUseCase(
    private val gruposRepository: List<Grupo>,
    private val usuariosRepository: MutableList<Usuario>
) {
    fun unirse(usuarioId: String, grupoId: String): Pair<ResultadoUnirse, String?> {
        val usuario = usuariosRepository.find { it.id == usuarioId }
            ?: return Pair(ResultadoUnirse.ERROR, "Usuario no encontrado")
        val grupo = gruposRepository.find { it.id == grupoId }
            ?: return Pair(ResultadoUnirse.ERROR, "Grupo no encontrado")

        if (usuario.grupoId == grupoId) {
            return Pair(ResultadoUnirse.ERROR, "El usuario ya pertenece a este grupo")
        }

        if (usuario.grupoId != null) {
            return Pair(ResultadoUnirse.ERROR, "El usuario ya pertenece a un grupo")
        }

        return if (grupo.tipo == TipoGrupo.PUBLICO) {
            usuario.grupoId = grupo.id
            grupo.puntajeTotal += usuario.puntos
            Pair(ResultadoUnirse.EXITOSO, null)
        } else {
            Pair(ResultadoUnirse.PENDIENTE, null)
        }
    }
}

