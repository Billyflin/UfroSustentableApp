package com.example.ufrosustentableapp.Screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen() {
    Box {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val universidadDeLaFrontera = LatLng(-38.74658429580099, -72.6157555230996)
            val cameraPositionState = rememberCameraPositionState {
                CameraPosition.fromLatLngZoom(universidadDeLaFrontera, 16f).also { position = it }
            }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false
                )
            ) {
                Marker(
                    state = MarkerState(position = universidadDeLaFrontera),
                    title = "Temuco",
                    snippet = "Temuco, Chile"
                )
                Marker(
                    state = MarkerState(position = LatLng(-38.74, -72.59)),
                    title = "Padre Las Casas",
                    snippet = "Padre Las Casas, Chile"
                )
            }
        }
    }
}