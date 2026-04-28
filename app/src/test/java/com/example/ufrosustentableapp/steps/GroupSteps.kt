package com.example.ufrosustentableapp.steps

import com.example.ufrosustentableapp.domain.*
import io.cucumber.datatable.DataTable
import io.cucumber.java.es.*
import org.junit.Assert.*

class GroupSteps {

    private val grupos = mutableListOf<Grupo>()
    private val usuarios = mutableListOf<Usuario>()
    private var resultadoAccion: Pair<ResultadoAccion, String?>? = null
    private var rankingGlobal: List<Grupo> = emptyList()
    private var rankingInterno: List<Usuario> = emptyList()
    private var recompensasList: List<String> = emptyList()

    // --- SHARED SETUP STEPS ---

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
                    puntajeTotal = (row["puntajeTotal"] as String).toInt(),
                    capacidad = (row["capacidad"] ?: "100").toInt()
                )
            )
        }
    }

    @Dado("que existen los siguientes usuarios:")
    fun que_existen_los_siguientes_usuarios(dataTable: DataTable) {
        val rows = dataTable.asMaps(String::class.java, String::class.java)
        for (row in rows) {
            usuarios.add(
                Usuario(
                    id = row["id"] as String,
                    email = row["email"] as String,
                    grupoId = if (row["grupoId"] == "null") null else row["grupoId"] as String,
                    puntos = (row["puntos"] as String).toInt()
                )
            )
        }
    }

    @Dado("un sistema con los siguientes usuarios base:")
    fun un_sistema_con_usuarios_base(dataTable: DataTable) = que_existen_los_siguientes_usuarios(dataTable)

    @Dado("los siguientes grupos base:")
    fun los_siguientes_grupos_base(dataTable: DataTable) {
        val rows = dataTable.asMaps(String::class.java, String::class.java)
        for (row in rows) {
            val tipo = if (row["tipo"] == "PUBLICO") TipoGrupo.PUBLICO else TipoGrupo.PRIVADO
            val miembrosStr = row["miembros"] as? String ?: ""
            val adminsStr = row["admins"] as? String ?: ""
            grupos.add(
                Grupo(
                    id = row["id"] as String,
                    nombre = row["nombre"] as String,
                    tipo = tipo,
                    puntajeTotal = (row["puntajeTotal"] as String).toInt(),
                    miembros = if (miembrosStr.isNotBlank()) miembrosStr.split(",").toMutableList() else mutableListOf(),
                    admins = if (adminsStr.isNotBlank()) adminsStr.split(",").toMutableList() else mutableListOf()
                )
            )
        }
    }

    @Dado("los siguientes grupos con sus puntajes:")
    fun los_siguientes_grupos_con_puntajes(dataTable: DataTable) {
        val rows = dataTable.asMaps(String::class.java, String::class.java)
        for (row in rows) {
            val recompensasStr = row["recompensasDisponibles"] as? String ?: ""
            grupos.add(
                Grupo(
                    id = row["id"] as String,
                    nombre = row["nombre"] as String,
                    tipo = TipoGrupo.PUBLICO,
                    puntajeTotal = (row["puntajeTotal"] as String).toInt(),
                    recompensasDisponibles = if (recompensasStr.isNotBlank()) recompensasStr.split(",").toMutableList() else mutableListOf()
                )
            )
        }
    }

    // --- INDIVIDUAL ACTIONS ---

    @Dado("que el usuario {string} con email {string} no pertenece a ningún grupo y tiene {int} puntos")
    fun que_el_usuario_no_pertenece_a_grupo_con_puntos(id: String, email: String, puntos: Int) {
        usuarios.add(Usuario(id = id, email = email, grupoId = null, puntos = puntos))
    }

    @Dado("que el usuario {string} con email {string} no pertenece a ningún grupo")
    fun que_el_usuario_no_pertenece_a_grupo(id: String, email: String) {
        usuarios.add(Usuario(id = id, email = email, grupoId = null, puntos = 0))
    }

    @Dado("que el usuario {string} con email {string} ya pertenece al grupo {string} y tiene {int} puntos")
    fun que_el_usuario_ya_pertenece_a_grupo(id: String, email: String, grupoId: String, puntos: Int) {
        usuarios.add(Usuario(id = id, email = email, grupoId = grupoId, puntos = puntos))
    }

    @Dado("que el grupo {string} esta lleno")
    fun que_el_grupo_esta_lleno(grupoId: String) {
        val grupo = grupos.find { it.id == grupoId } ?: throw Exception("Grupo no encontrado")
        grupo.miembros.clear()
        for (i in 1..grupo.capacidad) {
            grupo.miembros.add("M$i")
        }
    }

    @Dado("que el usuario {string} obtiene {int} puntos nuevos")
    fun usuario_obtiene_puntos(usuarioId: String, puntos: Int) {
        val useCase = SumarPuntosUseCase(grupos, usuarios)
        resultadoAccion = useCase.sumarPuntos(usuarioId, puntos)
    }

    @Dado("(que )el grupo {string} ahora tiene la recompensa {string}")
    fun grupo_tiene_recompensa_manual(grupoId: String, recompensa: String) {
        val grupo = grupos.find { it.id == grupoId }
        if (grupo != null && !grupo.recompensasDisponibles.contains(recompensa)) {
            grupo.recompensasDisponibles.add(recompensa)
        }
    }

    // --- WHEN STEPS ---

    @Cuando("el usuario intenta unirse al grupo {string}")
    fun el_usuario_intenta_unirse_al_grupo(grupoId: String) {
        val usuario = usuarios.last()
        val useCase = UnirseGrupoUseCase(grupos, usuarios)
        resultadoAccion = useCase.unirse(usuario.id, grupoId)
    }

    @Cuando("el usuario {string} crea un grupo {string} de tipo {string}")
    fun el_usuario_crea_grupo(usuarioId: String, nombre: String, tipoStr: String) {
        val useCase = CrearGrupoUseCase(grupos, usuarios)
        val tipo = if (tipoStr == "PUBLICO") TipoGrupo.PUBLICO else TipoGrupo.PRIVADO
        resultadoAccion = useCase.crearGrupo(usuarioId, nombre, "", tipo)
    }

    @Cuando("el usuario {string} obtiene {int} puntos nuevos")
    fun usuario_obtiene_puntos_accion(usuarioId: String, puntos: Int) = usuario_obtiene_puntos(usuarioId, puntos)

    @Cuando("el administrador {string} elimina al miembro {string} del grupo {string}")
    fun admin_elimina_miembro(adminId: String, miembroId: String, grupoId: String) {
        val useCase = GestionarMiembrosUseCase(grupos, usuarios)
        resultadoAccion = useCase.eliminarMiembro(adminId, miembroId, grupoId)
    }

    @Cuando("el administrador {string} agrega al usuario {string} al grupo {string}")
    fun admin_agrega_miembro(adminId: String, usuarioId: String, grupoId: String) {
        val useCase = GestionarMiembrosUseCase(grupos, usuarios)
        resultadoAccion = useCase.agregarMiembro(adminId, usuarioId, grupoId)
    }

    @Cuando("el administrador {string} promueve al miembro {string} a administrador del grupo {string}")
    fun admin_promueve_miembro(adminId: String, miembroId: String, grupoId: String) {
        val useCase = GestionarMiembrosUseCase(grupos, usuarios)
        resultadoAccion = useCase.asignarRolAdmin(adminId, miembroId, grupoId)
    }

    @Cuando("consulto el ranking global de grupos")
    fun consulto_ranking_global() {
        val useCase = RankingGruposUseCase(grupos, usuarios)
        rankingGlobal = useCase.obtenerRankingGlobalGrupos()
    }

    @Cuando("consulto el ranking interno del grupo {string}")
    fun consulto_ranking_interno(grupoId: String) {
        val useCase = RankingGruposUseCase(grupos, usuarios)
        rankingInterno = useCase.obtenerRankingInternoGrupo(grupoId)
    }

    @Cuando("el usuario {string} intenta reclamar la recompensa grupal {string}")
    fun el_usuario_intenta_reclamar_recompensa(usuarioId: String, recompensa: String) {
        val useCase = RecompensasGrupalesUseCase(grupos, usuarios)
        resultadoAccion = useCase.reclamarRecompensa(usuarioId, recompensa)
    }

    @Cuando("el usuario {string} consulta las recompensas disponibles de su grupo")
    fun usuario_consulta_recompensas(usuarioId: String) {
        val useCase = RecompensasGrupalesUseCase(grupos, usuarios)
        recompensasList = useCase.obtenerRecompensasDisponibles(usuarioId)
    }

    // --- THEN STEPS ---

    @Entonces("el resultado debe ser Exitoso")
    fun el_resultado_debe_ser_exitoso() {
        assertEquals(ResultadoAccion.EXITOSO, resultadoAccion?.first)
    }

    @Entonces("la accion debe ser exitosa")
    fun la_accion_debe_ser_exitosa() = el_resultado_debe_ser_exitoso()

    @Entonces("el resultado debe ser un Error")
    fun el_resultado_debe_ser_error() {
        assertEquals(ResultadoAccion.ERROR, resultadoAccion?.first)
    }

    @Entonces("el mensaje de error debe ser {string}")
    fun el_mensaje_de_error_debe_ser(mensajeEsperado: String) {
        assertEquals(mensajeEsperado, resultadoAccion?.second)
    }

    @Entonces("el usuario debe pertenecer al grupo {string}")
    fun el_usuario_debe_pertenecer_al_grupo(grupoId: String) {
        val usuario = usuarios.last()
        assertEquals(grupoId, usuario.grupoId)
    }

    @Entonces("el usuario {string} ya no pertenece al grupo {string}")
    fun usuario_no_pertenece_a_grupo(usuarioId: String, grupoId: String) {
        val grupo = grupos.find { it.id == grupoId }
        assertFalse(group_contains_member(grupo, usuarioId))
        val usuario = usuarios.find { it.id == usuarioId }
        assertNotEquals(grupoId, usuario?.grupoId)
    }

    private fun group_contains_member(grupo: Grupo?, memberId: String): Boolean {
        return grupo?.miembros?.contains(memberId) ?: false
    }

    @Entonces("el puntaje total del grupo {string} debe ser {int}")
    fun el_puntaje_total_del_grupo_debe_ser(grupoId: String, puntajeEsperado: Int) {
        val grupo = grupos.find { it.id == grupoId }
        assertEquals(puntajeEsperado, grupo?.puntajeTotal)
    }

    @Entonces("el grupo {string} debe tener {int} puntos en total")
    fun el_grupo_debe_tener_puntos_en_total(grupoId: String, puntos: Int) = el_puntaje_total_del_grupo_debe_ser(grupoId, puntos)

    @Entonces("el usuario {string} debe ser administrador de su grupo")
    fun usuario_es_administrador(usuarioId: String) {
        val usuario = usuarios.find { it.id == usuarioId }
        assertNotNull(usuario?.grupoId)
        val grupo = grupos.find { it.id == usuario?.grupoId }
        assertTrue(grupo?.admins?.contains(usuarioId) == true)
    }

    @Entonces("el grupo {string} debe tener la recompensa {string}")
    fun grupo_tiene_recompensa(grupoId: String, recompensa: String) {
        val grupo = grupos.find { it.id == grupoId }
        assertTrue(grupo?.recompensasDisponibles?.contains(recompensa) == true)
    }

    @Entonces("el grupo {string} debe estar en la posicion {int}")
    fun grupo_en_posicion(grupoId: String, posicion: Int) {
        assertEquals(grupoId, rankingGlobal[posicion - 1].id)
    }

    @Entonces("el usuario {string} debe estar en la posicion {int}")
    fun usuario_en_posicion(usuarioId: String, posicion: Int) {
        assertEquals(usuarioId, rankingInterno[posicion - 1].id)
    }

    @Entonces("el usuario {string} debe tener la recompensa {string} en su perfil")
    fun usuario_tiene_recompensa(usuarioId: String, recompensa: String) {
        val usuario = usuarios.find { it.id == usuarioId }
        assertTrue(usuario?.recompensasReclamadas?.contains(recompensa) == true)
    }

    @Entonces("debe ver la recompensa {string} en la lista")
    fun debe_ver_recompensa_en_lista(recompensa: String) {
        assertTrue(recompensasList.contains(recompensa))
    }
}
