package com.ecosense.service

import com.ecosense.model.Grupo
import com.ecosense.model.RecyclingRequest
import com.ecosense.model.Usuario

data class IntegrationEvent(
    val type: String,
    val aggregateId: String,
    val attributes: Map<String, String> = emptyMap()
)

interface EventPublisher {
    fun publish(event: IntegrationEvent)
}

interface GrupoRepositoryPort {
    fun findById(id: String): Grupo?
    fun findByName(nombre: String): Grupo?
    fun save(grupo: Grupo)
    fun findAll(): List<Grupo>
}

interface UsuarioRepositoryPort {
    fun findById(id: String): Usuario?
    fun save(usuario: Usuario)
    fun findAll(): List<Usuario>
    fun appendRecyclingHistory(userId: String, requestId: String)
    fun addPoints(userId: String, points: Int)
}

interface RecyclingRequestRepositoryPort {
    fun nextId(): String
    fun save(request: RecyclingRequest)
    fun findById(id: String): RecyclingRequest?
    fun findByUserId(userId: String): List<RecyclingRequest>
}

interface ImageStorageClient {
    fun upload(bytes: ByteArray): String
}
