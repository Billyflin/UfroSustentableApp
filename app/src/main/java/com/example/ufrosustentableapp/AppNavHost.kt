package com.example.ufrosustentableapp

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ufrosustentableapp.Screen.ProfileScreen
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
            LoggedInContent()
        }
        composable<ScreenB> { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name")
            Text("Screen B: $name")
        }
        composable<ScreenQrScanner> {
            QrScannerScreen()
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