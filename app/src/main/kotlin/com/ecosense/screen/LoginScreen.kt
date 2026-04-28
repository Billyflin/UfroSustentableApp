package com.ecosense.screen

import android.content.Context
import android.util.Log
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
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

private const val TAG = "LoginScreen"
// TODO: Replace with your actual Web Client ID from Firebase Console
private const val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID_HERE"

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

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            viewModel.resetState()
            onSignInSuccess()
        }
    }

    suspend fun signInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                viewModel.signInWithGoogle(googleIdTokenCredential.idToken)
            }
        } catch (e: NoCredentialException) {
            Log.e(TAG, "NoCredentialException: Error de autenticación", e)
            viewModel.setError("No se encontraron credenciales. Verifica tener una cuenta Google y el WEB_CLIENT_ID configurado.")
        } catch (e: GetCredentialException) {
            Log.e(TAG, "GetCredentialException: Error de autenticación", e)
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ecosense_logo),
                        contentDescription = "Logo",
                        colorFilter = ColorFilter.tint(colorScheme.onPrimary),
                        modifier = Modifier
                            .size(320.dp)
                            .padding(bottom = 26.dp)
                    )
                    if (uiState is LoginUiState.Error) {
                        Text(
                            text = (uiState as LoginUiState.Error).message,
                            color = colorScheme.errorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    GoogleSignInButton(onClick = {
                        scope.launch {
                            // Clear state before sign in to allow choosing account
                            credentialManager.clearCredentialState(ClearCredentialStateRequest())
                            signInWithGoogle()
                        }
                    })
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.signInAnonymously() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.onPrimary,
                            contentColor = colorScheme.primary
                        )
                    ) {
                        Text(text = "Ingresar como Invitado")
                    }
                }
            }
        }
    }
}
