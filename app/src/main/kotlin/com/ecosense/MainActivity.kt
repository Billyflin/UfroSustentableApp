package com.ecosense

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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ecosense.presentation.BottomNavigationBar
import com.ecosense.screen.LoginScreen
import com.ecosense.ui.theme.AppTheme
import com.ecosense.ui.theme.ContrastLevel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")
        enableEdgeToEdge()
        val preferencesManager = PreferencesManager(applicationContext)

        setContent {
            val initColor = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(initColor) }
            var isDynamicColor by remember { mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) }
            var contrastLevel by remember { mutableStateOf(ContrastLevel.NORMAL) }

            LaunchedEffect(Unit) {
                preferencesManager.preferencesFlow.collect { prefs ->
                    isDarkMode = prefs.darkMode
                    isDynamicColor = prefs.dynamicColor
                    contrastLevel = prefs.contrastLevel
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
                    val listener = FirebaseAuth.AuthStateListener { auth ->
                        user = auth.currentUser
                    }
                    Firebase.auth.addAuthStateListener(listener)
                    onDispose { Firebase.auth.removeAuthStateListener(listener) }
                }

                val backstackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backstackEntry?.destination
                val isOnQrScanner = currentDestination?.hasRoute(ScreenQrScanner::class) == true

                Scaffold(
                    topBar = {
                        if (user != null && !isOnQrScanner) {
                            TopAppBar(
                                title = {},
                                navigationIcon = {
                                    Image(
                                        painter = painterResource(id = R.drawable.ecosenselogo),
                                        contentDescription = "EcoSense",
                                        colorFilter = ColorFilter.tint(colorScheme.primary),
                                        modifier = Modifier.size(140.dp).padding(horizontal = 8.dp)
                                    )
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (user != null && !isOnQrScanner) {
                            BottomNavigationBar(navController, user)
                        }
                    }
                ) { innerPadding ->
                    if (user != null) {
                        AppNavHost(
                            modifier = Modifier.padding(innerPadding),
                            navController = navController,
                            user = user,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = {
                                isDarkMode = !isDarkMode
                                lifecycleScope.launch { preferencesManager.updateDarkMode(isDarkMode) }
                            },
                            isDynamicColor = isDynamicColor,
                            onToggleDynamicColor = {
                                isDynamicColor = !isDynamicColor
                                lifecycleScope.launch { preferencesManager.updateDynamicColor(isDynamicColor) }
                            },
                            onChangeContrastLevel = { newLevel ->
                                contrastLevel = newLevel
                                lifecycleScope.launch { preferencesManager.updateContrastLevel(newLevel) }
                            },
                            contrastLevel = contrastLevel,
                        )
                    } else {
                        LoginScreen(
                            context = context,
                            onSignInSuccess = {
                                navController.navigate(ScreenMap)
                            }
                        )
                    }
                }
            }
        }
    }
}



