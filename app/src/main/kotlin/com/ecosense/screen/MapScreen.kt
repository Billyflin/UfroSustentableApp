package com.ecosense.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ecosense.R
import com.ecosense.viewmodel.MapViewModel
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.key

@Composable
fun MapScreen(viewModel: MapViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val isDarkMap = colorScheme.background.luminance() < 0.5f
    val mapAccent = if (isDarkMap) Color(0xFF86D99F) else Color(0xFF176B3A)
    val mapChipColor = if (isDarkMap) Color(0xFF26352B) else Color(0xFFF4F8F4)
    val mapChipContentColor = if (isDarkMap) Color(0xFFE7EEE9) else Color(0xFF243129)

    val leafIcon = remember { mutableStateOf<BitmapDescriptor?>(null) }
    val locationPermissionGranted = remember { mutableStateOf(false) }

    LaunchedEffect(context) {
        leafIcon.value = bitmapDescriptorFromVector(
            context = context,
            vectorResId = R.drawable.leaves_svgrepo_com,
            sizeDp = 30
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted.value =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        locationPermissionGranted.value = fineGranted || coarseGranted
        if (!locationPermissionGranted.value) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val universidadDeLaFrontera = LatLng(-38.74658429580099, -72.6157555230996)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(universidadDeLaFrontera, 16.4f)
    }

    // Separated so permission changes don't trigger expensive JSON rebuild
    val mapStyleOptions = remember(isDarkMap) { createFromScheme(colorScheme) }
    val mapProperties = remember(locationPermissionGranted.value, mapStyleOptions) {
        MapProperties(
            isBuildingEnabled = true,
            isIndoorEnabled = true,
            isMyLocationEnabled = locationPermissionGranted.value,
            mapStyleOptions = mapStyleOptions
        )
    }
    val mapUiSettings = remember { MapUiSettings(zoomControlsEnabled = false) }

    if (locationPermissionGranted.value) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings
            ) {
                key("ufro") {
                    val markerState = remember { MarkerState(position = universidadDeLaFrontera) }
                    Marker(
                        state = markerState,
                        title = "Universidad de La Frontera",
                        snippet = "Temuco, Chile",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
                uiState.recyclingPoints.forEach { point ->
                    key(point.latitude, point.longitude) {
                        val markerState = remember(point.latitude, point.longitude) {
                            MarkerState(position = LatLng(point.latitude, point.longitude))
                        }
                        Marker(
                            state = markerState,
                            title = point.description,
                            snippet = "Punto de reciclaje",
                            icon = leafIcon.value
                        )
                    }
                }
            }

            // Loading overlay
            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
                exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
            ) {
                ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(12.dp).size(24.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.5.dp
                    )
                }
            }

            // Points count chip
            AnimatedVisibility(
                visible = !uiState.isLoading && uiState.recyclingPoints.isNotEmpty(),
                enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                        scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) +
                        slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { -it },
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
            ) {
                ElevatedSuggestionChip(
                    onClick = {},
                    label = { Text("${uiState.recyclingPoints.size} puntos de reciclaje") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(SuggestionChipDefaults.IconSize)
                        )
                    },
                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                        containerColor = mapChipColor,
                        labelColor = mapChipContentColor,
                        iconContentColor = mapAccent
                    )
                )
            }

            // Error chip
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
            ) {
                ElevatedCard(
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Button(
                        onClick = { viewModel.retry() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.errorContainer,
                            contentColor = colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Reintentar")
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Permisos de ubicación requeridos",
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Para ver los puntos de reciclaje cercanos necesitamos acceso a tu ubicación.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            ) {
                Text("Permitir ubicación")
            }
        }
    }
}

