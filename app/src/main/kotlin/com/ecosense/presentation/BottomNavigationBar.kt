package com.ecosense.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ecosense.R
import com.ecosense.ScreenHistory
import com.ecosense.ScreenMap
import com.ecosense.ScreenProfile
import com.ecosense.ScreenQrScanner
import com.ecosense.ScreenRewards
import com.google.firebase.auth.FirebaseUser

@Composable
fun BottomNavigationBar(navController: NavHostController, user: FirebaseUser?) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 4.dp
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Rounded.Map, contentDescription = null) },
                label = { Text("Mapa") },
                selected = currentDestination?.hasRoute(ScreenMap::class) == true,
                onClick = {
                    navController.navigate(ScreenMap) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                label = { Text("Historial") },
                selected = currentDestination?.hasRoute(ScreenHistory::class) == true,
                onClick = {
                    navController.navigate(ScreenHistory) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
            // Spacer item for FAB
            NavigationBarItem(
                icon = { Box(Modifier.size(48.dp)) },
                label = { Text("") },
                selected = false,
                onClick = {},
                enabled = false,
                alwaysShowLabel = false
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Star, contentDescription = null) },
                label = { Text("Premios") },
                selected = currentDestination?.hasRoute(ScreenRewards::class) == true,
                onClick = {
                    navController.navigate(ScreenRewards) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
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
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                },
                label = { Text("Perfil") },
                selected = currentDestination?.hasRoute(ScreenProfile::class) == true,
                onClick = {
                    navController.navigate(ScreenProfile) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }

        FloatingActionButton(
            onClick = { navController.navigate(ScreenQrScanner) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
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
    BottomNavigationBar(rememberNavController(), null)
}
