package com.example.ufrosustentableapp.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.ufrosustentableapp.R
import com.example.ufrosustentableapp.RecyclingPoint
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.MapsInitializer


@Composable
fun MapScreen() {
    val context = LocalContext.current

    // Estado para los puntos de reciclaje
    val recyclingPoints = remember { mutableStateListOf<RecyclingPoint>() }

    // Obtener puntos de reciclaje desde Firestore
    LaunchedEffect(Unit) {
        Firebase.firestore.collection("recycling_points")
            .get()
            .addOnSuccessListener { result ->
                recyclingPoints.clear()
                for (document in result) {
                    val point = RecyclingPoint(
                        latitude = document.getDouble("latitude") ?: 0.0,
                        longitude = document.getDouble("longitude") ?: 0.0,
                        description = document.getString("description") ?: ""
                    )
                    recyclingPoints.add(point)
                    Log.d("MapScreen", "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("MapScreen", "Error getting documents: ", exception)
            }
    }

    // Estado para el ícono personalizado
    val leafIcon = remember { mutableStateOf<BitmapDescriptor?>(null) }
    val colorScheme = MaterialTheme.colorScheme

    // Cargar el ícono personalizado
    LaunchedEffect(context) {
        leafIcon.value = bitmapDescriptorFromVector(context, R.drawable.leaves_svgrepo_com)
    }

    // Estado para los permisos de ubicación
    val locationPermissionGranted = remember { mutableStateOf(false) }

    // Lanzador de permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted.value = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Verificar y solicitar permisos
    LaunchedEffect(Unit) {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        locationPermissionGranted.value = fineLocationPermission || coarseLocationPermission

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

    val mapProperties = remember(locationPermissionGranted.value) {
        MapProperties(
            isBuildingEnabled = true,
            isIndoorEnabled = true,
            isMyLocationEnabled = locationPermissionGranted.value,
            mapStyleOptions = createFromScheme(colorScheme)
        )
    }

    if (locationPermissionGranted.value) {
        Box {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = mapProperties,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                    )
                ) {
                    // Marcador para la universidad
                    Marker(
                        state = MarkerState(position = universidadDeLaFrontera),
                        title = "Universidad de La Frontera",
                        snippet = "Temuco, Chile",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                    // Marcadores para los puntos de reciclaje
                    recyclingPoints.forEach { point ->
                        Marker(
                            state = MarkerState(
                                position = LatLng(
                                    point.latitude,
                                    point.longitude
                                )
                            ),
                            title = point.description,
                            snippet = "Punto de reciclaje",
                            icon = leafIcon.value
                        )
                    }
                }
            }
        }
    } else {
        // Mostrar mensaje si los permisos no han sido concedidos
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Se requieren permisos de ubicación para mostrar el mapa.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

fun createFromScheme(colorScheme: ColorScheme): MapStyleOptions? {
    return try {
        MapStyleOptions(
            """
        [
            {
                "elementType": "geometry",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.secondaryContainer.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "elementType": "labels.text.fill",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.onSurface.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "elementType": "labels.text.stroke",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.primaryContainer.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "administrative.country",
                "elementType": "geometry.stroke",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.secondary.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "administrative.land_parcel",
                "elementType": "labels.text.fill",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.onSecondary.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "administrative.province",
                "elementType": "geometry.stroke",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.secondaryContainer.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "landscape.man_made",
                "elementType": "geometry.stroke",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.background.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "landscape.natural",
                "elementType": "geometry",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.surface.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "poi",
                "elementType": "geometry",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.primaryContainer.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "poi",
                "elementType": "labels.text.fill",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.onPrimary.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "poi",
                "elementType": "labels.text.stroke",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.primary.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "poi.park",
                "elementType": "geometry.fill",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.secondaryContainer.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "poi.park",
                "elementType": "labels.text.fill",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.onSecondaryContainer.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "road",
                "elementType": "geometry",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.onPrimary.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "road",
                "elementType": "labels.text.fill",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.onBackground.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "road",
                "elementType": "labels.text.stroke",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.background.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "road.highway",
                "elementType": "geometry",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.primaryContainer.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "road.highway",
                "elementType": "geometry.stroke",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.primary.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "road.highway",
                "elementType": "labels.text.fill",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.onPrimary.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "road.highway",
                "elementType": "labels.text.stroke",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.secondary.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "transit",
                "elementType": "labels.text.fill",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.onSecondary.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "transit",
                "elementType": "labels.text.stroke",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.background.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "transit.line",
                "elementType": "geometry.fill",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.primary.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "transit.station",
                "elementType": "geometry",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.primaryContainer.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "water",
                "elementType": "geometry",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.surface.toArgb()).substring(2)
            }"
                    }
                ]
            },
            {
                "featureType": "water",
                "elementType": "labels.text.fill",
                "stylers": [
                    {
                        "color": "#${
                Integer.toHexString(colorScheme.onSurface.toArgb()).substring(2)
            }"
                    }
                ]
            }
        ]
    """.trimIndent()
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}



fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorResId: Int): BitmapDescriptor {
    // Inicializar el SDK de Google Maps
    MapsInitializer.initialize(context)

    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
    if (vectorDrawable == null) {
        Log.e("MapScreen", "El recurso vectorial no se pudo cargar.")
        throw Resources.NotFoundException("El recurso vectorial no se pudo cargar.")
    }

    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    vectorDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}


