package com.ecosense.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ecosense.ScreenRanking
import com.ecosense.model.Grupo
import com.ecosense.model.TipoGrupo
import com.ecosense.service.EntradaRankingInterno
import com.ecosense.service.ProgresoRecompensa
import com.ecosense.viewmodel.GruposUiState
import com.ecosense.viewmodel.GruposViewModel

@Composable
fun GruposScreen(
    navController: NavHostController,
    userId: String,
    userName: String,
    viewModel: GruposViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCrearDialog by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(userId) { viewModel.initialize(userId, userName) }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMensaje()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Grupos",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { navController.navigate(ScreenRanking) }) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = "Ver ranking",
                        tint = colorScheme.primary
                    )
                }
            }

            // ── Contenido animado por estado ─────────────────────────────────
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                            scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow), initialScale = 0.97f) togetherWith
                            fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
                },
                label = "gruposState"
            ) { state ->
                when (state) {
                    is GruposUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colorScheme.primary)
                        }
                    }
                    is GruposUiState.SinGrupo -> SinGrupoContent(
                        grupos = state.grupos,
                        onCrearGrupo = { showCrearDialog = true },
                        onUnirse = { viewModel.unirseAGrupo(it) }
                    )
                    is GruposUiState.EnGrupo -> EnGrupoContent(
                        grupo = state.grupo,
                        esAdmin = state.esAdmin,
                        progreso = state.progreso,
                        ranking = state.ranking
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        )
    }

    if (showCrearDialog) {
        CrearGrupoDialog(
            onConfirm = { nombre, tipo -> viewModel.crearGrupo(nombre, tipo); showCrearDialog = false },
            onDismiss = { showCrearDialog = false }
        )
    }
}

// ── Sin grupo ──────────────────────────────────────────────────────────────────

@Composable
private fun SinGrupoContent(
    grupos: List<Grupo>,
    onCrearGrupo: () -> Unit,
    onUnirse: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card: no perteneces a ningún grupo
        item {
            ElevatedCard(
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Aún no perteneces a ningún grupo",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Crea el tuyo o únete a uno para sumar puntos grupales y desbloquear recompensas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                    )
                    Button(
                        onClick = onCrearGrupo,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Crear mi grupo")
                    }
                }
            }
        }

        // Título sección
        item {
            Text(
                text = "Grupos disponibles",
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Lista de grupos
        items(grupos, key = { it.id }) { grupo ->
            GrupoCard(grupo = grupo, onUnirse = { onUnirse(grupo.id) })
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun GrupoCard(grupo: Grupo, onUnirse: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    ElevatedCard(
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = grupo.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                TipoBadge(grupo.tipo)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null,
                        tint = colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${grupo.puntajeTotal} pts", style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null,
                        tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${grupo.miembros.size} miembro${if (grupo.miembros.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            FilledTonalButton(
                onClick = onUnirse,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (grupo.tipo == TipoGrupo.PRIVADO) "Solicitar ingreso" else "Unirse")
            }
        }
    }
}

// ── En grupo ───────────────────────────────────────────────────────────────────

@Composable
private fun EnGrupoContent(
    grupo: Grupo,
    esAdmin: Boolean,
    progreso: ProgresoRecompensa?,
    ranking: List<EntradaRankingInterno>
) {
    val colorScheme = MaterialTheme.colorScheme
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Miembros", "Ranking interno")

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Card del grupo ────────────────────────────────────────────────
        ElevatedCard(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = grupo.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    TipoBadge(grupo.tipo)
                }
                if (esAdmin) {
                    Text(
                        text = "Tú eres administrador",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))

                // Barra de progreso hacia la recompensa
                if (progreso != null && progreso.metaPuntaje < Int.MAX_VALUE) {
                    val fraction = (progreso.puntajeActual.toFloat() / progreso.metaPuntaje).coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${progreso.puntajeActual} pts",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.primary
                        )
                        Text(
                            text = "Meta: ${progreso.metaPuntaje} pts",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.fillMaxWidth(),
                        trackColor = colorScheme.surfaceVariant
                    )
                    if (fraction >= 1f) {
                        Text(
                            text = "🏆 ¡Recompensa grupal desbloqueada!",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.tertiary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    } else {
                        Text(
                            text = "Faltan ${progreso.puntajeRestante} pts para la recompensa",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null,
                            tint = colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${grupo.puntajeTotal} puntos grupales",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorScheme.primary
                        )
                    }
                }
            }
        }

        // ── Tabs ──────────────────────────────────────────────────────────
        PrimaryTabRow(
            selectedTabIndex = tabIndex,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = tabIndex == idx,
                    onClick = { tabIndex = idx },
                    text = { Text(title) }
                )
            }
        }

        when (tabIndex) {
            0 -> MiembrosTab(grupo)
            1 -> RankingInternoTab(ranking)
        }
    }
}

@Composable
private fun MiembrosTab(grupo: Grupo) {
    val colorScheme = MaterialTheme.colorScheme
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "${grupo.miembros.size} miembro${if (grupo.miembros.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(grupo.miembros, key = { it.usuarioId }) { miembro ->
            Surface(
                shape = MaterialTheme.shapes.large,
                color = colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null,
                        tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = miembro.usuarioId,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (miembro.rol.name == "ADMINISTRADOR") {
                        Badge(containerColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.onPrimaryContainer) {
                            Text("Admin", modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun RankingInternoTab(ranking: List<EntradaRankingInterno>) {
    val colorScheme = MaterialTheme.colorScheme
    if (ranking.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Sin datos de ranking", color = colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(ranking, key = { _, e -> e.usuario.id }) { _, entrada ->
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
                        text = medalEmoji(entrada.posicion),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(36.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entrada.usuario.nombre, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Global: #${entrada.posicionGlobal}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
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

// ── Diálogo crear grupo ────────────────────────────────────────────────────────

@Composable
private fun CrearGrupoDialog(
    onConfirm: (String, TipoGrupo) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoGrupo.PUBLICO) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Groups, contentDescription = null) },
        title = { Text("Crear grupo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del grupo") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Tipo de grupo", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = tipo == TipoGrupo.PUBLICO,
                        onClick = { tipo = TipoGrupo.PUBLICO }
                    )
                    Text("Público", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(24.dp))
                    RadioButton(
                        selected = tipo == TipoGrupo.PRIVADO,
                        onClick = { tipo = TipoGrupo.PRIVADO }
                    )
                    Icon(Icons.Default.Lock, contentDescription = null,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp))
                    Text("Privado", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (nombre.isNotBlank()) onConfirm(nombre.trim(), tipo) },
                enabled = nombre.isNotBlank()
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// ── Helpers ────────────────────────────────────────────────────────────────────

@Composable
private fun TipoBadge(tipo: TipoGrupo) {
    val colorScheme = MaterialTheme.colorScheme
    val (label, container, content) = if (tipo == TipoGrupo.PUBLICO)
        Triple("Público",  colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer)
    else
        Triple("Privado",  colorScheme.secondaryContainer, colorScheme.onSecondaryContainer)

    Badge(containerColor = container, contentColor = content) {
        Text(label, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

private fun medalEmoji(posicion: Int) = when (posicion) {
    1 -> "🥇"
    2 -> "🥈"
    3 -> "🥉"
    else -> "#$posicion"
}
