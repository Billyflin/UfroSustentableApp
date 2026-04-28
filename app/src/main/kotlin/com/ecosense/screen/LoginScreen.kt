package com.ecosense.screen

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ecosense.R
import com.ecosense.presentation.GoogleSignInButton
import com.ecosense.viewmodel.LoginUiState
import com.ecosense.viewmodel.LoginViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.util.Base64

private const val TAG = "LoginScreen"

@Composable
fun LoginScreen(
    context: Context,
    onSignInSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { contentVisible = true }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            viewModel.resetState()
            onSignInSuccess()
        }
    }

    fun generateNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    suspend fun signInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .setNonce(generateNonce())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                viewModel.signInWithGoogle(googleIdTokenCredential.idToken)
            }
        } catch (e: NoCredentialException) {
            Log.e(TAG, "NoCredentialException", e)
            viewModel.setError("No se encontraron credenciales. Verifica tener una cuenta Google configurada.")
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException", e)
            viewModel.setError("Error al iniciar sesión: ${e.localizedMessage}")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(colorScheme.primary, colorScheme.secondary),
                    startY = 60f,
                    endY = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is LoginUiState.Loading -> {
                CircularProgressIndicator(color = colorScheme.onPrimary)
            }
            else -> {
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                            slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) { it / 3 }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logoecocuadrado),
                            contentDescription = "Logo EcoSense",
                            colorFilter = ColorFilter.tint(colorScheme.onPrimary),
                            modifier = Modifier
                                .size(280.dp)
                                .padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Recicla. Gana. Impacta.",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorScheme.onPrimary.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 40.dp)
                        )

                        ElevatedCard(
                            shape = MaterialTheme.shapes.extraLarge,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                if (uiState is LoginUiState.Error) {
                                    Text(
                                        text = (uiState as LoginUiState.Error).message,
                                        color = colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                }

                                GoogleSignInButton(onClick = {
                                    scope.launch {
                                        credentialManager.clearCredentialState(ClearCredentialStateRequest())
                                        signInWithGoogle()
                                    }
                                })

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { viewModel.signInAnonymously() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorScheme.primaryContainer,
                                        contentColor = colorScheme.onPrimaryContainer
                                    ),
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Text(text = "Continuar como Invitado")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
