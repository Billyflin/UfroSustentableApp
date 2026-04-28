package steps

import domain.Grupo
import domain.GrupoService
import domain.ResultadoUnion
import domain.TipoGrupo
import domain.Usuario
import io.cucumber.datatable.DataTable
import io.cucumber.java.es.Cuando
import io.cucumber.java.es.Dado
import io.cucumber.java.es.Entonces
import io.cucumber.java.es.Y
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class UnirseGrupoSteps {

    private lateinit var servicio: GrupoService
    private lateinit var usuario: Usuario
    private var resultado: ResultadoUnion? = null

    @Dado("que existen los siguientes grupos disponibles:")
    fun queExistenGruposDisponibles(tabla: DataTable) {
        servicio = GrupoService()
        tabla.asMaps().forEach { fila ->
            servicio.agregarGrupo(Grupo(
                id           = fila["id"]!!,
                nombre       = fila["nombre"]!!,
                tipo         = TipoGrupo.valueOf(fila["tipo"]!!),
                puntajeTotal = fila["puntajeTotal"]!!.toInt()
            ))
        }
    }

    @Dado("que el usuario {string} con email {string} no pertenece a ningún grupo y tiene {int} puntos")
    fun usuarioSinGrupo(id: String, email: String, puntos: Int) {
        usuario = Usuario(id = id, email = email, nombre = id, puntos = puntos, grupoId = null)
    }

    @Dado("que el usuario {string} con email {string} ya pertenece al grupo {string} y tiene {int} puntos")
    fun usuarioConGrupo(id: String, email: String, grupoId: String, puntos: Int) {
        usuario = Usuario(id = id, email = email, nombre = id, puntos = puntos, grupoId = grupoId)
    }

    @Cuando("el usuario intenta unirse al grupo {string}")
    fun usuarioIntentaUnirse(grupoId: String) {
        resultado = servicio.unirseAGrupo(usuario, grupoId)
    }

    @Entonces("el resultado debe ser Exitoso")
    fun resultadoExitoso() { assertIs<ResultadoUnion.Exitoso>(resultado) }

    @Entonces("el resultado debe ser Pendiente")
    fun resultadoPendiente() { assertIs<ResultadoUnion.Pendiente>(resultado) }

    @Entonces("el resultado debe ser un Error")
    fun resultadoError() { assertIs<ResultadoUnion.Error>(resultado) }

    @Y("el usuario debe pertenecer al grupo {string}")
    fun usuarioDebePertenecerAlGrupo(grupoId: String) {
        assertEquals(grupoId, usuario.grupoId)
    }

    @Y("el usuario no debe pertenecer a ningún grupo")
    fun usuarioNoDebePertenecerANingunGrupo() {
        assertEquals(null, usuario.grupoId)
    }

    @Y("el puntaje total del grupo {string} debe ser {int}")
    fun puntajeTotalDelGrupo(grupoId: String, puntajeEsperado: Int) {
        val grupo = servicio.obtenerGruposDisponibles().find { it.id == grupoId }
        assertNotNull(grupo)
        assertEquals(puntajeEsperado, grupo.puntajeTotal)
    }

    @Y("el mensaje de error debe ser {string}")
    fun mensajeDeError(mensajeEsperado: String) {
        val error = assertIs<ResultadoUnion.Error>(resultado)
        assertEquals(mensajeEsperado, error.mensaje)
    }
}
