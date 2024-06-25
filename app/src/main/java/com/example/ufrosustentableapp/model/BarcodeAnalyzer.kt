package com.example.ufrosustentableapp.model

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.ufrosustentableapp.RecyclingPoint
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
                    Log.d("CameraScreen", "DocumentSnapshot data: ${document.data.toString()}")
                    val recyclingPoint = RecyclingPoint(
                        latitude = document.data?.get("latitude") as Double,
                        longitude = document.data?.get("longitude") as Double,
                        description = document.data?.get("description") as String
                    )
                    val recyclingPointString = Json.encodeToString(recyclingPoint)
                    onDocumentFound(recyclingPointString)
                } else {
                    Toast.makeText(context, "Documento no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al acceder a Firestore", Toast.LENGTH_SHORT).show()
            }
    }
}