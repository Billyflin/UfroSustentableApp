package com.ecosense.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.ecosense.R
import com.ecosense.model.RecyclingRequest
import com.ecosense.model.RequestStatus
import com.ecosense.presentation.infiniteColorTransition
import com.ecosense.viewmodel.HistoryUiState
import com.ecosense.viewmodel.HistoryViewModel
import com.ecosense.viewmodel.RedeemState
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun RequestHistoryScreen(
    onNavigateToDetail: (String) -> Unit,
    userId: String,
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.loadRequests(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "Mis solicitudes",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (uiState is HistoryUiState.Success) {
                val count = (uiState as HistoryUiState.Success).requests.size
                Spacer(Modifier.weight(1f))
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.shapes.extraLarge
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                        slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 4 } togetherWith
                        fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
            },
            label = "historyState"
        ) { state ->
            when (state) {
                is HistoryUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is HistoryUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Error al cargar solicitudes",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.loadRequests(userId) }) {
                            Text("Reintentar")
                        }
                    }
                }
                is HistoryUiState.Success -> {
                    if (state.requests.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Aún no tienes solicitudes",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Escanea un código QR en un punto de reciclaje para comenzar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        val sortedRequests = state.requests.sortedBy { it.status == RequestStatus.REEDEMED }
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 20.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(sortedRequests, key = { it.id }) { request ->
                                RequestItem(onNavigateToDetail, request)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Routes to the animated variant only for REWARD-status requests so that
 * infinite color transitions aren't started for every card in the list.
 */
@Composable
fun RequestItem(onNavigateToDetail: (String) -> Unit, request: RecyclingRequest) {
    if (request.status == RequestStatus.REWARD) {
        RequestItemAnimated(onNavigateToDetail, request)
    } else {
        RequestItemStatic(onNavigateToDetail, request)
    }
}

/** REWARD status — runs 4 infinite color transitions to pulse the card. */
@Composable
private fun RequestItemAnimated(onNavigateToDetail: (String) -> Unit, request: RecyclingRequest) {
    val colorScheme = MaterialTheme.colorScheme
    val cardColor   by infiniteColorTransition(colorScheme.primary,          colorScheme.inversePrimary,     "cardColor")
    val contentColor by infiniteColorTransition(colorScheme.onPrimary,       colorScheme.onSurfaceVariant,   "contentColor")
    val accentColor2 by infiniteColorTransition(colorScheme.tertiaryContainer, colorScheme.tertiary,         "accentColor2")
    val accentColor3 by infiniteColorTransition(colorScheme.primaryContainer, colorScheme.primary,           "accentColor3")

    RequestItemCard(
        onNavigateToDetail = onNavigateToDetail,
        request            = request,
        cardContainer   = cardColor,
        cardContent     = contentColor,
        descriptionColor = accentColor2,
        materialColor   = accentColor3,
        quantityColor   = accentColor3,
        timeColor       = accentColor3
    )
}

/** All other statuses — static colors, no ongoing animation overhead. */
@Composable
private fun RequestItemStatic(onNavigateToDetail: (String) -> Unit, request: RecyclingRequest) {
    val colorScheme  = MaterialTheme.colorScheme
    val isRedeemed   = request.status == RequestStatus.REEDEMED
    val muted        = colorScheme.onSurfaceVariant
    val cardContainer = if (isRedeemed) colorScheme.surfaceContainerLow else colorScheme.surfaceContainerHigh

    RequestItemCard(
        onNavigateToDetail = onNavigateToDetail,
        request            = request,
        cardContainer    = cardContainer,
        cardContent      = if (isRedeemed) muted else colorScheme.onSurface,
        descriptionColor = if (isRedeemed) muted else colorScheme.tertiary,
        materialColor    = if (isRedeemed) muted else colorScheme.primary,
        quantityColor    = if (isRedeemed) muted else colorScheme.primary,
        timeColor        = if (isRedeemed) muted else colorScheme.primary
    )
}

@Composable
private fun RequestItemCard(
    onNavigateToDetail: (String) -> Unit,
    request:            RecyclingRequest,
    cardContainer:    androidx.compose.ui.graphics.Color,
    cardContent:      androidx.compose.ui.graphics.Color,
    descriptionColor: androidx.compose.ui.graphics.Color,
    materialColor:    androidx.compose.ui.graphics.Color,
    quantityColor:    androidx.compose.ui.graphics.Color,
    timeColor:        androidx.compose.ui.graphics.Color
) {
    val colorScheme  = MaterialTheme.colorScheme
    val isRedeemed   = request.status == RequestStatus.REEDEMED
    val dateFormat   = remember { SimpleDateFormat("dd/MM/yy", Locale.forLanguageTag("es-ES")) }
    val timeFormat   = remember { SimpleDateFormat("h:mm",     Locale.forLanguageTag("es-ES")) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isRedeemed) {
                onNavigateToDetail(request.id)
            },
        shape  = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardContainer,
            contentColor   = cardContent
        )
    ) {
        Row(
            modifier              = Modifier.padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier            = Modifier.weight(0.83f)
            ) {
                Text(
                    text  = request.description,
                    style = MaterialTheme.typography.labelMedium,
                    color = descriptionColor
                )
                Text(
                    text  = request.materialType,
                    style = MaterialTheme.typography.titleMedium,
                    color = materialColor
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = if (isRedeemed) colorScheme.onSurfaceVariant else colorScheme.onSurface)) {
                            append("Cantidad: ")
                        }
                        withStyle(SpanStyle(color = quantityColor)) {
                            append("${request.quantityKg} kg")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(
                modifier            = Modifier.weight(0.17f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text  = dateFormat.format(request.requestTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isRedeemed) colorScheme.onSurfaceVariant else colorScheme.onSurface
                )
                Text(
                    text  = timeFormat.format(request.requestTime),
                    style = MaterialTheme.typography.titleSmall,
                    color = timeColor
                )
                if (isRedeemed) {
                    Icon(
                        painter            = painterResource(id = R.drawable.baseline_workspace_premium_20),
                        contentDescription = "Canjeado",
                        tint               = colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(
    activeProgressBar: Int = 0,
    requestTime: Date?,
    updateTime: Date?,
    imageUrl: String = "",
    reward: Int = 0,
    description: String = "Hola",
    requestId: String = "",
    userId: String = "",
    status: RequestStatus = RequestStatus.PROCESSING,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val redeemState by viewModel.redeemState.collectAsStateWithLifecycle()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())

    val formattedRequestTime = requestTime?.toInstant()
        ?.atZone(ZoneId.systemDefault())?.toLocalDateTime()?.format(formatter) ?: "Desconocido"

    val formattedUpdateTime = updateTime?.toInstant()
        ?.atZone(ZoneId.systemDefault())?.toLocalDateTime()?.format(formatter) ?: "Desconocido"

    var activeProgressBar2 by remember { mutableIntStateOf(activeProgressBar) }
    var claimInitiated by remember { mutableStateOf(false) }

    LaunchedEffect(redeemState) {
        if (redeemState is RedeemState.Success) {
            activeProgressBar2 = 3
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Punto de reciclaje", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = description,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))

        ElevatedCard(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (status) {
                        RequestStatus.PROCESSING -> "Procesando solicitud"
                        RequestStatus.VALIDATING -> "Validando información"
                        RequestStatus.REWARD -> "Recompensa disponible"
                        RequestStatus.UNKNOWN -> "Estado desconocido"
                        RequestStatus.REEDEMED -> "Recompensa canjeada"
                        RequestStatus.REJECTED -> "Solicitud rechazada"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Solicitud: $formattedRequestTime", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Actualizado: $formattedUpdateTime", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            for (i in 0..2) {
                AnimatedProgressBar(
                    progress = if (i < activeProgressBar2) 1f else 0f,
                    isAnimating = i == activeProgressBar2,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (imageUrl.isNotEmpty() && status != RequestStatus.REWARD) {
            Spacer(modifier = Modifier.height(16.dp))
            ElevatedCard(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().height(220.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (status == RequestStatus.REWARD) {
            Spacer(modifier = Modifier.height(24.dp))
            AnimatedContent(
                targetState = redeemState,
                transitionSpec = {
                    fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                            fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
                },
                label = "redeemState"
            ) { state ->
                when (state) {
                    is RedeemState.Idle -> {
                        if (!claimInitiated) {
                            Button(
                                onClick = {
                                    claimInitiated = true
                                    viewModel.redeemReward(requestId, userId, reward)
                                },
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reclamar Recompensa")
                            }
                        }
                    }
                    is RedeemState.Loading -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                    is RedeemState.Success -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Recompensa reclamada",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Gracias por reciclar. Tu recompensa ha sido añadida a tu cuenta.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onBack,
                                shape = MaterialTheme.shapes.large,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Volver")
                            }
                        }
                    }
                    is RedeemState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error: ${state.message}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.resetRedeemState()
                                    claimInitiated = false
                                },
                                shape = MaterialTheme.shapes.large
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedProgressBar(progress: Float, isAnimating: Boolean, modifier: Modifier = Modifier) {
    val colorScheme        = MaterialTheme.colorScheme
    val infiniteTransition = rememberInfiniteTransition(label = "progressPulse")
    // Always call animateFloat unconditionally — Compose rules prohibit conditional hook calls.
    // The value is only used when isAnimating = true.
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progressPulse"
    )
    val animatedProgress = if (isAnimating) pulseProgress else progress

    Canvas(modifier.height(6.dp)) {
        val barWidth = size.width
        val barHeight = size.height
        val cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        val dotRadius = barHeight / 2

        drawRoundRect(
            color = colorScheme.surfaceContainerHighest,
            size = size,
            cornerRadius = cornerRadius
        )
        drawRoundRect(
            color = colorScheme.primary,
            size = size.copy(width = barWidth * if (isAnimating) animatedProgress else progress),
            cornerRadius = cornerRadius
        )
        if (isAnimating) {
            drawCircle(
                color = colorScheme.primary,
                radius = dotRadius,
                center = Offset(barWidth * animatedProgress, barHeight / 2)
            )
        }
    }
}

@Preview
@Composable
fun RequestItemPreview() {
    RequestItem(
        onNavigateToDetail = {},
        request = RecyclingRequest(
            id = "1",
            userId = "userId",
            materialType = "Plástico",
            quantityKg = 2.5,
            photoUrl = "",
            status = RequestStatus.PROCESSING,
            requestTime = Date(),
            updateTime = Date(),
            description = "Descripción de la solicitud",
            reward = 0
        )
    )
}

@Preview
@Composable
fun RequestHistoryScreenPreview() {
    RequestHistoryScreen(
        onNavigateToDetail = {},
        userId = "userId"
    )
}
