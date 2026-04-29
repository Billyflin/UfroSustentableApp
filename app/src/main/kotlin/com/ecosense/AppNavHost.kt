package com.ecosense

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
        modifier          = modifier,
        navController     = navController,
        startDestination  = ScreenHistory,
        enterTransition   = { fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) },
        exitTransition    = { fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) },
        popEnterTransition  = { fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) },
        popExitTransition   = { fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) }
    ) {
        composable<ScreenMap> {
            MapScreen()
        }
        composable<ScreenQrScanner> {
            CameraScreen(
                onDocumentFound = { data ->
                    navController.navigate(FormRecycle(data ?: ""))
                }
            )
        }
        composable<FormRecycle> {
            val args = it.toRoute<FormRecycle>()
            RecycleFormScreen(navController = navController, data = args.data.ifBlank { null })
        }
        composable<ScreenRewards> {
            Log.d("AppNavHost", "userId: ${user?.uid}")
            RewardsScreen(navController = navController, userId = user?.uid ?: "")
        }
        composable<ScreenRewardConfimation> {
            val args = it.toRoute<ScreenRewardConfimation>()
            RewardConfirmationScreen(
                navController = navController,
                rewardTitle   = args.rewardTitle,
                rewardCost    = args.rewardCost,
                userPoints    = args.userPoints
            )
        }
        composable<ScreenHistory> {
            RequestHistoryScreen(navController = navController, userId = user?.uid ?: "")
        }
        composable<ScreenProfile> {
            ProfileScreen(
                user                   = user,
                onToggleDarkMode       = onToggleDarkMode,
                onToggleDynamicColor   = onToggleDynamicColor,
                isDarkMode             = isDarkMode,
                isDynamicColor         = isDynamicColor,
                onLogout               = { Firebase.auth.signOut() },
                onChangeContrastLevel  = onChangeContrastLevel,
                contrastLevel          = contrastLevel
            )
        }
        composable<ScreenRequestDetail> { backStackEntry ->
            val args             = backStackEntry.toRoute<ScreenRequestDetail>()
            val historyViewModel = viewModel<HistoryViewModel>()
            val request          by historyViewModel.requestDetail.collectAsStateWithLifecycle()

            LaunchedEffect(args.requestId) {
                historyViewModel.loadRequestDetail(args.requestId)
            }

            val req = request
            if (req != null) {
                HistoryScreen(
                    navController     = navController,
                    viewModel         = historyViewModel,
                    activeProgressBar = when (req.status) {
                        RequestStatus.PROCESSING -> 0
                        RequestStatus.VALIDATING -> 1
                        RequestStatus.REWARD     -> 2
                        RequestStatus.REEDEMED   -> 3
                        else                     -> 0
                    },
                    requestTime  = req.requestTime,
                    updateTime   = req.updateTime,
                    imageUrl     = req.photoUrl,
                    description  = req.description,
                    status       = req.status,
                    reward       = req.reward,
                    userId       = user?.uid ?: "",
                    requestId    = args.requestId,
                    onCancel     = {}
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// ── Rutas de navegación (type-safe) ──────────────────────────────────────────

@Serializable object ScreenMap
@Serializable object ScreenQrScanner
@Serializable object ScreenRewards
@Serializable object ScreenHistory
@Serializable object ScreenProfile

@Serializable data class ScreenRequestDetail(val requestId: String)
@Serializable data class ScreenRewardConfimation(
    val rewardTitle: String,
    val rewardCost:  Int,
    val userPoints:  Int
)
@Serializable data class FormRecycle(val data: String)
@Serializable data class RecyclingPoint(
    val latitude:    Double,
    val longitude:   Double,
    val description: String
)
