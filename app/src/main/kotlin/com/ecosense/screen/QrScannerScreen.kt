package com.ecosense.screen

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ecosense.model.BarcodeAnalyzer

private const val TAG = "QrScannerScreen"

@Composable
fun CameraScreen(onDocumentFound: (String?) -> Unit) {
    val context  = LocalContext.current
    val executor = remember { ContextCompat.getMainExecutor(context) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { hasCameraPermission = it }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!hasCameraPermission) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Permiso de cámara requerido",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Necesitamos acceso a la cámara para escanear códigos QR en los puntos de reciclaje.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Permitir cámara")
            }
        }
        return
    }

    val lifecycleOwner        = LocalLifecycleOwner.current
    val cameraProviderFuture  = remember { ProcessCameraProvider.getInstance(context) }
    val primaryColor          = MaterialTheme.colorScheme.primary

    // Línea de escaneo animada
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // Vista de la cámara
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory  = { ctx ->
                val previewView = PreviewView(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val imageAnalysis = ImageAnalysis.Builder().build().apply {
                            setAnalyzer(executor, BarcodeAnalyzer(ctx, onDocumentFound))
                        }
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al iniciar la cámara", e)
                    }
                }, executor)
                previewView
            }
        )

        // Overlay oscuro + ventana de escaneo
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW    = size.width
            val canvasH    = size.height
            val boxSize    = minOf(canvasW, canvasH) * 0.72f
            val boxLeft    = (canvasW - boxSize) / 2f
            val boxTop     = (canvasH - boxSize) / 2f
            val cornerR    = CornerRadius(24.dp.toPx())
            val overlayColor = Color.Black.copy(alpha = 0.55f)

            // Fondo oscuro completo
            drawRect(color = overlayColor, size = size)

            // "Borrar" la ventana central con blend mode
            drawRoundRect(
                color        = Color.Transparent,
                topLeft      = Offset(boxLeft, boxTop),
                size         = Size(boxSize, boxSize),
                cornerRadius = cornerR,
                blendMode    = BlendMode.Clear
            )

            // Borde del recuadro
            drawRoundRect(
                color        = primaryColor,
                topLeft      = Offset(boxLeft, boxTop),
                size         = Size(boxSize, boxSize),
                cornerRadius = cornerR,
                style        = Stroke(width = 3.dp.toPx())
            )

            // Línea de escaneo (solo dentro del recuadro)
            val lineY = boxTop + boxSize * scanLineY
            drawLine(
                color       = primaryColor.copy(alpha = 0.8f),
                start       = Offset(boxLeft + 12.dp.toPx(), lineY),
                end         = Offset(boxLeft + boxSize - 12.dp.toPx(), lineY),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Texto de instrucción
        Text(
            text      = "Apunta al código QR del punto de reciclaje",
            style     = MaterialTheme.typography.bodyMedium,
            color     = Color.White,
            textAlign = TextAlign.Center,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp, start = 32.dp, end = 32.dp)
        )
    }
}
