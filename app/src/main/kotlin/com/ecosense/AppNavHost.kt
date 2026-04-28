package com.ecosense

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ecosense.model.RequestStatus
import com.ecosense.screen.CameraScreen
import com.ecosense.screen.HistoryScreen
import com.ecosense.screen.MapScreen
import com.ecosense.screen.ProfileScreen
import com.ecosense.screen.RecycleFormScreen
import com.ecosense.screen.RequestHistoryScreen
import com.ecosense.screen.RewardConfirmationScreen
import com.ecosense.screen.RewardsScreen
import com.ecosense.ui.theme.ContrastLevel
import com.ecosense.viewmodel.HistoryViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import kotlinx.serialization.Serializable

@Composable
fun AppNavHost(
    modifier: Modifier,
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
        modifier = modifier,
        navController = navController,
        startDestination = ScreenHistory
    ) {
        composable<ScreenMap> {
            MapScreen()
        }
        composable<ScreenQrScanner> {
            CameraScreen(
                onDocumentFound = { data ->
                    navController.navigate(FormRecycle(data!!))
                }
            )
        }
        composable<FormRecycle> {
            val args = it.toRoute<FormRecycle>()
            RecycleFormScreen(navController = navController, data = args.data)
        }
        composable<ScreenRewards> {
            Log.d("AppNavHost", "user: ${user?.uid}")
            RewardsScreen(navController = navController, userId = user?.uid ?: "")
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
                onLogout = { Firebase.auth.signOut() },
                onChangeContrastLevel = onChangeContrastLevel,
                contrastLevel = contrastLevel
            )
        }
        composable<ScreenRequestDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<ScreenRequestDetail>()
            Log.d("AppNavHost", "requestId: ${args.requestId}")

            val historyViewModel: HistoryViewModel = viewModel()
            val request by historyViewModel.requestDetail.collectAsStateWithLifecycle()

            LaunchedEffect(args.requestId) {
                historyViewModel.loadRequestDetail(args.requestId)
            }

            request?.let { req ->
                HistoryScreen(
                    navController = navController,
                    viewModel = historyViewModel,
                    activeProgressBar = when (req.status) {
                        RequestStatus.PROCESSING -> 0
                        RequestStatus.VALIDATING -> 1
                        RequestStatus.REWARD -> 2
                        RequestStatus.UNKNOWN -> 0
                        RequestStatus.REEDEMED -> 3
                        RequestStatus.REJECTED -> 0
                    },
                    requestTime = req.requestTime,
                    updateTime = req.updateTime,
                    imageUrl = req.photoUrl,
                    description = req.description,
                    status = req.status,
                    reward = req.reward,
                    userId = user?.uid ?: "",
                    requestId = args.requestId,
                    onCancel = {}
                )
            } ?: Text(text = "Cargando...")
        }
    }
}

@Serializable
data class ScreenRequestDetail(val requestId: String)

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


