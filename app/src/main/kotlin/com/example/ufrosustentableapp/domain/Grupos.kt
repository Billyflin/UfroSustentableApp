package com.example.ufrosustentableapp.domain

class CrearGrupoUseCase(
    private val gruposRepository: MutableList<Grupo>,
    private val usuariosRepository: MutableList<Usuario>
) {
    fun crearGrupo(usuarioId: String, nombre: String, descripcion: String, tipo: TipoGrupo): Pair<ResultadoAccion, String?> {
        val usuario = usuariosRepository.find { it.id == usuarioId }
            ?: return Pair(ResultadoAccion.ERROR, "Usuario no encontrado")

        if (nombre.isBlank()) return Pair(ResultadoAccion.ERROR, "El nombre del grupo no puede estar vacio")
        if (usuario.grupoId != null) return Pair(ResultadoAccion.ERROR, "El usuario ya pertenece a un grupo")

        val nuevoId = "G${gruposRepository.size + 1}"
        val grupo = Grupo(
            id = nuevoId,
            nombre = nombre,
            tipo = tipo,
            descripcion = descripcion,
            miembros = mutableListOf(usuarioId),
            admins = mutableListOf(usuarioId),
            puntajeTotal = usuario.puntos
        )
        gruposRepository.add(grupo)
        usuario.grupoId = grupo.id
        return Pair(ResultadoAccion.EXITOSO, nuevoId)
    }
}

class UnirseGrupoUseCase(
    private val gruposRepository: List<Grupo>,
    private val usuariosRepository: MutableList<Usuario>
) {
    fun unirse(usuarioId: String, grupoId: String): Pair<ResultadoAccion, String?> {
        val usuario = usuariosRepository.find { it.id == usuarioId }
            ?: return Pair(ResultadoAccion.ERROR, "Usuario no encontrado")
        val grupo = gruposRepository.find { it.id == grupoId }
            ?: return Pair(ResultadoAccion.ERROR, "Grupo no encontrado")

        if (usuario.grupoId == grupoId) {
            return Pair(ResultadoAccion.ERROR, "El usuario ya pertenece a este grupo")
        }

        if (usuario.grupoId != null) {
            return Pair(ResultadoAccion.ERROR, "El usuario ya pertenece a un grupo")
        }

        if (grupo.miembros.size >= grupo.capacidad) {
            return Pair(ResultadoAccion.ERROR, "El grupo ha alcanzado su capacidad maxima")
        }

        return if (grupo.tipo == TipoGrupo.PUBLICO) {
            usuario.grupoId = grupo.id
            grupo.miembros.add(usuario.id)
            grupo.puntajeTotal += usuario.puntos
            Pair(ResultadoAccion.EXITOSO, null)
        } else {
            Pair(ResultadoAccion.PENDIENTE, null)
        }
    }
}

class SumarPuntosUseCase(
    private val gruposRepository: List<Grupo>,
    private val usuariosRepository: MutableList<Usuario>
) {
    private val metas = mapOf(500 to "Insignia Oro", 1000 to "Trofeo Diamante")

    fun sumarPuntos(usuarioId: String, puntosNuevos: Int): Pair<ResultadoAccion, String?> {
        val usuario = usuariosRepository.find { it.id == usuarioId }
            ?: return Pair(ResultadoAccion.ERROR, "Usuario no encontrado")

        usuario.puntos += puntosNuevos

        val grupoId = usuario.grupoId
        if (grupoId != null) {
            val grupo = gruposRepository.find { it.id == grupoId }
            if (grupo != null) {
                grupo.puntajeTotal += puntosNuevos

                for ((metaPuntos, recompensa) in metas) {
                    if (grupo.puntajeTotal >= metaPuntos && !grupo.recompensasDisponibles.contains(recompensa)) {
                        grupo.recompensasDisponibles.add(recompensa)
                    }
                }
            }
        }
        return Pair(ResultadoAccion.EXITOSO, null)
    }
}

