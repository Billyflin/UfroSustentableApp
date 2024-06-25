package com.example.ufrosustentableapp.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.testing.TestNavHostController
import com.example.ufrosustentableapp.ScreenRequestDetail
import com.example.ufrosustentableapp.model.RecyclingRequest
import com.example.ufrosustentableapp.model.RequestStatus
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale


@Composable
fun RequestHistoryScreen(navController: NavHostController, userId: String) {
    LocalContext.current
    var requests by remember { mutableStateOf(listOf<RecyclingRequest>()) }

    LaunchedEffect(userId) {
        fetchUserRequests(userId) { requestsList ->
            requests = requestsList
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 74.dp, bottom = 110.dp),
    ) {
        Text(
            text = "Historial de Solicitudes",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (requests.isEmpty()) {
            Text("No hay solicitudes de reciclaje.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(requests) { request ->
                    RequestItem(navController, request)
                }
            }
        }
    }
}

@Composable
fun RequestItem(navController: NavHostController, request: RecyclingRequest) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(
                    ScreenRequestDetail(request.id)
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainerHigh,
            contentColor = colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = request.materialType,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Cantidad: ${request.quantityKg} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "Estado: ${request.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface
                )
            }
        }
    }
}

fun fetchUserRequests(userId: String, onResult: (List<RecyclingRequest>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("recycling_requests")
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { result ->
            val requests = result.map { document ->
                val status = try {
                    RequestStatus.valueOf(
                        document.getString("status")?.uppercase(Locale.ROOT) ?: "PROCESSING"
                    )
                } catch (e: IllegalArgumentException) {
                    RequestStatus.PROCESSING // Valor predeterminado en caso de error
                }
                RecyclingRequest(
                    id = document.id,
                    userId = document.getString("userId") ?: "",
                    materialType = document.getString("materialType") ?: "",
                    quantityKg = document.getDouble("quantityKg") ?: 0.0,
                    photoUrl = document.getString("photoUrl") ?: "",
                    status = status,
                    requestTime = document.getTimestamp("timestamp")?.toDate()?.toString() ?: "",
                    updateTime = document.getTimestamp("updateTime")?.toDate()?.toString() ?: ""
                )
            }
            onResult(requests)
        }
        .addOnFailureListener {
            onResult(emptyList()) // En caso de error, devolver una lista vacía
        }
}


@Composable
fun HistoryScreen(
    title: String = "Historial de Solicitudes",
    activeProgressBar: Int = 0,
    requestTime: String = "12:00",
    updateTime: String = "12:30",
    status: RequestStatus = RequestStatus.PROCESSING,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 118.dp, bottom = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title)
        Text(
            text = "UFRO RECICLA",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (status) {
                RequestStatus.PROCESSING -> "Estamos procesando tu solicitud"
                RequestStatus.VALIDATING -> "Estamos validando tu información"
                RequestStatus.REWARD -> "Recompensa disponible"
                RequestStatus.UNKNOWN -> "Estado desconocido"
            },
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hora Solicitud: $requestTime",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hora Actualización: $updateTime",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            for (i in 0..2) {
                AnimatedProgressBar(
                    progress = if (i == activeProgressBar) 1f else 0f, // Set progress for active bar
                    isAnimating = i == activeProgressBar,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Cancelar")
        }
    }
}

@Composable
fun AnimatedProgressBar(progress: Float, isAnimating: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val colorScheme = MaterialTheme.colorScheme
    val animatedProgress = if (isAnimating) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        ).value
    } else {
        progress
    }

    Canvas(modifier.height(4.dp)) {
        val barWidth = size.width
        val barHeight = size.height
        val cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        val dotRadius = barHeight / 2

        // Draw the progress bar background
        drawRoundRect(
            color = colorScheme.surfaceContainerHigh,
            size = size,
            cornerRadius = cornerRadius
        )

        // Draw the filled progress bar
        drawRoundRect(
            color = colorScheme.primary,
            size = size.copy(width = barWidth * if (isAnimating) animatedProgress else progress),
            cornerRadius = cornerRadius
        )

        // Draw the moving dot
        if (isAnimating) {
            val dotX = barWidth * animatedProgress
            drawCircle(
                color = colorScheme.primary,
                radius = dotRadius,
                center = Offset(dotX, barHeight / 2)
            )
        }
    }
}

@Preview
@Composable
fun RequestItemPreview() {
    RequestItem(
        navController = TestNavHostController(LocalContext.current),
        request = RecyclingRequest(
            id = "1",
            userId = "userId",
            materialType = "Plástico",
            quantityKg = 2.5,
            photoUrl = "",
            status = RequestStatus.PROCESSING,
            requestTime = "12:00",
            updateTime = "12:30"
        )
    )
}

@Preview
@Composable
fun RequestHistoryScreenPreview() {
    RequestHistoryScreen(
        navController = TestNavHostController(LocalContext.current),
        userId = "userId"
    )
}

@Preview
@Composable
fun HistoryScreenPreview() {
    HistoryScreen(onCancel = {})
}

@Preview
@Composable
fun HistoryScreenProcessingPreview() {
    HistoryScreen(
        title = "Historial de Solicitudes",
        activeProgressBar = 0,
        requestTime = "12:00",
        updateTime = "12:30",
        status = RequestStatus.PROCESSING,
        onCancel = {}
    )
}

@Preview
@Composable
fun HistoryScreenValidatingPreview() {
    HistoryScreen(
        title = "Historial de Solicitudes",
        activeProgressBar = 1,
        requestTime = "12:00",
        updateTime = "12:30",
        status = RequestStatus.VALIDATING,
        onCancel = {}
    )
}