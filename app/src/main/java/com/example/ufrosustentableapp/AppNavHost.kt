package com.example.ufrosustentableapp

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.ufrosustentableapp.screen.CameraScreen
import com.example.ufrosustentableapp.screen.HistoryScreen
import com.example.ufrosustentableapp.screen.LoginScreen
import com.example.ufrosustentableapp.screen.MapScreen
import com.example.ufrosustentableapp.screen.ProfileScreen
import com.example.ufrosustentableapp.screen.RewardConfirmationScreen
import com.example.ufrosustentableapp.screen.RewardItem
import com.example.ufrosustentableapp.screen.RewardsScreen
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

fun NavBackStackEntry?.fromRoute(): String? {
    return this?.destination?.route?.substringAfterLast(".")
}
@Composable
fun AppNavHost(
    navController: NavHostController,
    user: FirebaseUser?,
    token: String,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    context: Context,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    isDynamicColor: Boolean,
    onToggleDynamicColor: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = if (user == null) ScreenLogin else ScreenMap
    ) {
        composable<ScreenLogin> {
            LoginScreen(token = token, launcher = launcher, context = context)
        }
        composable<ScreenMap> {
            MapScreen()
        }
        composable<ScreenB> { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name")
            Text("Screen B: $name")
        }
        composable<ScreenQrScanner> {
            CameraScreen()
        }
        composable<ScreenRewards> {
            RewardsScreen(
                navController = navController,
                userPoints = 1500,
                rewards = listOf(
                    RewardItem("Café Gratis", 500),
                    RewardItem("Descuento en Tienda", 1000),
                    RewardItem("Entrada al Cine", 1500),
                    RewardItem("Descuento en Restaurante", 2000),
                    RewardItem("Descuento en Librería", 2500),
                    RewardItem("Descuento en Ropa", 3000),
                    RewardItem("Descuento en Tecnología", 3500),
                    RewardItem("Descuento en Viajes", 4000),
                )
            )
        }
        composable<ScreenRewardConfimation> {
            val args = it.toRoute<ScreenRewardConfimation>()
            RewardConfirmationScreen(
                navController = navController,
                rewardTitle = args.rewardTitle,
                rewardCost = args.rewardCost,
                userPoints = args.userPoints
            )
        }
        composable<ScreenHistory> {
            HistoryScreen()
        }
        composable<ScreenProfile> {
            ProfileScreen(
                user = user,
                onToggleDarkMode = onToggleDarkMode,
                onToggleDynamicColor = onToggleDynamicColor,
                isDarkMode = isDarkMode,
                isDynamicColor = isDynamicColor,
                onLogout = {
                    Firebase.auth.signOut()
                    navController.navigate("ScreenLogin") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
    }
}