fun createFromScheme(colorScheme: ColorScheme): MapStyleOptions? {
    val dark = colorScheme.background.luminance() < 0.5f
    val styleJson = if (dark) DARK_MAP_STYLE else LIGHT_MAP_STYLE
    return try {
        MapStyleOptions(styleJson)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun bitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorResId: Int,
    sizeDp: Int = 30
): BitmapDescriptor {
    MapsInitializer.initialize(context)
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        ?: run {
            Log.e("MapScreen", "El recurso vectorial no se pudo cargar.")
            throw android.content.res.Resources.NotFoundException("El recurso vectorial no se pudo cargar.")
        }
    val sizePx = (sizeDp * context.resources.displayMetrics.density).toInt()
    vectorDrawable.setBounds(0, 0, sizePx, sizePx)
    val bitmap = Bitmap.createBitmap(
        sizePx,
        sizePx,
        Bitmap.Config.ARGB_8888
    )
    vectorDrawable.draw(android.graphics.Canvas(bitmap))
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private val DARK_MAP_STYLE =
    """
    [
      {"elementType":"geometry","stylers":[{"color":"#202522"}]},
      {"elementType":"labels.icon","stylers":[{"saturation":-35},{"lightness":-12}]},
      {"elementType":"labels.text.fill","stylers":[{"color":"#C8D0CA"}]},
      {"elementType":"labels.text.stroke","stylers":[{"color":"#202522"},{"weight":3}]},
      {"featureType":"administrative","elementType":"geometry.stroke","stylers":[{"color":"#5D6861"}]},
      {"featureType":"administrative.land_parcel","elementType":"labels","stylers":[{"visibility":"off"}]},
      {"featureType":"landscape.natural","elementType":"geometry","stylers":[{"color":"#1D241F"}]},
      {"featureType":"poi","elementType":"geometry","stylers":[{"color":"#252C27"}]},
      {"featureType":"poi","elementType":"labels.text.fill","stylers":[{"color":"#AEB9B1"}]},
      {"featureType":"poi.business","elementType":"labels.icon","stylers":[{"visibility":"off"}]},
      {"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#183A29"}]},
      {"featureType":"poi.park","elementType":"labels.text.fill","stylers":[{"color":"#83C894"}]},
      {"featureType":"road","elementType":"geometry.fill","stylers":[{"color":"#3A423D"}]},
      {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#171B18"}]},
      {"featureType":"road","elementType":"labels.text.fill","stylers":[{"color":"#D8DDD9"}]},
      {"featureType":"road.highway","elementType":"geometry.fill","stylers":[{"color":"#526258"}]},
      {"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#263029"}]},
      {"featureType":"transit","elementType":"geometry","stylers":[{"color":"#323A35"}]},
      {"featureType":"transit.station","elementType":"labels.icon","stylers":[{"saturation":-45},{"lightness":-10}]},
      {"featureType":"water","elementType":"geometry","stylers":[{"color":"#18313A"}]},
      {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#8CB8C5"}]}
    ]
    """.trimIndent()

private val LIGHT_MAP_STYLE =
    """
    [
      {"elementType":"geometry","stylers":[{"color":"#EDF1EE"}]},
      {"elementType":"labels.icon","stylers":[{"saturation":-25},{"lightness":5}]},
      {"elementType":"labels.text.fill","stylers":[{"color":"#3F4943"}]},
      {"elementType":"labels.text.stroke","stylers":[{"color":"#F6F8F6"},{"weight":3}]},
      {"featureType":"administrative","elementType":"geometry.stroke","stylers":[{"color":"#AAB4AD"}]},
      {"featureType":"administrative.land_parcel","elementType":"labels","stylers":[{"visibility":"off"}]},
      {"featureType":"landscape.natural","elementType":"geometry","stylers":[{"color":"#EAF0EB"}]},
      {"featureType":"poi","elementType":"geometry","stylers":[{"color":"#E1E8E2"}]},
      {"featureType":"poi","elementType":"labels.text.fill","stylers":[{"color":"#526058"}]},
      {"featureType":"poi.business","elementType":"labels.icon","stylers":[{"visibility":"off"}]},
      {"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#CCE8D3"}]},
      {"featureType":"poi.park","elementType":"labels.text.fill","stylers":[{"color":"#347245"}]},
      {"featureType":"road","elementType":"geometry.fill","stylers":[{"color":"#FFFFFF"}]},
      {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#D9E0DB"}]},
      {"featureType":"road","elementType":"labels.text.fill","stylers":[{"color":"#46514A"}]},
      {"featureType":"road.highway","elementType":"geometry.fill","stylers":[{"color":"#DDEBE1"}]},
      {"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#C1D2C6"}]},
      {"featureType":"transit","elementType":"geometry","stylers":[{"color":"#D6DDD8"}]},
      {"featureType":"transit.station","elementType":"labels.icon","stylers":[{"saturation":-35},{"lightness":5}]},
      {"featureType":"water","elementType":"geometry","stylers":[{"color":"#CDE6ED"}]},
      {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#497985"}]}
    ]
    """.trimIndent()
