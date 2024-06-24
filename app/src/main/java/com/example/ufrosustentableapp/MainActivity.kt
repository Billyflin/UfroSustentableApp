package com.example.ufrosustentableapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ufrosustentableapp.presentation.BottomNavigationBar
import com.example.ufrosustentableapp.ui.theme.AppTheme
import com.example.ufrosustentableapp.ui.theme.ContrastLevel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val initColor=isSystemInDarkTheme()
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
                val launcher = rememberFirebaseAuthLauncher(
                    onAuthComplete = { result -> user = result.user },
                    onAuthError = { user = null }
                )
                val token = stringResource(R.string.default_web_client_id)
                val context = LocalContext.current

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
                val recyclingPoints = remember { mutableStateListOf<RecyclingPoint>() }

                Firebase.firestore.collection("recycling_points")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            val point = RecyclingPoint(
                                latitude = document.getDouble("latitude") ?: 0.0,
                                longitude = document.getDouble("longitude") ?: 0.0,
                                description = document.getString("description") ?: ""
                            )
                            recyclingPoints.add(point)
                            Log.d("MainActivity", "${document.id} => ${document.data}")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("MainActivity", "Error getting documents: ", exception)
                    }
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
                    AppNavHost(
                        navController = navController,
                        user = user,
                        token = token,
                        launcher = launcher,
                        context = context,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { isDarkMode = !isDarkMode },
                        isDynamicColor = isDynamicColor,
                        onToggleDynamicColor = { isDynamicColor = !isDynamicColor },
                        recyclingPoints = recyclingPoints,
                        onChangeContrastLevel = { newLevel -> contrastLevel = newLevel },
                        contrastLevel = contrastLevel,

                    )
                }
            }
        }
    }
}


@Composable
fun rememberFirebaseAuthLauncher(onAuthComplete: (AuthResult) -> Unit,onAuthError: (ApiException) -> Unit): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val scope = rememberCoroutineScope()
    return rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            scope.launch {
                val authResult = Firebase.auth.signInWithCredential(credential).await()
                onAuthComplete(authResult)
            }
        } catch (e: ApiException) {
            onAuthError(e)
        }
    }
}

