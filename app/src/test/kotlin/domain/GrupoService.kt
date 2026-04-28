package domain

sealed class ResultadoUnion {
    data class Exitoso(val grupo: Grupo) : ResultadoUnion()
    data class Pendiente(val grupoId: String) : ResultadoUnion()
    data class Error(val mensaje: String) : ResultadoUnion()
}

class GrupoService {
    private val grupos = mutableMapOf<String, Grupo>()
    private val solicitudesPendientes = mutableListOf<Pair<String, String>>()

    fun agregarGrupo(grupo: Grupo) { grupos[grupo.id] = grupo }
    fun obtenerGruposDisponibles(): List<Grupo> = grupos.values.toList()

    fun unirseAGrupo(usuario: Usuario, grupoId: String): ResultadoUnion {
        val grupo = grupos[grupoId]
            ?: return ResultadoUnion.Error("El grupo no existe")

        if (usuario.grupoId == grupoId)
            return ResultadoUnion.Error("El usuario ya pertenece a este grupo")

        if (usuario.grupoId != null)
            return ResultadoUnion.Error("El usuario ya pertenece a un grupo")

        return when (grupo.tipo) {
            TipoGrupo.PRIVADO -> {
                solicitudesPendientes.add(Pair(usuario.id, grupoId))
                ResultadoUnion.Pendiente(grupoId)
            }
            TipoGrupo.PUBLICO -> {
                grupo.miembros.add(usuario.id)
                grupo.puntajeTotal += usuario.puntos
                usuario.grupoId = grupoId
                ResultadoUnion.Exitoso(grupo)
            }
        }
    }
}
