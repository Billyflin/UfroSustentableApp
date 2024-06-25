package com.example.ufrosustentableapp

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.ufrosustentableapp.model.RecyclingRequest
import com.example.ufrosustentableapp.model.RequestStatus
import com.example.ufrosustentableapp.screen.CameraScreen
import com.example.ufrosustentableapp.screen.HistoryScreen
import com.example.ufrosustentableapp.screen.MapScreen
import com.example.ufrosustentableapp.screen.ProfileScreen
import com.example.ufrosustentableapp.screen.RecycleFormScreen
import com.example.ufrosustentableapp.screen.RequestHistoryScreen
import com.example.ufrosustentableapp.screen.RewardConfirmationScreen
import com.example.ufrosustentableapp.screen.RewardsScreen
import com.example.ufrosustentableapp.ui.theme.ContrastLevel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.serialization.Serializable
import java.util.Locale

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
        startDestination = ScreenHistory
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
            RewardsScreen(
                navController = navController,
                userId = user?.uid ?: ""
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
        composable<ScreenRequestDetail> { it ->
            val args = it.toRoute<ScreenRequestDetail>()
            Log.d("AppNavHost", "requestId: ${args.requestId}")
            val request by getRequestById(args.requestId)

            request?.let { it2 ->
                HistoryScreen(
                    title = "Detalle de Solicitud",
                    activeProgressBar = when (it2.status) {
                        RequestStatus.PROCESSING -> 0
                        RequestStatus.VALIDATING -> 1
                        RequestStatus.REWARD -> 2
                        RequestStatus.UNKNOWN -> 0
                    },
                    requestTime = it2.requestTime,
                    updateTime = it2.updateTime,
                    status = it2.status,
                    onCancel = { /* Lógica para cancelar */ }
                )
            } ?: run {
                // Muestra un estado de carga o error
                Text(text = "Cargando...")
            }
        }
    }
}

@Composable
fun getRequestById(requestId: String): State<RecyclingRequest?> {
    val result = remember { mutableStateOf<RecyclingRequest?>(null) }

    LaunchedEffect(requestId) {
        val db = FirebaseFirestore.getInstance()
        db.collection("recycling_requests").document(requestId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val status = try {
                        RequestStatus.valueOf(
                            document.getString("status")?.uppercase(Locale.ROOT) ?: "PROCESSING")
                    } catch (e: IllegalArgumentException) {
                        RequestStatus.PROCESSING // Valor predeterminado en caso de error
                    }
                    val request = RecyclingRequest(
                        id = document.id,
                        userId = document.getString("userId") ?: "",
                        materialType = document.getString("materialType") ?: "",
                        quantityKg = document.getDouble("quantityKg") ?: 0.0,
                        photoUrl = document.getString("photoUrl") ?: "",
                        status = status,
                        requestTime = document.getTimestamp("timestamp")?.toDate()?.toString() ?: "",
                        updateTime = document.getTimestamp("updateTime")?.toDate()?.toString() ?: ""
                    )
                    result.value = request
                } else {
                    result.value = null
                }
            }
            .addOnFailureListener {
                result.value = null // En caso de error, devolver null
            }
    }

    return result
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
