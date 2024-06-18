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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.rememberNavController
import com.example.ufrosustentableapp.presentation.BottomNavigationBar
import com.example.ufrosustentableapp.presentation.GoogleSignInButton
import com.example.ufrosustentableapp.ui.theme.AppTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
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
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
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
                    topBar = {
                        if (user != null) {
                             TopAppBar(
                                title = { Text("Ufro Sustentable") },
                                navigationIcon = {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo),
                                        contentDescription = "Logo",
                                        colorFilter = if (isSystemInDarkTheme()) ColorFilter.tint(Color(0xFFA9D194)) else null,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .padding(8.dp)
                                    )
                                }
                             )
                        }
                    },
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
    val colorScheme = MaterialTheme.colorScheme
    val gradientColors = if (isSystemInDarkTheme())
        listOf(colorScheme.secondary, colorScheme.primary)
    else
        listOf(Color.White, colorScheme.primary)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = 1000f
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
                colorFilter = if (isSystemInDarkTheme()) ColorFilter.tint(colorScheme.secondary) else null,
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
fun LoggedInContent() {
    Box {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MapsExample()
        }
    }
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
