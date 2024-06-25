package com.example.ufrosustentableapp.screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.testing.TestNavHostController
import com.example.ufrosustentableapp.RecyclingPoint
import com.example.ufrosustentableapp.ScreenHistory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleFormScreen(navController: NavHostController?, data: String?) {
    val context = LocalContext.current

    var expanded by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf("") }
    val materials = listOf("Plástico", "Vidrio", "Papel", "Metal", "Electrónicos")
    var recyclingPoint: RecyclingPoint? = null
    try {
        Log.d("RecycleFormScreen", "Data: $data")
        recyclingPoint = data?.let { Json.decodeFromString<RecyclingPoint>(data) }
        Log.d("RecycleFormScreen", "Recycling Point: $recyclingPoint")
    } catch (e: Exception) {
        Log.d("RecycleFormScreen", "Error: ${e.message}")
    }

    var isUploading by remember { mutableStateOf(false) }
    var kilos by remember { mutableStateOf("") }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap?
            capturedImage = imageBitmap
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        } else {
            // Handle permission denied
        }
    }

    fun takePicture() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(intent)
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    val user = Firebase.auth.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 70.dp, bottom = 130.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isUploading) {
//            ciculo en el centro de la pantalla
            CircularProgressIndicator(modifier = Modifier.size(50.dp).padding(top = 200.dp)
            )
        } else {
        Text(text = "Estás en el punto de reciclaje")
        recyclingPoint.let {
            it?.description?.let { it1 ->
                Text(
                    text = it1,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
        Text(
            text = "Completa la siguiente información para ganar puntos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "¿Qué material vas a reciclar?*",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            TextField(
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                value = selectedMaterial,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                label = { Text("Material de Reciclaje") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                shape = MaterialTheme.shapes.large,
            )
            ExposedDropdownMenu(
                shape = MaterialTheme.shapes.large,
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                materials.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            selectedMaterial = option
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = kilos,
            onValueChange = { kilos = it },
            label = { Text("Cantidad en kilos") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            shape = MaterialTheme.shapes.large,
        )

        Spacer(modifier = Modifier.height(16.dp))

        capturedImage?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                if (capturedImage == null) {
                    takePicture()
                } else {
                    capturedImage = null
                }
            },
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (capturedImage == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                contentColor = if (capturedImage == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
            )
        ) {
            Text(if (capturedImage == null) "Tomar Foto" else "Eliminar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (capturedImage != null) {
            Button(
                onClick = {
                    isUploading = true
                    capturedImage?.let { bitmap ->
                        uploadImageToFirebaseStorage(bitmap) { photoUrl ->
                            if (photoUrl != null) {
                                user?.uid?.let { userId ->
                                    val materialType = selectedMaterial
                                    val quantityKg = kilos.toDoubleOrNull() ?: 0.0

                                    createRecyclingRequest(
                                        userId = userId,
                                        materialType = materialType,
                                        quantityKg = quantityKg,
                                        photoUrl = photoUrl
                                    ) { success ->
                                        if (success) {
                                            Toast.makeText(context, "Solicitud creada exitosamente", Toast.LENGTH_SHORT).show()
                                            navController?.navigate(ScreenHistory)
                                        } else {
                                            Toast.makeText(context, "Error al crear la solicitud", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                shape = MaterialTheme.shapes.large
            ) {
                Text("Enviar")
            }
        }
        }
    }
}

fun createRecyclingRequest(
    userId: String,
    materialType: String,
    quantityKg: Double,
    photoUrl: String,
    onComplete: (Boolean) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val newRequestRef = db.collection("recycling_requests").document()

    val requestData = hashMapOf(
        "userId" to userId,
        "materialType" to materialType,
        "quantityKg" to quantityKg,
        "photoUrl" to photoUrl,
        "status" to "pending",
        "timestamp" to FieldValue.serverTimestamp()
    )

    newRequestRef.set(requestData)
        .addOnSuccessListener {
            // Update user's recyclingHistory
            val userRef = db.collection("users").document(userId)
            userRef.update("recyclingHistory", FieldValue.arrayUnion(newRequestRef.id))
                .addOnSuccessListener {
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        }
        .addOnFailureListener {
            onComplete(false)
        }
}


fun uploadImageToFirebaseStorage(bitmap: Bitmap, onComplete: (String?) -> Unit) {
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val data = baos.toByteArray()

    val uploadTask = imagesRef.putBytes(data)
    uploadTask.continueWithTask { task ->
        if (!task.isSuccessful) {
            task.exception?.let {
                throw it
            }
        }
        imagesRef.downloadUrl
    }.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val downloadUri = task.result
            onComplete(downloadUri.toString())
        } else {
            onComplete(null)
        }
    }
}



@Preview(showBackground = true)
@Composable
fun RecycleFormScreenPreview() {
    RecycleFormScreen(
        navController = TestNavHostController(LocalContext.current),
        data = """
            {
                "description": "Facultad de Ciencias Jurídicas y Empresariales UFRO",
                "latitude": -38.736,
                "longitude": -72.598
            }
        """.trimIndent()
    )
}
