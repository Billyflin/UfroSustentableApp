package com.example.ufrosustentableapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ufrosustentableapp.R
import com.example.ufrosustentableapp.ScreenHistory
import com.example.ufrosustentableapp.ScreenMap
import com.example.ufrosustentableapp.ScreenProfile
import com.example.ufrosustentableapp.ScreenQrScanner
import com.example.ufrosustentableapp.ScreenRewards
import com.google.firebase.auth.FirebaseUser


@Composable
fun BottomNavigationBar(navController: NavHostController, user: FirebaseUser?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(146.dp)
            .padding(bottom = 70.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(percent = 20),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                IconButton(onClick = {
                    navController.navigate(ScreenMap) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Map,
                        contentDescription = "Inicio",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                IconButton(onClick = {
                    navController.navigate(ScreenHistory) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Historial",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(56.dp))

                IconButton(onClick = {
                    navController.navigate(ScreenRewards) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Recompensas",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                IconButton(onClick = {
                    navController.navigate(ScreenProfile) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user?.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }


        FloatingActionButton(
            onClick = {
                navController.navigate(ScreenQrScanner)
                      },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
        ) {
            
            Icon(
                painter = painterResource(id = R.drawable.baseline_qr_code_scanner_24),
                contentDescription = "QR Scanner",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}



@Preview
@Composable
fun NavBarPreview() {
    BottomNavigationBar(rememberNavController(), null)
}