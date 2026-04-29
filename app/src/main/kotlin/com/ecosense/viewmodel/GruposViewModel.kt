package com.ecosense.viewmodel

import androidx.lifecycle.ViewModel
import com.ecosense.model.Grupo
import com.ecosense.model.MiembroGrupo
import com.ecosense.model.RolMiembro
import com.ecosense.model.TipoGrupo
import com.ecosense.model.Usuario
import com.ecosense.service.AccionGestion
import com.ecosense.service.EntradaRankingInterno
import com.ecosense.service.GrupoService
import com.ecosense.service.ProgresoRecompensa
import com.ecosense.service.RankingService
import com.ecosense.service.ResultadoCreacion
import com.ecosense.service.ResultadoGestion
import com.ecosense.service.ResultadoRankingGrupo
import com.ecosense.service.ResultadoUnion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ── UI State ──────────────────────────────────────────────────────────────────

sealed class GruposUiState {
    data object Loading : GruposUiState()
    data class SinGrupo(val grupos: List<Grupo>) : GruposUiState()
    data class EnGrupo(
        val grupo: Grupo,
        val esAdmin: Boolean,
        val progreso: ProgresoRecompensa?,
        val ranking: List<EntradaRankingInterno>
    ) : GruposUiState()
}

// ── Shared in-memory state (persiste durante la sesión) ───────────────────────

internal object GrupoState {
    val service = GrupoService().apply {
        agregarGrupo(Grupo("G001", "EcoVerde UFRO",     TipoGrupo.PUBLICO,  puntajeTotal = 340, metaPuntaje = 500,
            miembros = mutableListOf(MiembroGrupo("admin_g1", RolMiembro.ADMINISTRADOR), MiembroGrupo("u_lucia"))))
        agregarGrupo(Grupo("G002", "Recicladores Sur",  TipoGrupo.PUBLICO,  puntajeTotal = 210, metaPuntaje = 400,
            miembros = mutableListOf(MiembroGrupo("admin_g2", RolMiembro.ADMINISTRADOR))))
        agregarGrupo(Grupo("G003", "Sustentables FCFM", TipoGrupo.PRIVADO,  puntajeTotal = 180, metaPuntaje = 300,
            miembros = mutableListOf(MiembroGrupo("admin_g3", RolMiembro.ADMINISTRADOR))))
        agregarGrupo(Grupo("G004", "Club Verde",        TipoGrupo.PUBLICO,  puntajeTotal = 95,  metaPuntaje = 200,
            miembros = mutableListOf(MiembroGrupo("admin_g4", RolMiembro.ADMINISTRADOR))))
    }
    val rankingService = RankingService()
    var currentUser: Usuario? = null
    val grupoIds = mutableListOf("G001", "G002", "G003", "G004")
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class GruposViewModel : ViewModel() {

    private val _uiState  = MutableStateFlow<GruposUiState>(GruposUiState.Loading)
    val uiState: StateFlow<GruposUiState> = _uiState

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    // ── Inicialización ────────────────────────────────────────────────────────

    fun initialize(userId: String, nombre: String) {
        if (GrupoState.currentUser == null || GrupoState.currentUser?.id != userId) {
            GrupoState.currentUser = Usuario(id = userId, email = "", nombre = nombre, puntos = 50)
        }
        refreshState()
    }

    // ── Estado ────────────────────────────────────────────────────────────────

    private fun refreshState() {
        val user = GrupoState.currentUser ?: run { _uiState.value = GruposUiState.Loading; return }
        val grupoId = user.grupoId

        if (grupoId == null) {
            _uiState.value = GruposUiState.SinGrupo(
                grupos = GrupoState.grupoIds.mapNotNull { GrupoState.service.obtenerGrupo(it) }
            )
        } else {
            val grupo = GrupoState.service.obtenerGrupo(grupoId)
                ?: run { user.grupoId = null; refreshState(); return }
            val esAdmin = grupo.miembros.find { it.usuarioId == user.id }?.rol == RolMiembro.ADMINISTRADOR
            val progreso = GrupoState.service.progresoHaciaRecompensa(grupoId)
            val rankingResult = GrupoState.rankingService.obtenerRankingInterno(grupo, listOf(user))
            val ranking = (rankingResult as? ResultadoRankingGrupo.Exitoso)?.ranking ?: emptyList()
            _uiState.value = GruposUiState.EnGrupo(grupo, esAdmin, progreso, ranking)
        }
    }

    // ── Acciones ──────────────────────────────────────────────────────────────

    fun unirseAGrupo(grupoId: String) {
        val user = GrupoState.currentUser ?: return
        when (val res = GrupoState.service.unirseAGrupo(user, grupoId)) {
            is ResultadoUnion.Exitoso  -> { _mensaje.value = "¡Te uniste al grupo!"; refreshState() }
            is ResultadoUnion.Pendiente -> _mensaje.value = "Solicitud enviada. El administrador debe aprobarte."
            is ResultadoUnion.Error    -> _mensaje.value = res.mensaje
        }
    }

    fun crearGrupo(nombre: String, tipo: TipoGrupo) {
        val user = GrupoState.currentUser ?: return
        when (val res = GrupoState.service.crearGrupo(user.id, nombre, "", tipo)) {
            is ResultadoCreacion.Exitoso -> {
                GrupoState.grupoIds += res.grupo.id
                user.grupoId = res.grupo.id
                _mensaje.value = "¡Grupo \"${res.grupo.nombre}\" creado!"
                refreshState()
            }
            is ResultadoCreacion.Error -> _mensaje.value = res.mensaje
        }
    }

    fun gestionarMiembro(accion: AccionGestion, usuarioId: String) {
        val user    = GrupoState.currentUser ?: return
        val grupoId = user.grupoId ?: return
        when (val res = GrupoState.service.gestionarMiembro(user.id, grupoId, accion, usuarioId)) {
            is ResultadoGestion.Exitoso -> { _mensaje.value = "Operación exitosa"; refreshState() }
            is ResultadoGestion.Error   -> _mensaje.value = res.mensaje
        }
    }

    fun clearMensaje() { _mensaje.value = null }

    // Para que RankingViewModel pueda leer los grupos actualizados
    fun obtenerTodosGrupos(): List<Grupo> =
        GrupoState.grupoIds.mapNotNull { GrupoState.service.obtenerGrupo(it) }
}
