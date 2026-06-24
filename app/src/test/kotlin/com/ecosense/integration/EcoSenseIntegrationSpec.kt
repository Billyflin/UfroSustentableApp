package com.ecosense.integration

import com.ecosense.model.Grupo
import com.ecosense.model.RecyclingRequest
import com.ecosense.model.RequestStatus
import com.ecosense.model.TipoGrupo
import com.ecosense.model.Usuario
import com.ecosense.service.EventPublisher
import com.ecosense.service.GrupoApplicationService
import com.ecosense.service.GrupoRepositoryPort
import com.ecosense.service.ImageStorageClient
import com.ecosense.service.IntegrationEvent
import com.ecosense.service.RecyclingApplicationService
import com.ecosense.service.RecyclingRequestRepositoryPort
import com.ecosense.service.ResultadoCreacion
import com.ecosense.service.ResultadoRankingGrupo
import com.ecosense.service.ResultadoUnion
import com.ecosense.service.UsuarioRepositoryPort
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.Date

class EcoSenseIntegrationSpec : BehaviorSpec({

    Given("IT-01 creacion correcta de grupo desde servicio de aplicacion") {
        val usuarios = InMemoryUsuarioRepository(
            listOf(Usuario("U001", "ana@ufro.cl", "Ana"))
        )
        val grupos = InMemoryGrupoRepository()
        val events = RecordingEventPublisher()
        val service = GrupoApplicationService(grupos, usuarios, events)

        When("un usuario existente crea un grupo publico") {
            val result = service.crearGrupo(
                creadorId = "U001",
                nombre = "EcoLab",
                descripcion = "Grupo de reciclaje",
                tipo = TipoGrupo.PUBLICO
            )

            Then("el repositorio de grupos persiste el grupo con el creador como administrador") {
                val created = result.shouldBeInstanceOf<ResultadoCreacion.Exitoso>().grupo
                grupos.findById(created.id) shouldBe created
                grupos.findById(created.id)!!.miembros.first().usuarioId shouldBe "U001"
            }

            Then("el repositorio de usuarios queda enlazado al grupo y la cola recibe el evento") {
                val created = (result as ResultadoCreacion.Exitoso).grupo
                usuarios.findById("U001")!!.grupoId shouldBe created.id
                events.events.single().type shouldBe "GROUP_CREATED"
                events.events.single().aggregateId shouldBe created.id
            }
        }
    }

    Given("IT-02 conflicto al crear un grupo con nombre duplicado") {
        val usuarios = InMemoryUsuarioRepository(
            listOf(Usuario("U001", "ana@ufro.cl", "Ana"))
        )
        val grupos = InMemoryGrupoRepository(
            listOf(Grupo("G999", "EcoLab", TipoGrupo.PUBLICO))
        )
        val events = RecordingEventPublisher()
        val service = GrupoApplicationService(grupos, usuarios, events)

        When("el usuario intenta crear otro grupo con el mismo nombre") {
            val result = service.crearGrupo("U001", "EcoLab", "Duplicado", TipoGrupo.PUBLICO)

            Then("la integracion retorna error tipo 409 sin modificar la base in-memory") {
                val error = result.shouldBeInstanceOf<ResultadoCreacion.Error>()
                error.mensaje shouldBe "Ya existe un grupo con ese nombre"
                grupos.findAll() shouldHaveSize 1
                usuarios.findById("U001")!!.grupoId.shouldBeNull()
            }

            Then("no se publica ningun mensaje lateral") {
                events.events.shouldBeEmpty()
            }
        }
    }

    Given("IT-03 union correcta a grupo publico") {
        val usuarios = InMemoryUsuarioRepository(
            listOf(Usuario("U002", "carlos@ufro.cl", "Carlos", puntos = 30))
        )
        val grupos = InMemoryGrupoRepository(
            listOf(Grupo("G001", "EcoVerde", TipoGrupo.PUBLICO, puntajeTotal = 100))
        )
        val events = RecordingEventPublisher()
        val service = GrupoApplicationService(grupos, usuarios, events)

        When("el usuario sin grupo se une a un grupo publico existente") {
            val result = service.unirseAGrupo("U002", "G001")

            Then("usuario y grupo quedan actualizados en los repositorios") {
                result.shouldBeInstanceOf<ResultadoUnion.Exitoso>()
                usuarios.findById("U002")!!.grupoId shouldBe "G001"
                grupos.findById("G001")!!.miembros.map { it.usuarioId } shouldContain "U002"
                grupos.findById("G001")!!.puntajeTotal shouldBe 130
            }

            Then("se publica el evento GROUP_JOINED") {
                events.events.single().type shouldBe "GROUP_JOINED"
                events.events.single().attributes["userId"] shouldBe "U002"
            }
        }
    }

    Given("IT-04 error 404 al unirse a grupo inexistente") {
        val usuarios = InMemoryUsuarioRepository(
            listOf(Usuario("U002", "carlos@ufro.cl", "Carlos", puntos = 30))
        )
        val grupos = InMemoryGrupoRepository()
        val events = RecordingEventPublisher()
        val service = GrupoApplicationService(grupos, usuarios, events)

        When("se solicita union a un grupo que no esta en BD") {
            val result = service.unirseAGrupo("U002", "G404")

            Then("se informa el error y no cambia el estado del usuario") {
                val error = result.shouldBeInstanceOf<ResultadoUnion.Error>()
                error.mensaje shouldBe "El grupo no existe"
                usuarios.findById("U002")!!.grupoId.shouldBeNull()
                grupos.findAll().shouldBeEmpty()
            }

            Then("la cola no recibe mensajes") {
                events.events.shouldBeEmpty()
            }
        }
    }

    Given("IT-05 creacion correcta de solicitud de reciclaje") {
        val fixedDate = Date(1_711_234_567_000)
        val usuarios = InMemoryUsuarioRepository(
            listOf(Usuario("U010", "lucia@ufro.cl", "Lucia"))
        )
        val requests = InMemoryRecyclingRequestRepository()
        val storage = FakeStorageClient { bytes ->
            bytes.toList() shouldBe listOf(1.toByte(), 2.toByte(), 3.toByte())
            "https://storage.ecosense.test/REQ-1.jpg"
        }
        val events = RecordingEventPublisher()
        val service = RecyclingApplicationService(requests, usuarios, storage, events) { fixedDate }

        When("se sube la foto y se registra una solicitud valida") {
            val result = service.submitRequest(
                userId = "U010",
                materialType = "Plastico",
                quantityKg = 2.5,
                photoBytes = byteArrayOf(1, 2, 3),
                description = "Botellas PET"
            )

            Then("la solicitud queda en BD con estado PROCESSING") {
                val request = result.getOrThrow()
                requests.findById(request.id)!!.status shouldBe RequestStatus.PROCESSING
                requests.findById(request.id)!!.photoUrl shouldBe "https://storage.ecosense.test/REQ-1.jpg"
                requests.findById(request.id)!!.description shouldBe "Botellas PET"
            }

            Then("el historial del usuario y la cola de validacion quedan actualizados") {
                usuarios.recyclingHistory["U010"] shouldBe listOf("REQ-1")
                events.events.single().type shouldBe "RECYCLING_VALIDATION_REQUESTED"
                events.events.single().aggregateId shouldBe "REQ-1"
            }
        }
    }

    Given("IT-06 timeout del cliente storage al crear solicitud") {
        val usuarios = InMemoryUsuarioRepository(
            listOf(Usuario("U010", "lucia@ufro.cl", "Lucia"))
        )
        val requests = InMemoryRecyclingRequestRepository()
        val storage = FakeStorageClient {
            throw RuntimeException("timeout al subir imagen")
        }
        val events = RecordingEventPublisher()
        val service = RecyclingApplicationService(requests, usuarios, storage, events)

        When("el storage falla antes de guardar la solicitud") {
            val result = service.submitRequest(
                userId = "U010",
                materialType = "Vidrio",
                quantityKg = 1.2,
                photoBytes = byteArrayOf(9),
                description = null
            )

            Then("la operacion falla sin escrituras parciales en BD") {
                result.isFailure shouldBe true
                result.exceptionOrNull()!!.message shouldBe "timeout al subir imagen"
                storage.uploadCalls shouldBe 1
                requests.findByUserId("U010").shouldBeEmpty()
                usuarios.recyclingHistory["U010"].orEmpty().shouldBeEmpty()
            }

            Then("no se agenda el job de validacion") {
                events.events.shouldBeEmpty()
            }
        }
    }

    Given("IT-07 canje correcto de recompensa") {
        val fixedDate = Date(1_711_234_567_000)
        val usuarios = InMemoryUsuarioRepository(
            listOf(Usuario("U020", "pedro@ufro.cl", "Pedro", puntos = 10))
        )
        val requests = InMemoryRecyclingRequestRepository(
            listOf(
                RecyclingRequest(
                    id = "REQ-7",
                    userId = "U020",
                    materialType = "Metal",
                    quantityKg = 1.0,
                    photoUrl = "https://storage.ecosense.test/REQ-7.jpg",
                    status = RequestStatus.REWARD,
                    requestTime = fixedDate,
                    updateTime = fixedDate,
                    description = "Latas",
                    reward = 0
                )
            )
        )
        val events = RecordingEventPublisher()
        val service = RecyclingApplicationService(
            requests = requests,
            usuarios = usuarios,
            storage = FakeStorageClient { "unused" },
            events = events,
            clock = { fixedDate }
        )

        When("el usuario canjea los puntos pendientes de una solicitud") {
            val result = service.redeemReward("REQ-7", "U020", rewardPoints = 25)

            Then("la solicitud cambia a REEDEMED y los puntos del usuario aumentan") {
                result.isSuccess shouldBe true
                requests.findById("REQ-7")!!.status shouldBe RequestStatus.REEDEMED
                requests.findById("REQ-7")!!.reward shouldBe 25
                usuarios.findById("U020")!!.puntos shouldBe 35
            }

            Then("se publica el evento de recompensa canjeada") {
                events.events.single().type shouldBe "REWARD_REDEEMED"
                events.events.single().attributes["points"] shouldBe "25"
            }
        }
    }

    Given("IT-08 error 404 al canjear una recompensa inexistente") {
        val usuarios = InMemoryUsuarioRepository(
            listOf(Usuario("U020", "pedro@ufro.cl", "Pedro", puntos = 10))
        )
        val requests = InMemoryRecyclingRequestRepository()
        val events = RecordingEventPublisher()
        val service = RecyclingApplicationService(
            requests = requests,
            usuarios = usuarios,
            storage = FakeStorageClient { "unused" },
            events = events
        )

        When("se intenta canjear un request que no existe") {
            val result = service.redeemReward("REQ-404", "U020", rewardPoints = 25)

            Then("se retorna error sin modificar puntos ni publicar eventos") {
                result.isFailure shouldBe true
                result.exceptionOrNull()!!.message shouldBe "Solicitud no encontrada"
                usuarios.findById("U020")!!.puntos shouldBe 10
                events.events.shouldBeEmpty()
            }
        }
    }

    Given("IT-09 error al crear grupo con usuario inexistente") {
        val usuarios = InMemoryUsuarioRepository()
        val grupos = InMemoryGrupoRepository()
        val events = RecordingEventPublisher()
        val service = GrupoApplicationService(grupos, usuarios, events)

        When("se intenta crear un grupo con un creador que no existe") {
            val result = service.crearGrupo("U404", "EcoLab", "Grupo", TipoGrupo.PUBLICO)

            Then("se retorna error y no se persiste ningun grupo") {
                val error = result.shouldBeInstanceOf<ResultadoCreacion.Error>()
                error.mensaje shouldBe "Usuario no encontrado"
                grupos.findAll().shouldBeEmpty()
                events.events.shouldBeEmpty()
            }
        }
    }

    Given("IT-10 creacion de grupo evita colision de identificador") {
        val usuarios = InMemoryUsuarioRepository(
            listOf(Usuario("U001", "ana@ufro.cl", "Ana"))
        )
        val grupos = InMemoryGrupoRepository(
            listOf(Grupo("G1", "Existente", TipoGrupo.PUBLICO))
        )
        val events = RecordingEventPublisher()
        val service = GrupoApplicationService(grupos, usuarios, events)

        When("el primer id generado por dominio ya existe en el repositorio") {
            val result = service.crearGrupo("U001", "NuevoEco", "Grupo", TipoGrupo.PUBLICO)

            Then("el servicio genera el siguiente id disponible") {
                val grupo = result.shouldBeInstanceOf<ResultadoCreacion.Exitoso>().grupo
                grupo.id shouldBe "G2"
                grupos.findById("G2") shouldBe grupo
            }
        }
    }

    Given("IT-11 solicitud para unirse a grupo privado") {
        val usuarios = InMemoryUsuarioRepository(
            listOf(Usuario("U030", "sofia@ufro.cl", "Sofia", puntos = 12))
        )
        val grupos = InMemoryGrupoRepository(
            listOf(Grupo("G030", "Privado", TipoGrupo.PRIVADO, puntajeTotal = 80))
        )
        val events = RecordingEventPublisher()
        val service = GrupoApplicationService(grupos, usuarios, events)

        When("el usuario solicita entrar al grupo privado") {
            val result = service.unirseAGrupo("U030", "G030")

            Then("queda pendiente y se publica el evento de solicitud") {
                result.shouldBeInstanceOf<ResultadoUnion.Pendiente>()
                usuarios.findById("U030")!!.grupoId.shouldBeNull()
                events.events.single().type shouldBe "GROUP_JOIN_REQUESTED"
            }
        }
    }

    Given("IT-12 error de dominio al unirse a otro grupo") {
        val usuarios = InMemoryUsuarioRepository(
            listOf(Usuario("U031", "diego@ufro.cl", "Diego", grupoId = "G999"))
        )
        val grupos = InMemoryGrupoRepository(
            listOf(Grupo("G031", "EcoVerde", TipoGrupo.PUBLICO))
        )
        val events = RecordingEventPublisher()
        val service = GrupoApplicationService(grupos, usuarios, events)

        When("un usuario que ya tiene grupo intenta entrar a otro") {
            val result = service.unirseAGrupo("U031", "G031")

            Then("se retorna error sin publicar eventos") {
                val error = result.shouldBeInstanceOf<ResultadoUnion.Error>()
                error.mensaje shouldBe "El usuario ya pertenece a un grupo"
                events.events.shouldBeEmpty()
            }
        }
    }

    Given("IT-13 agregar puntos de reciclaje desbloquea recompensa grupal") {
        val usuarios = InMemoryUsuarioRepository(
            listOf(Usuario("U040", "nora@ufro.cl", "Nora", puntos = 5, grupoId = "G040"))
        )
        val grupos = InMemoryGrupoRepository(
            listOf(Grupo("G040", "MetaCerca", TipoGrupo.PUBLICO, puntajeTotal = 990, metaPuntaje = 1000))
        )
        val events = RecordingEventPublisher()
        val service = GrupoApplicationService(grupos, usuarios, events)

        When("se agregan puntos suficientes para llegar a la meta") {
            val result = service.agregarPuntosPorReciclaje("U040", 15)

            Then("usuario, grupo y eventos quedan actualizados") {
                result.isSuccess shouldBe true
                usuarios.findById("U040")!!.puntos shouldBe 20
                grupos.findById("G040")!!.puntajeTotal shouldBe 1005
                events.events.map { it.type } shouldContain "GROUP_REWARD_UNLOCKED"
                events.events.map { it.type } shouldContain "RECYCLING_POINTS_ADDED"
            }
        }
    }

    Given("IT-14 error al agregar puntos para usuario inexistente") {
        val usuarios = InMemoryUsuarioRepository()
        val grupos = InMemoryGrupoRepository()
        val events = RecordingEventPublisher()
        val service = GrupoApplicationService(grupos, usuarios, events)

        When("se intenta agregar puntos a un usuario no registrado") {
            val result = service.agregarPuntosPorReciclaje("U404", 10)

            Then("la operacion falla sin eventos") {
                result.isFailure shouldBe true
                result.exceptionOrNull()!!.message shouldBe "Usuario no encontrado"
                events.events.shouldBeEmpty()
            }
        }
    }

    Given("IT-15 ranking interno y grupal desde servicio de aplicacion") {
        val usuarios = InMemoryUsuarioRepository(
            listOf(
                Usuario("U050", "ana@ufro.cl", "Ana", puntos = 300),
                Usuario("U051", "bob@ufro.cl", "Bob", puntos = 100)
            )
        )
        val grupos = InMemoryGrupoRepository(
            listOf(
                Grupo(
                    id = "G050",
                    nombre = "EcoA",
                    tipo = TipoGrupo.PUBLICO,
                    puntajeTotal = 400,
                    miembros = mutableListOf(
                        com.ecosense.model.MiembroGrupo("U050"),
                        com.ecosense.model.MiembroGrupo("U051")
                    )
                ),
                Grupo("G051", "EcoB", TipoGrupo.PUBLICO, puntajeTotal = 900)
            )
        )
        val service = GrupoApplicationService(grupos, usuarios, RecordingEventPublisher())

        When("se consultan rankings desde la capa de aplicacion") {
            val interno = service.rankingInternoGrupo("G050")
            val grupal = service.rankingGrupal()
            val noEncontrado = service.rankingInternoGrupo("G404")

            Then("se retornan rankings ordenados y error controlado si falta el grupo") {
                interno.shouldBeInstanceOf<ResultadoRankingGrupo.Exitoso>().ranking.first().usuario.id shouldBe "U050"
                grupal.first().grupo.id shouldBe "G051"
                noEncontrado.shouldBeInstanceOf<ResultadoRankingGrupo.Error>().mensaje shouldBe "Grupo no encontrado"
            }
        }
    }

    Given("IT-16 validaciones de solicitud de reciclaje") {
        val usuarios = InMemoryUsuarioRepository(
            listOf(Usuario("U060", "lara@ufro.cl", "Lara"))
        )
        val requests = InMemoryRecyclingRequestRepository()
        val events = RecordingEventPublisher()
        val storage = FakeStorageClient { "unused" }
        val service = RecyclingApplicationService(requests, usuarios, storage, events)

        When("el material esta vacio o la cantidad no es positiva") {
            val materialVacio = service.submitRequest("U060", " ", 1.0, byteArrayOf(1), null)
            val cantidadInvalida = service.submitRequest("U060", "Plastico", 0.0, byteArrayOf(1), null)

            Then("ambas operaciones fallan antes de subir imagen") {
                materialVacio.isFailure shouldBe true
                materialVacio.exceptionOrNull()!!.message shouldBe "Tipo de material requerido"
                cantidadInvalida.isFailure shouldBe true
                cantidadInvalida.exceptionOrNull()!!.message shouldBe "La cantidad debe ser mayor a cero"
                storage.uploadCalls shouldBe 0
                requests.findByUserId("U060").shouldBeEmpty()
                events.events.shouldBeEmpty()
            }
        }
    }

    Given("IT-17 validaciones de canje de recompensa") {
        val fixedDate = Date(1_711_234_567_000)
        val usuarios = InMemoryUsuarioRepository(
            listOf(
                Usuario("U070", "raul@ufro.cl", "Raul"),
                Usuario("U071", "ines@ufro.cl", "Ines")
            )
        )
        val requests = InMemoryRecyclingRequestRepository(
            listOf(
                RecyclingRequest(
                    id = "REQ-70",
                    userId = "U070",
                    materialType = "Vidrio",
                    quantityKg = 2.0,
                    photoUrl = "https://storage.ecosense.test/REQ-70.jpg",
                    status = RequestStatus.REWARD,
                    requestTime = fixedDate,
                    updateTime = fixedDate,
                    description = "",
                    reward = 0
                )
            )
        )
        val events = RecordingEventPublisher()
        val service = RecyclingApplicationService(
            requests = requests,
            usuarios = usuarios,
            storage = FakeStorageClient { "unused" },
            events = events,
            clock = { fixedDate }
        )

        When("el usuario no existe o no es propietario de la solicitud") {
            val usuarioInexistente = service.redeemReward("REQ-70", "U404", 10)
            val propietarioIncorrecto = service.redeemReward("REQ-70", "U071", 10)

            Then("se bloquea el canje sin alterar estado") {
                usuarioInexistente.isFailure shouldBe true
                usuarioInexistente.exceptionOrNull()!!.message shouldBe "Usuario no encontrado"
                propietarioIncorrecto.isFailure shouldBe true
                propietarioIncorrecto.exceptionOrNull()!!.message shouldBe "La solicitud no pertenece al usuario"
                requests.findById("REQ-70")!!.status shouldBe RequestStatus.REWARD
                events.events.shouldBeEmpty()
            }
        }
    }
})

