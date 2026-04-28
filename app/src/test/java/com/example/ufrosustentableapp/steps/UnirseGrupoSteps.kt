package com.example.ufrosustentableapp.steps
import com.example.ufrosustentableapp.domain.*
import io.cucumber.datatable.DataTable
import io.cucumber.java.es.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
class UnirseGrupoSteps {
    private val grupos = mutableListOf<Grupo>()
    private val usuarios = mutableListOf<Usuario>()
    private lateinit var useCase: UnirseGrupoUseCase
    private var resultado: Pair<ResultadoUnirse, String?>? = null
    @Dado("que existen los siguientes grupos disponibles:")
    fun que_existen_los_siguientes_grupos_disponibles(dataTable: DataTable) {
        val rows = dataTable.asMaps(String::class.java, String::class.java)
        for (row in rows) {
            val tipo = if (row["tipo"] == "PUBLICO") TipoGrupo.PUBLICO else TipoGrupo.PRIVADO
            grupos.add(
                Grupo(
                    id = row["id"] as String,
                    nombre = row["nombre"] as String,
                    tipo = tipo,
                    puntajeTotal = (row["puntajeTotal"] as String).toInt()
                )
            )
        }
        useCase = UnirseGrupoUseCase(grupos, usuarios)
    }
    @Dado("que el usuario {string} con email {string} no pertenece a ningún grupo y tiene {int} puntos")
    fun que_el_usuario_con_email_no_pertenece_a_ningun_grupo(id: String, email: String, puntos: Int) {
        usuarios.add(Usuario(id = id, email = email, grupoId = null, puntos = puntos))
    }
    @Dado("que el usuario {string} con email {string} ya pertenece al grupo {string} y tiene {int} puntos")
    fun que_el_usuario_con_email_ya_pertenece_a_grupo(id: String, email: String, grupoId: String, puntos: Int) {
        usuarios.add(Usuario(id = id, email = email, grupoId = grupoId, puntos = puntos))
    }
    @Cuando("el usuario intenta unirse al grupo {string}")
    fun el_usuario_intenta_unirse_al_grupo(grupoId: String) {
        val usuario = usuarios.last()
        resultado = useCase.unirse(usuario.id, grupoId)
    }
    @Entonces("el resultado debe ser Exitoso")
    fun el_resultado_debe_ser_exitoso() {
        assertEquals(ResultadoUnirse.EXITOSO, resultado?.first)
    }
    @Entonces("el resultado debe ser Pendiente")
    fun el_resultado_debe_ser_pendiente() {
        assertEquals(ResultadoUnirse.PENDIENTE, resultado?.first)
    }
    @Entonces("el resultado debe ser un Error")
    fun el_resultado_debe_ser_error() {
        assertEquals(ResultadoUnirse.ERROR, resultado?.first)
    }
    @Entonces("el usuario debe pertenecer al grupo {string}")
    fun el_usuario_debe_pertenecer_al_grupo(grupoId: String) {
        val usuario = usuarios.last()
        assertEquals(grupoId, usuario.grupoId)
    }
    @Entonces("el usuario no debe pertenecer a ningún grupo")
    fun el_usuario_no_debe_pertenecer_a_ningun_grupo() {
        val usuario = usuarios.last()
        assertNull(usuario.grupoId)
    }
    @Entonces("el puntaje total del grupo {string} debe ser {int}")
    fun el_puntaje_total_del_grupo_debe_ser(grupoId: String, puntajeEsperado: Int) {
        val grupo = grupos.find { it.id == grupoId }
        assertEquals(puntajeEsperado, grupo?.puntajeTotal)
    }
    @Entonces("el mensaje de error debe ser {string}")
    fun el_mensaje_de_error_debe_ser(mensajeEsperado: String) {
        assertEquals(mensajeEsperado, resultado?.second)
    }
}
