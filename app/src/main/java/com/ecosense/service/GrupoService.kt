package com.ecosense.service

import com.ecosense.model.Grupo
import com.ecosense.model.MiembroGrupo
import com.ecosense.model.RolMiembro
import com.ecosense.model.TipoGrupo
import com.ecosense.model.Usuario

sealed class ResultadoCreacion {
    data class Exitoso(val grupo: Grupo) : ResultadoCreacion()
    data class Error(val mensaje: String) : ResultadoCreacion()
}

sealed class ResultadoUnion {
    data class Exitoso(val grupo: Grupo) : ResultadoUnion()
    data class Pendiente(val grupoId: String) : ResultadoUnion()
    data class Error(val mensaje: String) : ResultadoUnion()
}

sealed class ResultadoGestion {
    object Exitoso : ResultadoGestion()
    data class Error(val mensaje: String) : ResultadoGestion()
}

enum class AccionGestion { AGREGAR, ELIMINAR }

data class RecompensaGrupal(val grupoId: String, val descripcion: String)

data class ProgresoGrupo(
    val puntajeActual: Int,
    val metaPuntaje: Int,
    val puntajeRestante: Int
)

class GrupoService {
    private val grupos = mutableMapOf<String, Grupo>()
    private val solicitudesPendientes = mutableListOf<Pair<String, String>>()
    private var nextId = 1

    fun agregarGrupo(grupo: Grupo) { grupos[grupo.id] = grupo }
    fun obtenerGruposDisponibles(): List<Grupo> = grupos.values.toList()
    fun obtenerGrupo(grupoId: String): Grupo? = grupos[grupoId]

    fun crearGrupo(
        creadorId: String,
        nombre: String,
        descripcion: String = "",
        tipo: TipoGrupo = TipoGrupo.PUBLICO
    ): ResultadoCreacion {
        if (nombre.isBlank()) return ResultadoCreacion.Error("El nombre del grupo no puede estar vacío")
        val id = "G${nextId++}"
        val grupo = Grupo(
            id = id,
            nombre = nombre.trim(),
            descripcion = descripcion.trim(),
            tipo = tipo,
            miembros = mutableListOf(MiembroGrupo(creadorId, RolMiembro.ADMINISTRADOR))
        )
        grupos[id] = grupo
        return ResultadoCreacion.Exitoso(grupo)
    }

    fun unirseAGrupo(usuario: Usuario, grupoId: String): ResultadoUnion {
        val grupo = grupos[grupoId] ?: return ResultadoUnion.Error("El grupo no existe")
        if (usuario.grupoId == grupoId) return ResultadoUnion.Error("El usuario ya pertenece a este grupo")
        if (usuario.grupoId != null) return ResultadoUnion.Error("El usuario ya pertenece a un grupo")
        return when (grupo.tipo) {
            TipoGrupo.PRIVADO -> {
                solicitudesPendientes.add(Pair(usuario.id, grupoId))
                ResultadoUnion.Pendiente(grupoId)
            }
            TipoGrupo.PUBLICO -> {
                grupo.miembros.add(MiembroGrupo(usuario.id))
                grupo.puntajeTotal += usuario.puntos
                usuario.grupoId = grupoId
                ResultadoUnion.Exitoso(grupo)
            }
        }
    }

    fun agregarPuntosAlGrupo(usuario: Usuario, puntos: Int) {
        usuario.puntos += puntos
        val grupoId = usuario.grupoId ?: return
        grupos[grupoId]?.also { it.puntajeTotal += puntos }
    }

    fun verificarRecompensaGrupal(grupoId: String): RecompensaGrupal? {
        val grupo = grupos[grupoId] ?: return null
        return if (grupo.puntajeTotal >= grupo.metaPuntaje)
            RecompensaGrupal(grupoId, "¡Meta alcanzada! Recompensa grupal disponible.")
        else null
    }

    fun progresoHaciaRecompensa(grupoId: String): ProgresoGrupo? {
        val grupo = grupos[grupoId] ?: return null
        return ProgresoGrupo(
            puntajeActual = grupo.puntajeTotal,
            metaPuntaje = grupo.metaPuntaje,
            puntajeRestante = maxOf(0, grupo.metaPuntaje - grupo.puntajeTotal)
        )
    }

    fun gestionarMiembro(
        adminId: String,
        grupoId: String,
        accion: AccionGestion,
        usuarioId: String
    ): ResultadoGestion {
        val grupo = grupos[grupoId] ?: return ResultadoGestion.Error("El grupo no existe")
        val adminMiembro = grupo.miembros.find { it.usuarioId == adminId }
            ?: return ResultadoGestion.Error("No tienes permisos de administrador")
        if (adminMiembro.rol != RolMiembro.ADMINISTRADOR)
            return ResultadoGestion.Error("No tienes permisos de administrador")
        return when (accion) {
            AccionGestion.AGREGAR -> {
                if (grupo.miembros.any { it.usuarioId == usuarioId })
                    ResultadoGestion.Error("El usuario ya es miembro del grupo")
                else {
                    grupo.miembros.add(MiembroGrupo(usuarioId))
                    ResultadoGestion.Exitoso
                }
            }
            AccionGestion.ELIMINAR -> {
                val removed = grupo.miembros.removeIf { it.usuarioId == usuarioId }
                if (removed) ResultadoGestion.Exitoso
                else ResultadoGestion.Error("El usuario no es miembro del grupo")
            }
        }
    }
}


