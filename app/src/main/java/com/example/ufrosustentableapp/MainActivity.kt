package com.example.ufrosustentableapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ufrosustentableapp.ui.theme.UfroSustentableAppTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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
                NavHost(
                    navController = navController,
                    startDestination = ScreenA
                ) {
                    composable<ScreenA> {
                        var user by remember { mutableStateOf(Firebase.auth.currentUser) }
                        val launcher = rememberFirebaseAuthLauncher(
                            onAuthComplete = { result ->
                                user = result.user
                            },
                            onAuthError = {
                                user = null
                            }
                        )
                        val token = stringResource(R.string.default_web_client_id)
                        val context = LocalContext.current
                        Box(contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                            ) {
                                if (user == null) {
                                    Text("Not logged in")
                                    Spacer(Modifier.height(10.dp))
                                    Button(onClick = {
                                        val gso =
                                            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                .requestIdToken(token)
                                                .requestEmail()
                                                .build()
                                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                        launcher.launch(googleSignInClient.signInIntent)
                                    }) {
                                        Text("Sign in via Google")
                                    }
                                } else {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(user!!.photoUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentScale = ContentScale.Crop,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text("Welcome ${user!!.displayName}")
                                    Spacer(Modifier.height(10.dp))
                                    Button(onClick = {
                                        Firebase.auth.signOut()
                                        user = null
                                    }) {
                                        Text("Sign out")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit,
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

@Serializable
object ScreenA

@Serializable
data class ScreenB(
    val name: String?
)