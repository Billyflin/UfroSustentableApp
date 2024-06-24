package com.example.ufrosustentableapp.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class RequestStatus {
    PROCESSING,
    VALIDATING,
    REWARD
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
fun HistoryScreenPreview() {
    HistoryScreen( onCancel = {})
}