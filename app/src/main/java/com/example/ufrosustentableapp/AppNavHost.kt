package com.example.ufrosustentableapp

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.ufrosustentableapp.screen.CameraScreen
import com.example.ufrosustentableapp.screen.MapScreen
import com.example.ufrosustentableapp.screen.ProfileScreen
import com.example.ufrosustentableapp.screen.RecycleFormScreen
import com.example.ufrosustentableapp.screen.RequestHistoryScreen
import com.example.ufrosustentableapp.screen.RewardConfirmationScreen
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
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    isDynamicColor: Boolean,
    onToggleDynamicColor: () -> Unit,
    contrastLevel: ContrastLevel,
    onChangeContrastLevel: (ContrastLevel) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = ScreenMap
//        startDestination = ScreenA
    ) {
        composable<ScreenMap> {
            MapScreen()
        }
        composable<ScreenQrScanner> {
            CameraScreen(
                onDocumentFound = {
                    navController.navigate(
                        FormRecycle(it!!)
                    )
                }
            )
        }
        composable<FormRecycle> {
            val args = it.toRoute<FormRecycle>()
            RecycleFormScreen(
                navController = navController,
                data = args.data
            )
        }
        composable<ScreenRewards> {
            Log.d("AppNavHost", "user: ${user?.uid}")
//            RewardsScreen(
//                navController = navController,
//                userId = user?.uid ?: ""
//            )
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
            RequestHistoryScreen(navController = navController, userId = user?.uid ?: "")
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
data class FormRecycle(val data: String)

@Serializable
data class ScreenRewardConfimation(
    val rewardTitle: String,
    val rewardCost: Int,
    val userPoints: Int
)

@Serializable
object ScreenHistory

@Serializable
object ScreenProfile
