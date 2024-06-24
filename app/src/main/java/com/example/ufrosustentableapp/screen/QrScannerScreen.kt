package com.example.ufrosustentableapp.screen

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ufrosustentableapp.RecyclingPoint
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage


@Composable
fun CameraScreen(onDocumentFound: (RecyclingPoint?) -> Unit) {
    val localContext = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(localContext)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val previewView = PreviewView(context).apply {
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(this.surfaceProvider)
                }

                val selector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                val imageAnalysis = ImageAnalysis.Builder().build().apply {
                    setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        BarcodeAnalyzer(context, onDocumentFound)
                    )
                }

                cameraProviderFuture.get().bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imageAnalysis
                )
            }
            previewView
        }
    )
}

class BarcodeAnalyzer(
    private val context: Context,
    private val onDocumentFound: (RecyclingPoint?) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    )

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    barcodes.firstOrNull()?.rawValue?.let { rawValue ->
                        checkDocumentExists(rawValue)
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun checkDocumentExists(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("recycling_points").document(documentId)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("CameraScreen", "DocumentSnapshot data: ${document.data}")
                    val recyclingPoint = document.toRecyclingPoint()
                    onDocumentFound(recyclingPoint)
                } else {
                    Toast.makeText(context, "Documento no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al acceder a Firestore", Toast.LENGTH_SHORT).show()
            }
    }
}


fun DocumentSnapshot.toRecyclingPoint(): RecyclingPoint? {
    val data = this.data ?: return null

    val latitude = data["latitude"] as? Double ?: return null
    val longitude = data["longitude"] as? Double ?: return null
    val description = data["description"] as? String ?: return null

    return RecyclingPoint(latitude, longitude, description)
}