class GestionarMiembrosUseCase(
    private val gruposRepository: List<Grupo>,
    private val usuariosRepository: MutableList<Usuario>
) {
    fun eliminarMiembro(adminId: String, miembroId: String, grupoId: String): Pair<ResultadoAccion, String?> {
        val grupo = gruposRepository.find { it.id == grupoId } ?: return Pair(ResultadoAccion.ERROR, "Grupo no encontrado")
        if (!grupo.admins.contains(adminId)) return Pair(ResultadoAccion.ERROR, "Permisos insuficientes")
        if (!grupo.miembros.contains(miembroId)) return Pair(ResultadoAccion.ERROR, "El usuario no es miembro del grupo")
        if (adminId == miembroId) return Pair(ResultadoAccion.ERROR, "El administrador no puede eliminarse a si mismo")

        grupo.miembros.remove(miembroId)
        val miembro = usuariosRepository.find { it.id == miembroId }
        if (miembro != null) {
            grupo.puntajeTotal -= miembro.puntos
            miembro.grupoId = null
        }
        return Pair(ResultadoAccion.EXITOSO, null)
    }

    fun agregarMiembro(adminId: String, usuarioId: String, grupoId: String): Pair<ResultadoAccion, String?> {
        val grupo = gruposRepository.find { it.id == grupoId } ?: return Pair(ResultadoAccion.ERROR, "Grupo no encontrado")
        if (!grupo.admins.contains(adminId)) return Pair(ResultadoAccion.ERROR, "Permisos insuficientes")
        
        val usuario = usuariosRepository.find { it.id == usuarioId } ?: return Pair(ResultadoAccion.ERROR, "Usuario no encontrado")
        if (usuario.grupoId != null) return Pair(ResultadoAccion.ERROR, "El usuario ya pertenece a un grupo")
        if (grupo.miembros.size >= grupo.capacidad) return Pair(ResultadoAccion.ERROR, "El grupo ha alcanzado su capacidad maxima")

        grupo.miembros.add(usuarioId)
        usuario.grupoId = grupoId
        grupo.puntajeTotal += usuario.puntos
        return Pair(ResultadoAccion.EXITOSO, null)
    }

    fun asignarRolAdmin(adminId: String, miembroId: String, grupoId: String): Pair<ResultadoAccion, String?> {
        val grupo = gruposRepository.find { it.id == grupoId } ?: return Pair(ResultadoAccion.ERROR, "Grupo no encontrado")
        if (!grupo.admins.contains(adminId)) return Pair(ResultadoAccion.ERROR, "Permisos insuficientes")
        if (!grupo.miembros.contains(miembroId)) return Pair(ResultadoAccion.ERROR, "El usuario no es miembro del grupo")

        if (!grupo.admins.contains(miembroId)) {
            grupo.admins.add(miembroId)
        }
        return Pair(ResultadoAccion.EXITOSO, null)
    }
}

class RankingGruposUseCase(
    private val gruposRepository: List<Grupo>,
    private val usuariosRepository: List<Usuario>
) {
    fun obtenerRankingGlobalGrupos(): List<Grupo> {
        return gruposRepository.sortedByDescending { it.puntajeTotal }
    }

    fun obtenerRankingInternoGrupo(grupoId: String): List<Usuario> {
        val grupo = gruposRepository.find { it.id == grupoId } ?: return emptyList()
        return usuariosRepository.filter { grupo.miembros.contains(it.id) }.sortedByDescending { it.puntos }
    }
}

class RecompensasGrupalesUseCase(
    private val gruposRepository: List<Grupo>,
    private val usuariosRepository: MutableList<Usuario>
) {
    fun reclamarRecompensa(usuarioId: String, recompensaNombre: String): Pair<ResultadoAccion, String?> {
        val usuario = usuariosRepository.find { it.id == usuarioId }
            ?: return Pair(ResultadoAccion.ERROR, "Usuario no encontrado")
        
        val grupoId = usuario.grupoId ?: return Pair(ResultadoAccion.ERROR, "El usuario no pertenece a un grupo")
        val grupo = gruposRepository.find { it.id == grupoId } ?: return Pair(ResultadoAccion.ERROR, "Grupo no encontrado")

        if (!grupo.recompensasDisponibles.contains(recompensaNombre)) {
            return Pair(ResultadoAccion.ERROR, "Recompensa no disponible para el grupo")
        }

        if (usuario.recompensasReclamadas.contains(recompensaNombre)) {
            return Pair(ResultadoAccion.ERROR, "Recompensa ya reclamada por el usuario")
        }

        usuario.recompensasReclamadas.add(recompensaNombre)
        return Pair(ResultadoAccion.EXITOSO, null)
    }

    fun obtenerRecompensasDisponibles(usuarioId: String): List<String> {
        val usuario = usuariosRepository.find { it.id == usuarioId } ?: return emptyList()
        val grupoId = usuario.grupoId ?: return emptyList()
        val grupo = gruposRepository.find { it.id == grupoId } ?: return emptyList()
        return grupo.recompensasDisponibles
    }
}
