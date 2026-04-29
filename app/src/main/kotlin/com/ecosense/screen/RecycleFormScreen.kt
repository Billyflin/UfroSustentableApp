package com.ecosense.screen

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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.testing.TestNavHostController
import com.ecosense.RecyclingPoint
import com.ecosense.ScreenHistory
import com.ecosense.viewmodel.RecycleFormUiState
import com.ecosense.viewmodel.RecycleFormViewModel
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleFormScreen(
    navController: NavHostController?,
    data: String?,
    viewModel: RecycleFormViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = Firebase.auth.currentUser

    var expanded by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf("") }
    val materials = listOf("Plástico", "Vidrio", "Papel", "Metal", "Electrónicos")
    var kilos by remember { mutableStateOf("") }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }

    var recyclingPoint: RecyclingPoint? = null
    try {
        recyclingPoint = data?.let { Json.decodeFromString<RecyclingPoint>(it) }
    } catch (e: Exception) {
        Log.d("RecycleFormScreen", "Error parsing QR data: ${e.message}")
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is RecycleFormUiState.Success -> {
                Toast.makeText(context, "Solicitud creada exitosamente", Toast.LENGTH_SHORT).show()
                navController?.navigate(ScreenHistory)
                viewModel.resetState()
            }
            is RecycleFormUiState.Error -> {
                Toast.makeText(context, (uiState as RecycleFormUiState.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            @Suppress("DEPRECATION")
            capturedImage = result.data?.extras?.get("data") as? Bitmap
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
    }

    fun takePicture() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        if (uiState is RecycleFormUiState.Uploading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Enviando solicitud...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                ElevatedCard(
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Punto de reciclaje",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        recyclingPoint?.description?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Completa la información para ganar puntos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Material selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        value = selectedMaterial,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("Material de reciclaje") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        shape = MaterialTheme.shapes.large
                    )
                    ExposedDropdownMenu(
                        shape = MaterialTheme.shapes.large,
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        materials.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    selectedMaterial = option
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                // Kilos input
                OutlinedTextField(
                    value = kilos,
                    onValueChange = { kilos = it },
                    label = { Text("Cantidad en kilos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                )

                // Photo section
                AnimatedVisibility(
                    visible = capturedImage != null,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                            scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
                ) {
                    capturedImage?.let { bitmap ->
                        ElevatedCard(
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Foto capturada",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { if (capturedImage == null) takePicture() else capturedImage = null },
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (capturedImage == null) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.errorContainer,
                            contentColor = if (capturedImage == null) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (capturedImage == null) "Tomar Foto" else "Eliminar foto")
                    }

                    AnimatedVisibility(visible = capturedImage != null) {
                        Button(
                            onClick = {
                                val bitmap = capturedImage ?: return@Button
                                val uid = user?.uid ?: return@Button
                                viewModel.submitRequest(
                                    userId = uid,
                                    materialType = selectedMaterial,
                                    quantityKg = kilos.toDoubleOrNull() ?: 0.0,
                                    image = bitmap,
                                    description = recyclingPoint?.description
                                )
                            },
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.weight(1f),
                            enabled = selectedMaterial.isNotEmpty() && kilos.isNotEmpty()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Enviar")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecycleFormScreenPreview() {
    RecycleFormScreen(
        navController = TestNavHostController(LocalContext.current),
        data = """{"description":"Facultad de Ciencias Jurídicas","latitude":-38.736,"longitude":-72.598}"""
    )
}
