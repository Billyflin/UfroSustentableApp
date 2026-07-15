package steps

import com.ecosense.model.Grupo
import com.ecosense.model.MiembroGrupo
import com.ecosense.model.RolMiembro
import com.ecosense.model.TipoGrupo
import com.ecosense.model.Usuario
import com.ecosense.service.AccionGestion
import com.ecosense.service.EntradaRanking
import com.ecosense.service.EntradaRankingGrupal
import com.ecosense.service.EntradaRankingInterno
import com.ecosense.service.GrupoService
import com.ecosense.service.RankingService
import com.ecosense.service.RecompensaGrupal
import com.ecosense.service.ResultadoCreacion
import com.ecosense.service.ResultadoGestion
import com.ecosense.service.ResultadoRankingGrupo
import io.cucumber.datatable.DataTable
import io.cucumber.java.es.Cuando
import io.cucumber.java.es.Dado
import io.cucumber.java.es.Entonces
import io.cucumber.java.es.Y
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GruposRankingSteps {

    private val rankingService = RankingService()
    private lateinit var grupoService: GrupoService
    private lateinit var usuario: Usuario
    private var usuarios: List<Usuario> = emptyList()
    private var grupos: List<Grupo> = emptyList()
    private lateinit var grupoActual: Grupo
    private var resultadoCreacion: ResultadoCreacion? = null
    private var resultadoGestion: ResultadoGestion? = null
    private var recompensa: RecompensaGrupal? = null
    private var rankingGlobal: List<EntradaRanking> = emptyList()
    private var rankingInterno: List<EntradaRankingInterno> = emptyList()
    private var rankingGrupal: List<EntradaRankingGrupal> = emptyList()

    @Dado("un servicio de grupos nuevo para BDD")
    fun servicioDeGruposNuevo() {
        grupoService = GrupoService()
    }

    @Dado("los siguientes usuarios para ranking global:")
    fun usuariosParaRankingGlobal(tabla: DataTable) {
        usuarios = tabla.asMaps().map(::usuarioDesdeFila)
    }

    @Cuando("calculo el ranking global de usuarios")
    fun calculoRankingGlobal() {
        rankingGlobal = rankingService.obtenerRankingGlobal(usuarios)
    }

    @Entonces("el primer usuario del ranking global debe ser {string}")
    fun primerUsuarioRankingGlobal(usuarioId: String) {
        assertEquals(usuarioId, rankingGlobal.first().usuario.id)
    }

    @Y("la posicion global del usuario {string} debe ser {int}")
    fun posicionGlobalUsuario(usuarioId: String, posicion: Int) {
        assertEquals(posicion, rankingService.obtenerPosicionGlobal(usuarioId, usuarios))
    }

    @Cuando("el usuario {string} crea un grupo publico llamado {string}")
    fun usuarioCreaGrupoPublico(creadorId: String, nombre: String) {
        resultadoCreacion = grupoService.crearGrupo(
            creadorId = creadorId,
            nombre = nombre,
            descripcion = "Grupo BDD",
            tipo = TipoGrupo.PUBLICO
        )
    }

    @Entonces("el grupo se crea exitosamente con nombre {string}")
    fun grupoCreadoConNombre(nombre: String) {
        val grupo = assertIs<ResultadoCreacion.Exitoso>(resultadoCreacion).grupo
        assertEquals(nombre, grupo.nombre)
    }

    @Y("el usuario {string} queda como administrador del grupo creado")
    fun usuarioQuedaComoAdministrador(usuarioId: String) {
        val grupo = assertIs<ResultadoCreacion.Exitoso>(resultadoCreacion).grupo
        val miembro = grupo.miembros.single()
        assertEquals(usuarioId, miembro.usuarioId)
        assertEquals(RolMiembro.ADMINISTRADOR, miembro.rol)
    }

    @Dado("el grupo {string} con miembros para ranking interno:")
    fun grupoConMiembrosParaRankingInterno(grupoId: String, tabla: DataTable) {
        grupoActual = Grupo(
            id = grupoId,
            nombre = "EcoVerde",
            tipo = TipoGrupo.PUBLICO,
            miembros = tabla.asMaps().map { MiembroGrupo(it["usuarioId"]!!) }.toMutableList()
        )
    }

    @Y("los siguientes usuarios para ranking interno:")
    fun usuariosParaRankingInterno(tabla: DataTable) {
        usuarios = tabla.asMaps().map(::usuarioDesdeFila)
    }

    @Cuando("calculo el ranking interno del grupo")
    fun calculoRankingInterno() {
        val resultado = rankingService.obtenerRankingInterno(grupoActual, usuarios)
        rankingInterno = assertIs<ResultadoRankingGrupo.Exitoso>(resultado).ranking
    }

    @Entonces("el primer miembro del ranking interno debe ser {string}")
    fun primerMiembroRankingInterno(usuarioId: String) {
        assertEquals(usuarioId, rankingInterno.first().usuario.id)
    }

    @Y("la posicion global interna del usuario {string} debe ser {int}")
    fun posicionGlobalInterna(usuarioId: String, posicionGlobal: Int) {
        val entrada = rankingInterno.first { it.usuario.id == usuarioId }
        assertEquals(posicionGlobal, entrada.posicionGlobal)
    }

    @Dado("existe el grupo publico {string} con puntaje {int}")
    fun existeGrupoPublicoConPuntaje(grupoId: String, puntaje: Int) {
        grupoService.agregarGrupo(
            Grupo(id = grupoId, nombre = grupoId, tipo = TipoGrupo.PUBLICO, puntajeTotal = puntaje)
        )
    }

    @Y("el usuario {string} pertenece al grupo {string} con {int} puntos")
    fun usuarioPerteneceAlGrupoConPuntos(usuarioId: String, grupoId: String, puntos: Int) {
        usuario = Usuario(
            id = usuarioId,
            email = "$usuarioId@ufro.cl",
            nombre = usuarioId,
            puntos = puntos,
            grupoId = grupoId
        )
    }

    @Cuando("agrego {int} puntos sustentables al usuario")
    fun agregoPuntosSustentables(puntos: Int) {
        grupoService.agregarPuntosAlGrupo(usuario, puntos)
    }

    @Entonces("el usuario queda con {int} puntos")
    fun usuarioQuedaConPuntos(puntos: Int) {
        assertEquals(puntos, usuario.puntos)
    }

    @Y("el grupo {string} queda con {int} puntos")
    fun grupoQuedaConPuntos(grupoId: String, puntos: Int) {
        assertEquals(puntos, grupoService.obtenerGrupo(grupoId)?.puntajeTotal)
    }

    @Y("existe el grupo publico {string} con puntaje {int} y meta {int}")
    fun existeGrupoPublicoConPuntajeYMeta(grupoId: String, puntaje: Int, meta: Int) {
        grupoService.agregarGrupo(
            Grupo(
                id = grupoId,
                nombre = grupoId,
                tipo = TipoGrupo.PUBLICO,
                puntajeTotal = puntaje,
                metaPuntaje = meta
            )
        )
    }

    @Cuando("verifico recompensa grupal para {string}")
    fun verificoRecompensaGrupal(grupoId: String) {
        recompensa = grupoService.verificarRecompensaGrupal(grupoId)
    }

    @Entonces("la recompensa grupal debe estar disponible para {string}")
    fun recompensaGrupalDisponible(grupoId: String) {
        assertEquals(grupoId, assertNotNull(recompensa).grupoId)
    }

    @Y("existe el grupo {string} con administrador {string} y miembro {string}")
    fun existeGrupoConAdminYMiembro(grupoId: String, adminId: String, miembroId: String) {
        grupoService.agregarGrupo(
            Grupo(
                id = grupoId,
                nombre = grupoId,
                tipo = TipoGrupo.PUBLICO,
                miembros = mutableListOf(
                    MiembroGrupo(adminId, RolMiembro.ADMINISTRADOR),
                    MiembroGrupo(miembroId, RolMiembro.MIEMBRO)
                )
            )
        )
    }

    @Cuando("el administrador {string} agrega al usuario {string} en el grupo {string}")
    fun administradorAgregaUsuario(adminId: String, usuarioId: String, grupoId: String) {
        resultadoGestion = grupoService.gestionarMiembro(
            adminId = adminId,
            grupoId = grupoId,
            accion = AccionGestion.AGREGAR,
            usuarioId = usuarioId
        )
    }

    @Entonces("la gestion de miembros debe ser exitosa")
    fun gestionMiembrosExitosa() {
        assertEquals(ResultadoGestion.Exitoso, resultadoGestion)
    }

    @Y("el usuario {string} debe aparecer como miembro del grupo {string}")
    fun usuarioApareceComoMiembro(usuarioId: String, grupoId: String) {
        val grupo = assertNotNull(grupoService.obtenerGrupo(grupoId))
        assertTrue(grupo.miembros.any { it.usuarioId == usuarioId })
    }

    @Dado("los siguientes grupos para ranking grupal:")
    fun gruposParaRankingGrupal(tabla: DataTable) {
        grupos = tabla.asMaps().map {
            Grupo(
                id = it["id"]!!,
                nombre = it["nombre"]!!,
                tipo = TipoGrupo.PUBLICO,
                puntajeTotal = it["puntajeTotal"]!!.toInt()
            )
        }
    }

    @Cuando("calculo el ranking grupal")
    fun calculoRankingGrupal() {
        rankingGrupal = rankingService.obtenerRankingGrupal(grupos)
    }

    @Entonces("el primer grupo del ranking grupal debe ser {string}")
    fun primerGrupoRankingGrupal(grupoId: String) {
        assertEquals(grupoId, rankingGrupal.first().grupo.id)
    }

    @Y("la posicion del grupo {string} debe ser {int}")
    fun posicionDelGrupo(grupoId: String, posicion: Int) {
        assertEquals(posicion, rankingService.obtenerPosicionGrupo(grupoId, grupos))
    }

    private fun usuarioDesdeFila(fila: Map<String, String>): Usuario =
        Usuario(
            id = fila["id"]!!,
            email = fila["email"]!!,
            nombre = fila["nombre"]!!,
            puntos = fila["puntos"]!!.toInt()
        )
}
