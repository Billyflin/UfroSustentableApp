package com.example.ufrosustentableapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ufrosustentableapp.presentation.BottomNavigationBar
import com.example.ufrosustentableapp.screen.LoginScreen
import com.example.ufrosustentableapp.ui.theme.AppTheme
import com.example.ufrosustentableapp.ui.theme.ContrastLevel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        initializeRewards()
        setContent {
            val initColor = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(initColor) }
            var isDynamicColor by remember { mutableStateOf(true) }
            var contrastLevel by remember { mutableStateOf(ContrastLevel.NORMAL) }
            AppTheme(
                darkTheme = isDarkMode,
                dynamicColor = isDynamicColor,
                contrastLevel = contrastLevel
            ) {
                val colorScheme = MaterialTheme.colorScheme
                val navController = rememberNavController()
                var user by remember { mutableStateOf(Firebase.auth.currentUser) }
                val context = LocalContext.current

                // Listener to update user state on auth state changes
                DisposableEffect(Unit) {
                    val authStateListener = FirebaseAuth.AuthStateListener { auth ->
                        user = auth.currentUser
                    }
                    Firebase.auth.addAuthStateListener(authStateListener)
                    onDispose {
                        Firebase.auth.removeAuthStateListener(authStateListener)
                    }
                }

                val backstackEntry = navController.currentBackStackEntryAsState()
                val currentScreen = backstackEntry.value?.destination?.route

                Scaffold(
                    topBar = {
                        if (user != null) {
                            TopAppBar(
                                title = { Text("Sustentable") },
                                navigationIcon = {
                                    Image(
                                        painter = painterResource(id = R.drawable.ufro_sustentable_app_logo),
                                        contentDescription = "Logo",
                                        colorFilter = ColorFilter.tint(colorScheme.primary),
                                        modifier = Modifier
                                            .size(150.dp)
                                            .padding(8.dp)
                                    )
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (user != null && currentScreen != "ScreenQrScanner") {
                            BottomNavigationBar(navController, user)
                        }
                    }
                ) {
                    if (user != null) {
                        AppNavHost(
                            navController = navController,
                            user = user,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { isDarkMode = !isDarkMode },
                            isDynamicColor = isDynamicColor,
                            onToggleDynamicColor = { isDynamicColor = !isDynamicColor },
                            onChangeContrastLevel = { newLevel -> contrastLevel = newLevel },
                            contrastLevel = contrastLevel,
                        )
                    } else {
                        LoginScreen(context = context) {
                            navController.navigate(ScreenMap)
                        }
                    }
                }
            }
        }
    }
}

fun initializeRewards() {
    val db = FirebaseFirestore.getInstance()
    val rewardsCollection = db.collection("rewards")

    val rewardTitles = listOf(
        "Café Gratis", "Descuento en Tienda", "Entrada al Cine", "Tarjeta de Regalo", "Producto Ecológico"
    )

    val batch = db.batch()

    rewardTitles.forEach { title ->
        val pointsRequired = Random.nextInt(50, 2000) // Genera un número aleatorio entre 50 y 200
        val reward = mapOf(
            "title" to title,
            "pointsRequired" to pointsRequired
        )
        val newRewardRef = rewardsCollection.document()
        batch.set(newRewardRef, reward)
    }

    batch.commit()
        .addOnSuccessListener {
            println("Recompensas inicializadas correctamente")
        }
        .addOnFailureListener { e ->
            println("Error al inicializar recompensas: ${e.message}")
        }
}




