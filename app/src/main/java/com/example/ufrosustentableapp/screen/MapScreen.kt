package com.example.ufrosustentableapp.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
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

@Composable
fun MapScreen() {
    val recyclingPoints = remember { mutableStateListOf<RecyclingPoint>() }

    Firebase.firestore.collection("recycling_points")
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                val point = RecyclingPoint(
                    latitude = document.getDouble("latitude") ?: 0.0,
                    longitude = document.getDouble("longitude") ?: 0.0,
                    description = document.getString("description") ?: ""
                )
                recyclingPoints.add(point)
                Log.d("MainActivity", "${document.id} => ${document.data}")
            }
        }
        .addOnFailureListener { exception ->
            Log.w("MainActivity", "Error getting documents: ", exception)
        }
    val context = LocalContext.current
    val leafIcon = remember { mutableStateOf<BitmapDescriptor?>(null) }

    LaunchedEffect(context) {
        leafIcon.value = bitmapDescriptorFromVector(context, R.drawable.leaves_svgrepo_com)
    }
    val locationPermissionGranted = remember { mutableStateOf(false) }

    LaunchedEffect(context) {
        locationPermissionGranted.value = checkLocationPermission(context)
    }
    val universidadDeLaFrontera = LatLng(-38.74658429580099, -72.6157555230996)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(universidadDeLaFrontera, 16.4f)
    }
    val colorScheme = MaterialTheme.colorScheme

    val mapProperties = remember(locationPermissionGranted.value) {
        MapProperties(
            isBuildingEnabled = true,
            isIndoorEnabled = true,
            isMyLocationEnabled = locationPermissionGranted.value,
//          Usar el color scheme para el estilo del mapa
            mapStyleOptions = createFromScheme(colorScheme)
        )
    }
    Log.d("MapScreen", "MapScreen: ${locationPermissionGranted.value}")

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
                Marker(
                    state = MarkerState(position = universidadDeLaFrontera),
                    title = "Temuco",
                    snippet = "Temuco, Chile",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
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


fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorResId: Int): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
    vectorDrawable?.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val bitmap = Bitmap.createBitmap(
        vectorDrawable!!.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}


