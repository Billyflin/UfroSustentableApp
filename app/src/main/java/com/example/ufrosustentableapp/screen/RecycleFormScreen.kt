package com.example.ufrosustentableapp.screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
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
import kotlinx.serialization.json.Json

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 70.dp, bottom = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Estás en el punto de reciclaje")
        recyclingPoint.let {
            Text(
                text = "${recyclingPoint?.description}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)

            )
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
                onClick = { /* Lógica para enviar el formulario */ },
                shape = MaterialTheme.shapes.large
            ) {
                Text("Enviar")
            }
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
