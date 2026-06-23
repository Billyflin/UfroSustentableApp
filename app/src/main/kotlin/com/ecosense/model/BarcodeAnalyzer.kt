package com.ecosense.model

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.ecosense.RecyclingPoint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicBoolean

class BarcodeAnalyzer(
    private val context: Context,
    private val onDocumentFound: (String?) -> Unit
) : ImageAnalysis.Analyzer {

    private companion object {
        private const val DUPLICATE_LOOKUP_WINDOW_MS = 2_500L
    }

    private val db = FirebaseFirestore.getInstance()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val isLookingUp = AtomicBoolean(false)
    private var lastLookupId: String? = null
    private var lastLookupAt: Long = 0L
    private var completedDocumentId: String? = null

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    )

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val image = imageProxy.image
        if (image == null || completedDocumentId != null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { checkDocumentExists(it) }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun checkDocumentExists(documentId: String) {
        val now = System.currentTimeMillis()
        val isDuplicateWindow = documentId == lastLookupId && now - lastLookupAt < DUPLICATE_LOOKUP_WINDOW_MS
        if (completedDocumentId == documentId || isDuplicateWindow || !isLookingUp.compareAndSet(false, true)) {
            return
        }

        lastLookupId = documentId
        lastLookupAt = now

        db.collection("recycling_points").document(documentId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    showToast("Código QR no reconocido")
                    return@addOnSuccessListener
                }
                Log.d("BarcodeAnalyzer", "Datos del punto: ${document.data}")
                val lat = (document.get("latitude") as? Double) ?: run {
                    showToast("Datos del punto incompletos")
                    return@addOnSuccessListener
                }
                val lng = (document.get("longitude") as? Double) ?: run {
                    showToast("Datos del punto incompletos")
                    return@addOnSuccessListener
                }
                val desc = document.getString("description") ?: ""
                val encoded = Json.encodeToString(RecyclingPoint(lat, lng, desc))
                completedDocumentId = documentId
                mainHandler.post { onDocumentFound(encoded) }
            }
            .addOnFailureListener {
                showToast("Error al leer el punto de reciclaje")
            }
            .addOnCompleteListener {
                isLookingUp.set(false)
            }
    }

    fun close() {
        scanner.close()
    }

    private fun showToast(message: String) {
        mainHandler.post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
