package com.ecosense.service

import com.ecosense.model.Grupo
import com.ecosense.model.MiembroGrupo
import com.ecosense.model.RolMiembro
import com.ecosense.model.TipoGrupo
import com.ecosense.model.Usuario

/**
 * Servicio de dominio puro (sin Firebase) que gestiona grupos en memoria.
 * Cada instancia mantiene su propio mapa de grupos.
 */
class GrupoService {

    private val grupos = mutableMapOf<String, Grupo>()
    private var idCounter = 1

    // ── API pública ────────────────────────────────────────────────────────────

    fun agregarGrupo(grupo: Grupo) {
        grupos[grupo.id] = grupo
    }

    fun obtenerGrupo(id: String): Grupo? = grupos[id]

    // RF-12: Unirse a un grupo
    fun unirseAGrupo(usuario: Usuario, grupoId: String): ResultadoUnion {
        val grupo = grupos[grupoId]
            ?: return ResultadoUnion.Error("El grupo no existe")

        if (usuario.grupoId == grupoId)
            return ResultadoUnion.Error("El usuario ya pertenece a este grupo")

        if (usuario.grupoId != null)
            return ResultadoUnion.Error("El usuario ya pertenece a un grupo")

        return when (grupo.tipo) {
            TipoGrupo.PRIVADO -> ResultadoUnion.Pendiente(grupoId)
            TipoGrupo.PUBLICO -> {
                grupo.miembros.add(MiembroGrupo(usuario.id))
                grupo.puntajeTotal += usuario.puntos
                usuario.grupoId = grupoId
                ResultadoUnion.Exitoso(grupo)
            }
        }
    }

    // RF-11: Crear grupo
    fun crearGrupo(
        creadorId: String,
        nombre: String,
        descripcion: String,
        tipo: TipoGrupo = TipoGrupo.PUBLICO
    ): ResultadoCreacion {
        if (nombre.isBlank())
            return ResultadoCreacion.Error("El nombre del grupo no puede estar vacío")

        val id = "G${idCounter++}"
        val grupo = Grupo(
            id       = id,
            nombre   = nombre,
            tipo     = tipo,
            miembros = mutableListOf(MiembroGrupo(creadorId, RolMiembro.ADMINISTRADOR))
        )
        grupos[id] = grupo
        return ResultadoCreacion.Exitoso(grupo)
    }

    // RF-14: Sumar puntos al grupo
    fun agregarPuntosAlGrupo(usuario: Usuario, puntos: Int) {
        usuario.puntos += puntos
        usuario.grupoId?.let { gid ->
            grupos[gid]?.let { it.puntajeTotal += puntos }
        }
    }

    // RF-15: Recompensa grupal
    fun verificarRecompensaGrupal(grupoId: String): RecompensaGrupal? {
        val grupo = grupos[grupoId] ?: return null
        return if (grupo.puntajeTotal >= grupo.metaPuntaje) RecompensaGrupal(grupoId) else null
    }

    fun progresoHaciaRecompensa(grupoId: String): ProgresoRecompensa? {
        val grupo = grupos[grupoId] ?: return null
        return ProgresoRecompensa(
            puntajeActual = grupo.puntajeTotal,
            metaPuntaje   = grupo.metaPuntaje
        )
    }

    // RF-16: Gestión de miembros por administrador
    fun gestionarMiembro(
        adminId:   String,
        grupoId:   String,
        accion:    AccionGestion,
        usuarioId: String
    ): ResultadoGestion {
        val grupo = grupos[grupoId]
            ?: return ResultadoGestion.Error("Grupo no encontrado")

        val admin = grupo.miembros.find { it.usuarioId == adminId }
        if (admin?.rol != RolMiembro.ADMINISTRADOR)
            return ResultadoGestion.Error("No tienes permisos de administrador")

        return when (accion) {
            AccionGestion.AGREGAR -> {
                if (grupo.miembros.any { it.usuarioId == usuarioId })
                    return ResultadoGestion.Error("El usuario ya es miembro del grupo")
                grupo.miembros.add(MiembroGrupo(usuarioId))
                ResultadoGestion.Exitoso
            }
            AccionGestion.ELIMINAR -> {
                val miembro = grupo.miembros.find { it.usuarioId == usuarioId }
                    ?: return ResultadoGestion.Error("El usuario no es miembro del grupo")
                grupo.miembros.remove(miembro)
                ResultadoGestion.Exitoso
            }
        }
    }
}
