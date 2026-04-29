package com.ecosense.viewmodel

import androidx.lifecycle.ViewModel
import com.ecosense.model.Grupo
import com.ecosense.model.TipoGrupo
import com.ecosense.model.Usuario
import com.ecosense.service.EntradaRanking
import com.ecosense.service.EntradaRankingGrupal
import com.ecosense.service.RankingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class RankingUiState(
    val rankingUsuarios: List<EntradaRanking>    = emptyList(),
    val rankingGrupos:   List<EntradaRankingGrupal> = emptyList()
)

class RankingViewModel : ViewModel() {

    private val rankingService = RankingService()

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState

    init { cargarDatos() }

    private fun cargarDatos() {
        // Demo: usuarios con puntos de reciclaje acumulados.
        // TODO: reemplazar con consulta a Firestore usuarios/{uid}.puntos
        val usuarios = listOf(
            Usuario("U01", "ana@ufro.cl",    "Ana González",   puntos = 240),
            Usuario("U02", "carlos@ufro.cl", "Carlos Muñoz",   puntos = 195),
            Usuario("U03", "lucia@ufro.cl",  "Lucía Rojas",    puntos = 170),
            Usuario("U04", "pedro@ufro.cl",  "Pedro Silva",    puntos = 150),
            Usuario("U05", "maria@ufro.cl",  "María Torres",   puntos = 130),
            Usuario("U06", "jose@ufro.cl",   "José Vargas",    puntos = 115),
            Usuario("U07", "carmen@ufro.cl", "Carmen Díaz",    puntos = 90),
            Usuario("U08", "miguel@ufro.cl", "Miguel Reyes",   puntos = 75),
            Usuario("U09", "sofia@ufro.cl",  "Sofía Pérez",    puntos = 60),
            Usuario("U10", "diego@ufro.cl",  "Diego Mora",     puntos = 45),
        )

        // Los grupos se leen desde GrupoState para reflejar grupos creados en sesión
        val grupos: List<Grupo> = GrupoState.grupoIds
            .mapNotNull { GrupoState.service.obtenerGrupo(it) }
            .ifEmpty {
                // Fallback si se entra al ranking sin pasar por GruposScreen
                listOf(
                    Grupo("G001", "EcoVerde UFRO",     TipoGrupo.PUBLICO, puntajeTotal = 340),
                    Grupo("G002", "Recicladores Sur",  TipoGrupo.PUBLICO, puntajeTotal = 210),
                    Grupo("G003", "Sustentables FCFM", TipoGrupo.PRIVADO, puntajeTotal = 180),
                    Grupo("G004", "Club Verde",        TipoGrupo.PUBLICO, puntajeTotal = 95),
                )
            }

        _uiState.value = RankingUiState(
            rankingUsuarios = rankingService.obtenerRankingGlobal(usuarios),
            rankingGrupos   = rankingService.obtenerRankingGrupal(grupos)
        )
    }
}
