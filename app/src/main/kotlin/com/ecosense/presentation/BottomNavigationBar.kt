package com.ecosense.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ecosense.R
import com.ecosense.ScreenGrupos
import com.ecosense.ScreenHistory
import com.ecosense.ScreenMap
import com.ecosense.ScreenProfile
import com.google.firebase.auth.FirebaseUser

@Composable
fun BottomNavigationBar(
    currentKey:       NavKey,
    onNavigate:       (NavKey) -> Unit,
    onNavigateToQr:   () -> Unit,
    user:             FirebaseUser?
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 4.dp
        ) {
            NavigationBarItem(
                icon     = { Icon(Icons.Rounded.Map, contentDescription = null) },
                label    = { Text("Mapa") },
                selected = currentKey is ScreenMap,
                onClick  = { onNavigate(ScreenMap) }
            )
            NavigationBarItem(
                icon     = { Icon(Icons.Default.DateRange, contentDescription = null) },
                label    = { Text("Historial") },
                selected = currentKey is ScreenHistory,
                onClick  = { onNavigate(ScreenHistory) }
            )
            // Spacer para el FAB central
            NavigationBarItem(
                icon            = { Box(Modifier.size(48.dp)) },
                label           = { Text("") },
                selected        = false,
                onClick         = {},
                enabled         = false,
                alwaysShowLabel = false
            )
            NavigationBarItem(
                icon     = { Icon(Icons.Default.Groups, contentDescription = null) },
                label    = { Text("Grupos") },
                selected = currentKey is ScreenGrupos,
                onClick  = { onNavigate(ScreenGrupos) }
            )
            NavigationBarItem(
                icon = {
                    if (user?.photoUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(user.photoUrl)
                                .crossfade(true)
                                .build(),
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp).clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                },
                label    = { Text("Perfil") },
                selected = currentKey is ScreenProfile,
                onClick  = { onNavigate(ScreenProfile) }
            )
        }

        FloatingActionButton(
            onClick        = onNavigateToQr,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor   = MaterialTheme.colorScheme.onPrimary,
            modifier       = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_qr_code_scanner_24),
                contentDescription = "Escanear QR",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Preview
@Composable
fun NavBarPreview() {
    BottomNavigationBar(
        currentKey     = ScreenHistory,
        onNavigate     = {},
        onNavigateToQr = {},
        user           = null
    )
}
