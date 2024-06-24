package com.example.ufrosustentableapp

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.ufrosustentableapp.screen.HistoryScreen
import com.example.ufrosustentableapp.screen.LoginScreen
import com.example.ufrosustentableapp.screen.MapScreen
import com.example.ufrosustentableapp.screen.ProfileScreen
import com.example.ufrosustentableapp.screen.RewardConfirmationScreen
import com.example.ufrosustentableapp.screen.RewardItem
import com.example.ufrosustentableapp.screen.RewardsScreen
import com.example.ufrosustentableapp.ui.theme.ContrastLevel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.serialization.Serializable

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
    onToggleDynamicColor: () -> Unit,
    contrastLevel: ContrastLevel,
    onChangeContrastLevel: (ContrastLevel) -> Unit,
    recyclingPoints: SnapshotStateList<RecyclingPoint>
) {
    NavHost(
        navController = navController,
//        startDestination = if (user == null) ScreenLogin else ScreenMap
        startDestination = ScreenA
    ) {
        composable<ScreenLogin> {
            LoginScreen(token = token, launcher = launcher, context = context)
        }
        composable<ScreenMap> {
            MapScreen(recyclingPoints)
        }
        composable<ScreenA> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
            Text("Screen A")
            val numero : Double = 5.0
            Button(onClick = { navController.navigate(ScreenB(double = numero)) }) {
                Text("Go to Screen B")
            }}
        }
        composable<ScreenB> {
            val args = it.toRoute<ScreenB>()
            Log.d("ScreenB", "name: ${args.double}")
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center){
                Text("Screen B: ${args.double}")
            }
        }
//        composable<ScreenQrScanner> {
//            CameraScreen(
//                onDocumentFound = {navController.navigate(
//                    RecyclingPoint(
//                        latitude = it!!.latitude,
//                        longitude = it.longitude,
//                        description = it.description
//                    ))
//                }
//            )
//        }
//        composable<RecyclingPoint>(){
//            val args = it.toRoute<RecyclingPoint>()
//            RecycleFormScreen(
//                navController = navController,
//                document = args
//            )
//        }
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
            HistoryScreen( onCancel = {})
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
                },
                onChangeContrastLevel = onChangeContrastLevel,
                contrastLevel = contrastLevel
            )
        }
    }
}
@Serializable
object ScreenLogin

@Serializable
object ScreenMap
@Serializable
object ScreenA

@Serializable
data class ScreenB(val double: Double?)

@Serializable
object ScreenQrScanner

@Serializable
object ScreenRewards
@Serializable
data class RecyclingPoint(val latitude: Double, val longitude: Double, val description: String)

@Serializable
data class ScreenRewardConfimation(val rewardTitle:String, val rewardCost:Int, val userPoints:Int)
@Serializable
object ScreenHistory

@Serializable
object ScreenProfile