private class InMemoryGrupoRepository(
    initial: List<Grupo> = emptyList()
) : GrupoRepositoryPort {
    private val grupos = linkedMapOf<String, Grupo>()

    init {
        initial.forEach(::save)
    }

    override fun findById(id: String): Grupo? = grupos[id]

    override fun findByName(nombre: String): Grupo? =
        grupos.values.firstOrNull { it.nombre.equals(nombre, ignoreCase = true) }

    override fun save(grupo: Grupo) {
        grupos[grupo.id] = grupo
    }

    override fun findAll(): List<Grupo> = grupos.values.toList()
}

private class InMemoryUsuarioRepository(
    initial: List<Usuario> = emptyList()
) : UsuarioRepositoryPort {
    private val usuarios = linkedMapOf<String, Usuario>()
    val recyclingHistory = linkedMapOf<String, MutableList<String>>()

    init {
        initial.forEach(::save)
    }

    override fun findById(id: String): Usuario? = usuarios[id]

    override fun save(usuario: Usuario) {
        usuarios[usuario.id] = usuario
        recyclingHistory.putIfAbsent(usuario.id, mutableListOf())
    }

    override fun findAll(): List<Usuario> = usuarios.values.toList()

    override fun appendRecyclingHistory(userId: String, requestId: String) {
        requireNotNull(usuarios[userId]) { "Usuario no encontrado" }
        recyclingHistory.getOrPut(userId) { mutableListOf() }.add(requestId)
    }

    override fun addPoints(userId: String, points: Int) {
        val user = requireNotNull(usuarios[userId]) { "Usuario no encontrado" }
        user.puntos += points
    }
}

private class InMemoryRecyclingRequestRepository(
    initial: List<RecyclingRequest> = emptyList()
) : RecyclingRequestRepositoryPort {
    private var counter = 1
    private val requests = linkedMapOf<String, RecyclingRequest>()

    init {
        initial.forEach(::save)
    }

    override fun nextId(): String = "REQ-${counter++}"

    override fun save(request: RecyclingRequest) {
        requests[request.id] = request
    }

    override fun findById(id: String): RecyclingRequest? = requests[id]

    override fun findByUserId(userId: String): List<RecyclingRequest> =
        requests.values.filter { it.userId == userId }
}

private class RecordingEventPublisher : EventPublisher {
    val events = mutableListOf<IntegrationEvent>()

    override fun publish(event: IntegrationEvent) {
        events += event
    }
}

private class FakeStorageClient(
    private val uploader: (ByteArray) -> String
) : ImageStorageClient {
    var uploadCalls = 0

    override fun upload(bytes: ByteArray): String {
        uploadCalls++
        return uploader(bytes)
    }
}
