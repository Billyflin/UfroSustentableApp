package com.example.ufrosustentableapp

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate: Activity created")
        enableEdgeToEdge()
        val preferencesManager = PreferencesManager(applicationContext)

        setContent {
            val initColor = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(initColor) }
            var isDynamicColor by remember { mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) }
            var contrastLevel by remember { mutableStateOf(ContrastLevel.NORMAL) }

            // Load user preferences
            LaunchedEffect(Unit) {
                Log.d("MainActivity", "LaunchedEffect: Loading user preferences")
                preferencesManager.preferencesFlow.collect { userPreferences ->
                    isDarkMode = userPreferences.darkMode
                    isDynamicColor = userPreferences.dynamicColor
                    contrastLevel = userPreferences.contrastLevel
                    Log.d("MainActivity", "User preferences loaded: darkMode=$isDarkMode, dynamicColor=$isDynamicColor, contrastLevel=$contrastLevel")
                }
            }
            AppTheme(
                darkTheme = isDarkMode,
                dynamicColor = isDynamicColor,
                contrastLevel = contrastLevel
            ) {
                val colorScheme = MaterialTheme.colorScheme
                val navController = rememberNavController()
                var user by remember { mutableStateOf(Firebase.auth.currentUser) }
                val context = LocalContext.current

                DisposableEffect(Unit) {
                    Log.d("MainActivity", "DisposableEffect: Adding Firebase AuthStateListener")
                    val authStateListener = FirebaseAuth.AuthStateListener { auth ->
                        user = auth.currentUser
                        Log.d("MainActivity", "AuthStateListener: User state changed, user=${user?.uid}")
                    }
                    Firebase.auth.addAuthStateListener(authStateListener)
                    onDispose {
                        Firebase.auth.removeAuthStateListener(authStateListener)
                        Log.d("MainActivity", "DisposableEffect: AuthStateListener removed")
                    }
                }

                val backstackEntry = navController.currentBackStackEntryAsState()
                val currentScreen = backstackEntry.value?.destination?.route
                Log.d("MainActivity", "Current screen: $currentScreen")

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
                ) { innerPadding ->
                    if (user != null) {
                        Log.d("MainActivity", "User is logged in, displaying AppNavHost")
                        AppNavHost(
                            Modifier.padding(innerPadding),
                            navController = navController,
                            user = user,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = {
                                isDarkMode = !isDarkMode
                                lifecycleScope.launch { preferencesManager.updateDarkMode(isDarkMode) }
                                Log.d("MainActivity", "Dark mode toggled: $isDarkMode")
                            },
                            isDynamicColor = isDynamicColor,
                            onToggleDynamicColor = {
                                isDynamicColor = !isDynamicColor
                                lifecycleScope.launch { preferencesManager.updateDynamicColor(isDynamicColor) }
                                Log.d("MainActivity", "Dynamic color toggled: $isDynamicColor")
                            },
                            onChangeContrastLevel = { newLevel ->
                                contrastLevel = newLevel
                                lifecycleScope.launch { preferencesManager.updateContrastLevel(newLevel) }
                                Log.d("MainActivity", "Contrast level changed: $newLevel")
                            },
                            contrastLevel = contrastLevel,
                        )
                    } else {
                        Log.d("MainActivity", "User is not logged in, displaying LoginScreen")
                        LoginScreen(context = context) {
                            navController.navigate(ScreenMap)
                            Log.d("MainActivity", "Navigating to ScreenMap after login")
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
    Log.d("initializeRewards", "Initializing rewards collection")

    val rewardTitles = listOf(
        "Café Gratis", "Descuento en Tienda", "Entrada al Cine", "Tarjeta de Regalo", "Producto Ecológico"
    )

    val batch = db.batch()

    rewardTitles.forEach { title ->
        val pointsRequired = Random.nextInt(50, 2000) // Genera un número aleatorio entre 50 y 2000
        val reward = mapOf(
            "title" to title,
            "pointsRequired" to pointsRequired
        )
        val newRewardRef = rewardsCollection.document()
        batch.set(newRewardRef, reward)
        Log.d("initializeRewards", "Reward added: title=$title, pointsRequired=$pointsRequired")
    }

    batch.commit()
        .addOnSuccessListener {
            Log.d("initializeRewards", "Recompensas inicializadas correctamente")
        }
        .addOnFailureListener { e ->
            Log.e("initializeRewards", "Error al inicializar recompensas: ${e.message}")
        }
}
