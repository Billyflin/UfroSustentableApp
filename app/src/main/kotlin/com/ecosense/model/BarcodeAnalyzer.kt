package com.ecosense.model

import android.annotation.SuppressLint
import android.content.Context
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

class BarcodeAnalyzer(
    private val context: Context,
    private val onDocumentFound: (String?) -> Unit
) : ImageAnalysis.Analyzer {

    private val db = FirebaseFirestore.getInstance()

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
    )

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            scanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    barcodes.firstOrNull()?.rawValue?.let { checkDocumentExists(it) }
                }
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    private fun checkDocumentExists(documentId: String) {
        db.collection("recycling_points").document(documentId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Toast.makeText(context, "Código QR no reconocido", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                Log.d("BarcodeAnalyzer", "Datos del punto: ${document.data}")
                val lat  = (document.get("latitude")    as? Double) ?: run {
                    Toast.makeText(context, "Datos del punto incompletos", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val lng  = (document.get("longitude")   as? Double) ?: run {
                    Toast.makeText(context, "Datos del punto incompletos", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val desc = (document.getString("description")) ?: ""
                val encoded = Json.encodeToString(RecyclingPoint(lat, lng, desc))
                onDocumentFound(encoded)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al leer el punto de reciclaje", Toast.LENGTH_SHORT).show()
            }
    }
}
