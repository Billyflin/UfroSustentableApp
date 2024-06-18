package com.example.ufrosustentableapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import java.util.concurrent.Executors


class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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


                Scaffold(
                    bottomBar = {
                        if (user != null){
                            BottomNavigationBar(navController, user)
                        }
                    }
                ) {
                    AppNavHost(navController, user, token, launcher, context)
                }
            }
        }
    }
}
@Composable
fun BottomNavigationBar(navController: NavHostController, user: FirebaseUser?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(146.dp)
            .padding(bottom = 70.dp)
            .background(Color.Transparent)
        ,
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(percent = 20),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                IconButton(onClick = {
                    navController.navigate(ScreenMap) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }) {
                    Icon(Icons.Default.Home,  modifier = Modifier
                        .size(30.dp),
                        contentDescription = "Inicio", tint = Color.Gray)
                }

                IconButton(onClick = {
                    navController.navigate(ScreenHistory) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }) {
                    Icon(Icons.Default.DateRange,  modifier = Modifier
                        .size(30.dp),
                        contentDescription = "Historial", tint = Color.Gray)
                }

                Spacer(modifier = Modifier.width(56.dp)) // Espacio para el FAB

                IconButton(onClick = {
                    navController.navigate(ScreenRewards) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }) {
                    Icon(Icons.Default.Star,
                        contentDescription = "Recompensas",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(30.dp)

                    )
                }

                IconButton(onClick = {
                    navController.navigate(ScreenProfile) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user?.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate(ScreenQrScanner) },
            containerColor = Color(0xFF00AA5B),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_qr_code_scanner_24),
                contentDescription = "QR Scanner",
                modifier = Modifier.size(36.dp)
            )
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
            LoggedInContent(user)
        }
        composable<ScreenB> { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name")
            Text("Screen B: $name")
        }
        composable<ScreenQrScanner> {
            QrScannerScreen()
        }
        composable<ScreenRewards> {
            RewardsScreen()
        }
        composable<ScreenHistory> {
            HistoryScreen()
        }
        composable<ScreenProfile> {
            ProfileScreen(user){
                Firebase.auth.signOut()
                navController.navigate(ScreenLogin) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(user: FirebaseUser?, content: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user?.photoUrl)
                    .crossfade(true)
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(16.dp))
            Text(user?.displayName ?: "Usuario")
            Spacer(Modifier.height(16.dp))
            FloatingActionButton(
                onClick = {
                    content()
                },
                containerColor = Color(0xFF00AA5B),
                contentColor = Color.White
            ) {
                Text("Cerrar sesión")
            }
        }
    }

}

@Composable
fun HistoryScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("History Screen")
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


@Composable
fun RewardsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Rewards Screen")
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
fun LoggedInContent(user: FirebaseUser?) {
    Box {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome ${user?.displayName}")
            Spacer(Modifier.height(10.dp))
            MapsExample()
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview
@Composable
fun NavBarPreview() {
    BottomNavigationBar(rememberNavController(), null)
}
@Serializable
object ScreenLogin


@Serializable
object ScreenMap

@Serializable
data class ScreenB(val name: String?)

@Serializable
object ScreenQrScanner

@Serializable
object ScreenRewards

@Serializable
object ScreenHistory

@Serializable
object ScreenProfile

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

@Composable
fun QrScannerScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { context ->
                    PreviewView(context).apply {
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(this.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setTargetResolution(Size(1280, 720))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(Executors.newSingleThreadExecutor(), QRCodeAnalyzer { qrCode ->
                                        Log.d("QrScanner", "QR Code scanned: $qrCode")
                                        // Manejar el resultado del escaneo del QR aquí

                                        Toast.makeText(context, qrCode, Toast.LENGTH_SHORT).show()
                                    })
                                }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                                )
                            } catch (exc: Exception) {
                                Log.e("CameraXApp", "Use case binding failed", exc)
                            }
                        }, ContextCompat.getMainExecutor(context))
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "Por favor, permite el acceso a la cámara para escanear códigos QR",
            )
        }
    }
}


class QRCodeAnalyzer(private val onQrCodeScanned: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE)))
    }
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val buffer = mediaImage.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val source = PlanarYUVLuminanceSource(
                bytes,
                mediaImage.width,
                mediaImage.height,
                0,
                0,
                mediaImage.width,
                mediaImage.height,
                false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            try {
                val result = reader.decode(binaryBitmap)
                onQrCodeScanned(result.text)
            } catch (e: NotFoundException) {
                // No QR code found
            } finally {
                imageProxy.close()
            }
        }
    }
}
