package com.example.ufrosustentableapp.screen

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ufrosustentableapp.R
import com.example.ufrosustentableapp.presentation.GoogleSignInButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(
    context: Context,
    onSignInSuccess: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val gradientColors = listOf(colorScheme.primary, colorScheme.secondary)

    // Remember a launcher to handle the result of the Google Sign-In intent
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(context, account.idToken!!, onSignInSuccess)
            } catch (e: ApiException) {
                // Handle error
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 60f,
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
                painter = painterResource(id = R.drawable.ufro_sustentable_app_logo),
                contentDescription = "Logo",
                colorFilter = ColorFilter.tint(colorScheme.onPrimary),
                modifier = Modifier
                    .size(320.dp)
                    .padding(bottom = 26.dp)
            )
            GoogleSignInButton(onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .requestProfile()
                    .build()
                val signInClient = GoogleSignIn.getClient(context, gso)
                signInClient.signOut().addOnCompleteListener {
                    val signInIntent = signInClient.signInIntent
                    launcher.launch(signInIntent)
                }
            })
            Spacer(modifier = Modifier.height(16.dp))
            // Botón de invitado
            Button(
                onClick = {
                    firebaseAnonymousAuth(context, onSignInSuccess)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = "Ingresar como Invitado")
            }

        }
    }
}


private fun firebaseAnonymousAuth(context: Context, onSignInSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    auth.signInAnonymously()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userId = user?.uid

                userId?.let {
                    val userDocRef = db.collection("users").document(it)
                    userDocRef.get()
                        .addOnSuccessListener { document ->
                            if (!document.exists()) {
                                // Crear un nuevo documento de usuario si no existe
                                val userDoc = hashMapOf(
                                    "name" to "Invitado",
                                    "email" to "",
                                    "points" to 0,
                                    "recyclingHistory" to emptyList<String>()
                                )

                                userDocRef.set(userDoc)
                                    .addOnSuccessListener {
                                        onSignInSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(
                                            "Auth",
                                            "Error al crear el documento de usuario: ${e.message}"
                                        )
                                        // Manejar el error
                                    }
                            } else {
                                onSignInSuccess()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Auth", "Error al obtener el documento de usuario: ${e.message}")
                            // Manejar el error
                        }
                }
            } else {
                Log.e("Auth", "Error de autenticación anónima: ${task.exception?.message}")
                // Manejar el error de autenticación
            }
        }
}


private fun firebaseAuthWithGoogle(context: Context, idToken: String, onSignInSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val credential = GoogleAuthProvider.getCredential(idToken, null)

    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userId = user?.uid

                userId?.let {
                    val userDocRef = db.collection("users").document(it)
                    userDocRef.get()
                        .addOnSuccessListener { document ->
                            if (!document.exists()) {
                                // Crear un nuevo documento de usuario si no existe
                                val userDoc = hashMapOf(
                                    "name" to (user.displayName ?: ""),
                                    "email" to (user.email ?: ""),
                                    "points" to 0,
                                    "recyclingHistory" to emptyList<String>()
                                )

                                userDocRef.set(userDoc)
                                    .addOnSuccessListener {
                                        onSignInSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(
                                            "Auth",
                                            "Error al crear el documento de usuario: ${e.message}"
                                        )
                                        // Manejar el error
                                    }
                            } else {
                                // Asegurarse de que el documento existente tiene el campo "points"
                                if (!document.contains("points")) {
                                    userDocRef.update("points", 0)
                                        .addOnSuccessListener {
                                            onSignInSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(
                                                "Auth",
                                                "Error al actualizar el documento de usuario: ${e.message}"
                                            )
                                            // Manejar el error
                                        }
                                } else {
                                    onSignInSuccess()
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Auth", "Error al obtener el documento de usuario: ${e.message}")
                            // Manejar el error
                        }
                }
            } else {
                Log.e("Auth", "Error de autenticación: ${task.exception?.message}")
                // Manejar el error de autenticación
            }
        }
}
