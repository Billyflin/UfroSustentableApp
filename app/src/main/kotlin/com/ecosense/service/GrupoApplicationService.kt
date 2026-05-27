package com.ecosense.service

import com.ecosense.model.TipoGrupo

private const val USER_NOT_FOUND = "Usuario no encontrado"

class GrupoApplicationService(
    private val grupos: GrupoRepositoryPort,
    private val usuarios: UsuarioRepositoryPort,
    private val events: EventPublisher,
    private val rankingService: RankingService = RankingService()
) {
    fun crearGrupo(
        creadorId: String,
        nombre: String,
        descripcion: String,
        tipo: TipoGrupo = TipoGrupo.PUBLICO
    ): ResultadoCreacion {
        val creador = usuarios.findById(creadorId)
            ?: return ResultadoCreacion.Error(USER_NOT_FOUND)

        if (grupos.findByName(nombre) != null) {
            return ResultadoCreacion.Error("Ya existe un grupo con ese nombre")
        }

        val domain = GrupoService()
        var result = domain.crearGrupo(creadorId, nombre, descripcion, tipo)
        while (result is ResultadoCreacion.Exitoso && grupos.findById(result.grupo.id) != null) {
            result = domain.crearGrupo(creadorId, nombre, descripcion, tipo)
        }

        if (result is ResultadoCreacion.Exitoso) {
            creador.grupoId = result.grupo.id
            usuarios.save(creador)
            grupos.save(result.grupo)
            events.publish(
                IntegrationEvent(
                    type = "GROUP_CREATED",
                    aggregateId = result.grupo.id,
                    attributes = mapOf("creatorId" to creadorId, "type" to tipo.name)
                )
            )
        }
        return result
    }

    fun unirseAGrupo(usuarioId: String, grupoId: String): ResultadoUnion {
        val usuario = usuarios.findById(usuarioId)
            ?: return ResultadoUnion.Error(USER_NOT_FOUND)
        if (grupos.findById(grupoId) == null) {
            return ResultadoUnion.Error("El grupo no existe")
        }

        val domain = GrupoService().also { service ->
            grupos.findAll().forEach(service::agregarGrupo)
        }

        return when (val result = domain.unirseAGrupo(usuario, grupoId)) {
            is ResultadoUnion.Exitoso -> {
                usuarios.save(usuario)
                grupos.save(result.grupo)
                events.publish(
                    IntegrationEvent(
                        type = "GROUP_JOINED",
                        aggregateId = grupoId,
                        attributes = mapOf("userId" to usuarioId)
                    )
                )
                result
            }
            is ResultadoUnion.Pendiente -> {
                events.publish(
                    IntegrationEvent(
                        type = "GROUP_JOIN_REQUESTED",
                        aggregateId = grupoId,
                        attributes = mapOf("userId" to usuarioId)
                    )
                )
                result
            }
            is ResultadoUnion.Error -> result
        }
    }

    fun agregarPuntosPorReciclaje(usuarioId: String, puntos: Int): Result<Unit> = runCatching {
        val usuario = usuarios.findById(usuarioId)
            ?: throw NoSuchElementException(USER_NOT_FOUND)

        val domain = GrupoService().also { service ->
            grupos.findAll().forEach(service::agregarGrupo)
        }

        domain.agregarPuntosAlGrupo(usuario, puntos)
        usuarios.save(usuario)

        usuario.grupoId?.let { grupoId ->
            domain.obtenerGrupo(grupoId)?.let { grupoActualizado ->
                grupos.save(grupoActualizado)
                if (domain.verificarRecompensaGrupal(grupoId) != null) {
                    events.publish(
                        IntegrationEvent(
                            type = "GROUP_REWARD_UNLOCKED",
                            aggregateId = grupoId,
                            attributes = mapOf("userId" to usuarioId)
                        )
                    )
                }
            }
        }

        events.publish(
            IntegrationEvent(
                type = "RECYCLING_POINTS_ADDED",
                aggregateId = usuarioId,
                attributes = mapOf("points" to puntos.toString())
            )
        )
    }

    fun rankingInternoGrupo(grupoId: String): ResultadoRankingGrupo {
        val grupo = grupos.findById(grupoId)
            ?: return ResultadoRankingGrupo.Error("Grupo no encontrado")
        return rankingService.obtenerRankingInterno(grupo, usuarios.findAll())
    }

    fun rankingGrupal(): List<EntradaRankingGrupal> =
        rankingService.obtenerRankingGrupal(grupos.findAll())
}
