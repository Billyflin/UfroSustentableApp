package com.ecosense.service

import com.ecosense.model.RecyclingRequest
import com.ecosense.model.RequestStatus
import java.util.Date

class RecyclingApplicationService(
    private val requests: RecyclingRequestRepositoryPort,
    private val usuarios: UsuarioRepositoryPort,
    private val storage: ImageStorageClient,
    private val events: EventPublisher,
    private val clock: () -> Date = { Date() }
) {
    fun submitRequest(
        userId: String,
        materialType: String,
        quantityKg: Double,
        photoBytes: ByteArray,
        description: String?
    ): Result<RecyclingRequest> = runCatching {
        usuarios.findById(userId)
            ?: throw NoSuchElementException("Usuario no encontrado")
        require(materialType.isNotBlank()) { "Tipo de material requerido" }
        require(quantityKg > 0) { "La cantidad debe ser mayor a cero" }

        val photoUrl = storage.upload(photoBytes)
        val now = clock()
        val request = RecyclingRequest(
            id = requests.nextId(),
            userId = userId,
            materialType = materialType,
            quantityKg = quantityKg,
            photoUrl = photoUrl,
            status = RequestStatus.PROCESSING,
            requestTime = now,
            updateTime = now,
            description = description.orEmpty(),
            reward = 0
        )

        requests.save(request)
        usuarios.appendRecyclingHistory(userId, request.id)
        events.publish(
            IntegrationEvent(
                type = "RECYCLING_VALIDATION_REQUESTED",
                aggregateId = request.id,
                attributes = mapOf("userId" to userId, "materialType" to materialType)
            )
        )

        request
    }

    fun redeemReward(requestId: String, userId: String, rewardPoints: Int): Result<Unit> = runCatching {
        val request = requests.findById(requestId)
            ?: throw NoSuchElementException("Solicitud no encontrada")
        usuarios.findById(userId)
            ?: throw NoSuchElementException("Usuario no encontrado")
        check(request.userId == userId) { "La solicitud no pertenece al usuario" }

        requests.save(
            request.copy(
                status = RequestStatus.REEDEMED,
                reward = rewardPoints,
                updateTime = clock()
            )
        )
        usuarios.addPoints(userId, rewardPoints)
        events.publish(
            IntegrationEvent(
                type = "REWARD_REDEEMED",
                aggregateId = requestId,
                attributes = mapOf("userId" to userId, "points" to rewardPoints.toString())
            )
        )
    }
}
