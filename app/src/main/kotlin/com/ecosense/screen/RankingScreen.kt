package com.ecosense.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ecosense.service.EntradaRanking
import com.ecosense.service.EntradaRankingGrupal
import com.ecosense.viewmodel.RankingViewModel

@Composable
fun RankingScreen(
    navController: NavHostController,
    viewModel: RankingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    var tabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Header ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Icon(
                imageVector = Icons.Default.Leaderboard,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Ranking", style = MaterialTheme.typography.headlineSmall)
        }

        // ── Tabs ────────────────────────────────────────────────────────────
        PrimaryTabRow(
            selectedTabIndex = tabIndex,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = tabIndex == 0,
                onClick = { tabIndex = 0 },
                icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                text = { Text("Usuarios") }
            )
            Tab(
                selected = tabIndex == 1,
                onClick = { tabIndex = 1 },
                icon = { Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp)) },
                text = { Text("Grupos") }
            )
        }

        Spacer(Modifier.height(8.dp))

        when (tabIndex) {
            0 -> RankingUsuariosTab(uiState.rankingUsuarios)
            1 -> RankingGruposTab(uiState.rankingGrupos)
        }
    }
}

// ── Tab: usuarios ─────────────────────────────────────────────────────────────

@Composable
private fun RankingUsuariosTab(ranking: List<EntradaRanking>) {
    val colorScheme = MaterialTheme.colorScheme

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Podio top-3
        if (ranking.size >= 3) {
            item {
                PodioUsuarios(
                    primero  = ranking[0],
                    segundo  = ranking[1],
                    tercero  = ranking[2]
                )
            }
        }

        // Resto del ranking
        val resto = if (ranking.size > 3) ranking.drop(3) else emptyList()
        itemsIndexed(resto, key = { _, e -> e.usuario.id }) { _, entrada ->
            Surface(
                shape = MaterialTheme.shapes.large,
                color = colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${entrada.posicion}",
                        style = MaterialTheme.typography.titleSmall,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(40.dp)
                    )
                    Icon(Icons.Default.Person, contentDescription = null,
                        tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = entrada.usuario.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${entrada.usuario.puntos} pts",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.primary
                    )
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun PodioUsuarios(
    primero: EntradaRanking,
    segundo: EntradaRanking,
    tercero: EntradaRanking
) {
    val colorScheme = MaterialTheme.colorScheme
    ElevatedCard(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Top 3",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                PodioItem("🥈", segundo.usuario.nombre,  segundo.usuario.puntos, height = 70.dp)
                PodioItem("🥇", primero.usuario.nombre,  primero.usuario.puntos, height = 90.dp, isFirst = true)
                PodioItem("🥉", tercero.usuario.nombre,  tercero.usuario.puntos, height = 55.dp)
            }
        }
    }
}

@Composable
private fun PodioItem(
    medal: String,
    nombre: String,
    puntos: Int,
    height: androidx.compose.ui.unit.Dp,
    isFirst: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Text(medal, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = nombre.split(" ").first(), // Solo primer nombre
            style = if (isFirst) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodySmall,
            fontWeight = if (isFirst) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            modifier = Modifier.padding(vertical = 2.dp)
        )
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (isFirst) colorScheme.primaryContainer else colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$puntos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isFirst) colorScheme.onPrimaryContainer else colorScheme.onSurface
                )
            }
        }
    }
}

// ── Tab: grupos ───────────────────────────────────────────────────────────────

@Composable
private fun RankingGruposTab(ranking: List<EntradaRankingGrupal>) {
    val colorScheme = MaterialTheme.colorScheme

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Podio top-3 de grupos
        if (ranking.size >= 3) {
            item {
                ElevatedCard(
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Top grupos", style = MaterialTheme.typography.labelLarge,
                            color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom) {
                            PodioItem("🥈", ranking[1].grupo.nombre, ranking[1].grupo.puntajeTotal, 70.dp)
                            PodioItem("🥇", ranking[0].grupo.nombre, ranking[0].grupo.puntajeTotal, 90.dp, true)
                            PodioItem("🥉", ranking[2].grupo.nombre, ranking[2].grupo.puntajeTotal, 55.dp)
                        }
                    }
                }
            }
        }

        val resto = if (ranking.size > 3) ranking.drop(3) else emptyList()
        itemsIndexed(resto, key = { _, e -> e.grupo.id }) { _, entrada ->
            Surface(
                shape = MaterialTheme.shapes.large,
                color = colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#${entrada.posicion}", style = MaterialTheme.typography.titleSmall,
                        color = colorScheme.onSurfaceVariant, modifier = Modifier.width(40.dp))
                    Icon(Icons.Default.Groups, contentDescription = null,
                        tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(entrada.grupo.nombre, style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null,
                            tint = colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("${entrada.grupo.puntajeTotal}", style = MaterialTheme.typography.labelLarge,
                            color = colorScheme.primary)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
