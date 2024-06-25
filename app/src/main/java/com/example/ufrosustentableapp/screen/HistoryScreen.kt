package com.example.ufrosustentableapp.screen

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.testing.TestNavHostController
import coil.compose.rememberAsyncImagePainter
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
    val transition = rememberInfiniteTransition(label = "")
    val containerColor by transition.animateColor(
        initialValue = colorScheme.primary,
        targetValue = colorScheme.primaryContainer,
        label = "containerColor",
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val transitionIcon = rememberInfiniteTransition(label = "")
    val containerColorIcon by transitionIcon.animateColor(
        initialValue = colorScheme.onPrimary,
        targetValue = colorScheme.onPrimaryContainer,
        label = "containerColorIcon",
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
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
            containerColor =if (request.status == RequestStatus.REWARD) containerColor else colorScheme.surfaceContainerHigh,
            contentColor = if (request.status == RequestStatus.REWARD) containerColorIcon else colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = request.description, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = request.materialType,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (request.status == RequestStatus.REWARD) containerColorIcon else colorScheme.onSurface
                )
                Text(
                    text = "Cantidad: ${request.quantityKg} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (request.status == RequestStatus.REWARD) containerColorIcon else colorScheme.onSurface
                )
                Text(
                    text = "Estado: ${request.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (request.status == RequestStatus.REWARD) containerColorIcon else colorScheme.onSurface
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
                    updateTime = document.getTimestamp("updateTime")?.toDate()?.toString() ?: "",
                    description = document.getString("description") ?: "",
                    reward = document.getLong("reward")?.toInt() ?: 0
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
    activeProgressBar: Int = 0,
    requestTime: String = "12:00",
    updateTime: String = "12:30",
    imageUrl: String = "",
    reward: Int = 0,
    description: String = "Hola",
    requestId: String = "",
    userId: String = "",
    status: RequestStatus = RequestStatus.PROCESSING,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 90.dp, bottom = 130.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Punto de reciclaje", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = description,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (status) {
                RequestStatus.PROCESSING -> "Estamos procesando tu solicitud"
                RequestStatus.VALIDATING -> "Estamos validando tu información"
                RequestStatus.REWARD -> "Recompensa disponible"
                RequestStatus.UNKNOWN -> "Estado desconocido"
                RequestStatus.REEDEMED -> "Recompensa canjeada"
                RequestStatus.REJECTED -> "Solicitud rechazada"
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
                    progress = if (i < activeProgressBar) 1f else 0f, // Completar las fases anteriores
                    isAnimating = i == activeProgressBar, // Animar la fase actual
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (imageUrl.isNotEmpty()&&status!=RequestStatus.REWARD) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(16.dp)
            ) {
                    Image(
                        painter =  rememberAsyncImagePainter(model = imageUrl),
                        contentDescription = null,
                        contentScale = ContentScale.FillHeight,
                        modifier = Modifier.fillMaxSize()
                    )
            }
        }
        if (status == RequestStatus.REWARD) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    redeemReward(requestId, userId, reward) { success ->
                        if (success) {

                        } else {
                            // Manejar error
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Reclamar Recompensa")
            }
        }
    }
}
fun redeemReward(requestId: String, userId: String, rewardPoints: Int, onComplete: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val requestRef = db.collection("recycling_requests").document(requestId)
    val userRef = db.collection("users").document(userId)

    db.runTransaction { transaction ->
        val requestSnapshot = transaction.get(requestRef)
        val userSnapshot = transaction.get(userRef)

        if (requestSnapshot.exists() && userSnapshot.exists()) {
            // Actualizar el estado de la solicitud a REEDEMED
            transaction.update(requestRef, "status", RequestStatus.REEDEMED.name)

            // Actualizar el puntaje del usuario
            val currentPoints = userSnapshot.getLong("points") ?: 0
            val newPoints = currentPoints + rewardPoints
            transaction.update(userRef, "points", newPoints)
        } else {
            throw Exception("Solicitud o usuario no encontrado")
        }
    }.addOnSuccessListener {
        onComplete(true)
    }.addOnFailureListener { e ->
        println("Error al reclamar la recompensa: ${e.message}")
        onComplete(false)
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
            updateTime = "12:30",
            description = "Descripción de la solicitud",
            reward = 0
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
        activeProgressBar = 1,
        requestTime = "12:00",
        updateTime = "12:30",
        status = RequestStatus.VALIDATING,
        onCancel = {}
    )
}