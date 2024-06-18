package com.example.ufrosustentableapp.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.ufrosustentableapp.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(recyclingPoints: List<RecyclingPoint>) {
    Box {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val localContext = LocalContext.current
            val leafIcon = remember { mutableStateOf<BitmapDescriptor?>(null) }

            LaunchedEffect(localContext) {
                leafIcon.value = bitmapDescriptorFromVector(localContext, R.drawable.leaves_svgrepo_com)
            }
            val locationPermissionGranted = remember { mutableStateOf(false) }

            LaunchedEffect(localContext) {
                locationPermissionGranted.value = checkLocationPermission(localContext)
            }
            val universidadDeLaFrontera = LatLng(-38.74658429580099, -72.6157555230996)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(universidadDeLaFrontera, 16f)
            }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermissionGranted.value),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = true,
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
                        state = MarkerState(position = LatLng(point.latitude, point.longitude)),
                        title = point.description,
                        snippet = "Punto de reciclaje",

                        icon = leafIcon.value
//                        icon= BitmapDescriptorFactory.fromResource(R.drawable.leaves_svgrepo_com)
                    )
                }
            }
        }
    }
}

fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
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


data class RecyclingPoint(
    val latitude: Double,
    val longitude: Double,
    val description: String
)
