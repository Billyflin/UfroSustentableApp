package com.example.ufrosustentableapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ufrosustentableapp.presentation.GoogleSignInButton
import com.example.ufrosustentableapp.ui.theme.UfroSustentableAppTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UfroSustentableAppTheme {
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

                AppNavHost(navController, user, token, launcher, context)
            }
        }
    }
}
@Composable
fun AppNavHost(
    navController: NavHostController,
    user: FirebaseUser?,
    token: String,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    context: Context
) {
    NavHost(
        navController = navController,
        startDestination = if (user == null) ScreenLogin else ScreenMap
    ) {
        composable<ScreenLogin> {
            LoginScreen(token = token, launcher = launcher, context = context)
        }
        composable<ScreenMap> {
            LoggedInContent(user, navController) {
                Firebase.auth.signOut()
                navController.navigate(ScreenLogin) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
        composable<ScreenB> { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name")
            Text("Screen B: $name")
        }
        composable<ScreenQrScanner> {
            Text("QR Scanner")
        }
    }
}

@Composable
fun LoginScreen(token: String, launcher: ManagedActivityResultLauncher<Intent, ActivityResult>, context: Context) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isSystemInDarkTheme()) listOf(Color(0xFF2A5C2C), Color(0xFF3E8717))
                    else
                        listOf(Color.White, Color(0xFF3E8717)),
                    startY = 10f,
                    endY = 3000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                colorFilter = if (isSystemInDarkTheme())ColorFilter.tint(Color(0xFFA9D194)) else null,
                modifier = Modifier
                    .size(320.dp)
                    .padding(bottom = 26.dp)
            )
            GoogleSignInButton(onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(token)
                    .requestEmail()
                    .requestProfile()
                    .build()
                val signInClient = GoogleSignIn.getClient(context, gso)
                val signInIntent = signInClient.signInIntent
                launcher.launch(signInIntent)
            })
        }
    }
}


@Composable
fun LoggedInContent(user: FirebaseUser?, navController: NavHostController, onSignOut: () -> Unit) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                shape = CircleShape,
                onClick = { navController.navigate(ScreenQrScanner) },
                modifier = Modifier.padding(bottom = 16.dp) // Ajusta el padding para mover el FAB hacia abajo
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_qr_code_scanner_24),
                    contentDescription = "QR Scanner",
                    modifier = Modifier.size(36.dp) // Ajusta el tamaño del ícono
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user?.photoUrl)
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                )
                Button(onClick = {
                    navController.navigate(ScreenB(name=user!!.displayName))
                }) {
                    Text("Go to Screen B")
                }
                Spacer(Modifier.height(8.dp))
                Text("Welcome ${user?.displayName}")
                Spacer(Modifier.height(10.dp))
                Button(onClick = {
                    onSignOut()
                }) {
                    Text("Sign out")
                }
                MapsExample()
            }
        }
    }
}

@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
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

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    BottomAppBar(
        actions = {
            IconButton(onClick = {
                navController.navigate(ScreenMap) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }) {
                Icon(Icons.Default.Home, contentDescription = "Screen A")
            }
            Spacer(modifier = Modifier.weight(1f, true)) // This is to balance the FAB in the center
            IconButton(onClick = {
                navController.navigate(ScreenB) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }) {
                Icon(Icons.Default.Person, contentDescription = "Screen B")
            }
        }
    )
}

@Serializable
object ScreenInit
@Serializable
object ScreenLogin

@Serializable
object ScreenMap

@Serializable
data class ScreenB(val name: String?)

@Serializable
object ScreenQrScanner




@Composable
fun MapsExample() {
    val universidadDeLaFrontera = LatLng(-38.74658429580099, -72.6157555230996)
    val cameraPositionState = rememberCameraPositionState {
        CameraPosition.fromLatLngZoom(universidadDeLaFrontera, 16f).also { position = it }
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false
        )
    ) {
        Marker(
            state = MarkerState(position = universidadDeLaFrontera),
            title = "Temuco",
            snippet = "Temuco, Chile"
        )
        Marker(
            state = MarkerState(position = LatLng(-38.74, -72.59)),
            title = "Padre Las Casas",
            snippet = "Padre Las Casas, Chile"
        )
    }
}
