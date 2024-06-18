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
import com.example.ufrosustentableapp.screen.CameraScreen
import com.example.ufrosustentableapp.screen.HistoryScreen
import com.example.ufrosustentableapp.screen.LoginScreen
import com.example.ufrosustentableapp.screen.MapScreen
import com.example.ufrosustentableapp.screen.ProfileScreen
import com.example.ufrosustentableapp.screen.RewardsScreen
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun AppNavHost(
    navController: NavHostController,
    user: FirebaseUser?,
    token: String,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    context: Context
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
            RewardsScreen()
        }
        composable<ScreenHistory> {
            HistoryScreen()
        }
        composable<ScreenProfile> {
            ProfileScreen(user){
                Firebase.auth.signOut()
                navController.navigate(ScreenLogin) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    }
}

fun NavBackStackEntry?.fromRoute(): String? {
    return this?.destination?.route?.substringAfterLast(".")
}
