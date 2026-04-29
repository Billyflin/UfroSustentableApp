package com.ecosense

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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ecosense.model.RequestStatus
import com.ecosense.screen.CameraScreen
import com.ecosense.screen.GruposScreen
import com.ecosense.screen.HistoryScreen
import com.ecosense.screen.MapScreen
import com.ecosense.screen.ProfileScreen
import com.ecosense.screen.RankingScreen
import com.ecosense.screen.RecycleFormScreen
import com.ecosense.screen.RequestHistoryScreen
import com.ecosense.screen.RewardConfirmationScreen
import com.ecosense.screen.RewardsScreen
import com.ecosense.ui.theme.ContrastLevel
import com.ecosense.viewmodel.HistoryViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.serialization.Serializable

// ── Rutas (NavKey) ────────────────────────────────────────────────────────────
// Cada objeto/clase anotado con @Serializable implementa NavKey.
// Navigation3 serializa el back stack para sobrevivir process death.

@Serializable data object ScreenMap              : NavKey
@Serializable data object ScreenQrScanner        : NavKey
@Serializable data object ScreenRewards          : NavKey
@Serializable data object ScreenHistory          : NavKey
@Serializable data object ScreenProfile          : NavKey
@Serializable data object ScreenGrupos           : NavKey
@Serializable data object ScreenRanking          : NavKey

@Serializable data class ScreenRequestDetail(val requestId: String)   : NavKey
@Serializable data class ScreenRewardConfimation(
    val rewardTitle: String,
    val rewardCost:  Int,
    val userPoints:  Int
) : NavKey
@Serializable data class FormRecycle(val data: String) : NavKey

// No es una ruta — se usa para deserializar datos QR
@Serializable data class RecyclingPoint(
    val latitude:    Double,
    val longitude:   Double,
    val description: String
)

// ── Navegación principal ──────────────────────────────────────────────────────

/**
 * Reemplaza AppNavHost con Navigation3:
 *  - [NavDisplay] en lugar de NavHost
 *  - [backStack] es un SnapshotStateList observable; navegar = backStack.add()
 *  - Las pantallas reciben lambdas de navegación, no NavController
 */
@Composable
fun AppNavigation(
    modifier:             Modifier = Modifier,
    backStack:            NavBackStack<NavKey>,
    user:                 FirebaseUser?,
    isDarkMode:           Boolean,
    onToggleDarkMode:     () -> Unit,
    isDynamicColor:       Boolean,
    onToggleDynamicColor: () -> Unit,
    contrastLevel:        ContrastLevel,
    onChangeContrastLevel:(ContrastLevel) -> Unit
) {
    NavDisplay(
        modifier  = modifier,
        backStack = backStack,
        onBack    = { if (backStack.size > 1) backStack.removeLast() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(), // persiste state Compose
            rememberViewModelStoreNavEntryDecorator()        // scopa ViewModels a la entrada
        ),
        entryProvider = entryProvider {

            entry<ScreenMap> {
                MapScreen()
            }

            entry<ScreenQrScanner> {
                CameraScreen(
                    onDocumentFound = { data -> backStack.add(FormRecycle(data ?: "")) }
                )
            }

            entry<FormRecycle> { key ->
                RecycleFormScreen(
                    onNavigateToHistory = {
                        // Pop hasta la raíz (ScreenHistory) tras enviar la solicitud
                        while (backStack.size > 1) backStack.removeLast()
                    },
                    data = key.data.ifBlank { null }
                )
            }

            entry<ScreenRewards> {
                RewardsScreen(
                    onNavigateToConfirmation = { title, cost, points ->
                        backStack.add(ScreenRewardConfimation(title, cost, points))
                    },
                    userId = user?.uid ?: ""
                )
            }

            entry<ScreenRewardConfimation> { key ->
                RewardConfirmationScreen(
                    onBack      = { backStack.removeLastOrNull() },
                    rewardTitle = key.rewardTitle,
                    rewardCost  = key.rewardCost,
                    userPoints  = key.userPoints
                )
            }

            entry<ScreenHistory> {
                RequestHistoryScreen(
                    onNavigateToDetail = { id -> backStack.add(ScreenRequestDetail(id)) },
                    userId = user?.uid ?: ""
                )
            }

            entry<ScreenProfile> {
                ProfileScreen(
                    user                  = user,
                    onToggleDarkMode      = onToggleDarkMode,
                    onToggleDynamicColor  = onToggleDynamicColor,
                    isDarkMode            = isDarkMode,
                    isDynamicColor        = isDynamicColor,
                    onLogout              = { Firebase.auth.signOut() },
                    onChangeContrastLevel = onChangeContrastLevel,
                    contrastLevel         = contrastLevel,
                    onVerPremios          = { backStack.add(ScreenRewards) }
                )
            }

            entry<ScreenGrupos> {
                GruposScreen(
                    onNavigateToRanking = { backStack.add(ScreenRanking) },
                    userId   = user?.uid ?: "",
                    userName = user?.displayName ?: "Usuario"
                )
            }

            entry<ScreenRanking> {
                RankingScreen(onBack = { backStack.removeLastOrNull() })
            }

            entry<ScreenRequestDetail> { key ->
                val historyViewModel = viewModel<HistoryViewModel>()
                val request          by historyViewModel.requestDetail.collectAsStateWithLifecycle()

                LaunchedEffect(key.requestId) {
                    historyViewModel.loadRequestDetail(key.requestId)
                }

                val req = request
                if (req != null) {
                    HistoryScreen(
                        onBack            = { backStack.removeLastOrNull() },
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
                        requestId    = key.requestId,
                        onCancel     = {}
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    )
}
