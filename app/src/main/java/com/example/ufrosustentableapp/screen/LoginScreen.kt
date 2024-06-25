package com.example.ufrosustentableapp.screen

import android.app.Activity
import android.content.Context
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
    val gradientColors = listOf(colorScheme.primary, colorScheme.primaryContainer)

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
                colorFilter = ColorFilter.tint(colorScheme.onPrimaryContainer),
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
                    db.collection("users").document(it).get()
                        .addOnSuccessListener { document ->
                            if (!document.exists()) {
                                val userDoc = hashMapOf(
                                    "name" to user.displayName,
                                    "email" to user.email,
                                    "points" to 0,
                                    "recyclingHistory" to emptyList<String>()
                                )

                                db.collection("users").document(it).set(userDoc)
                                    .addOnSuccessListener {
                                        onSignInSuccess()
                                    }
                                    .addOnFailureListener {
                                        // Handle the error
                                    }
                            } else {
                                onSignInSuccess()
                            }
                        }
                        .addOnFailureListener {
                            // Handle the error
                        }
                }
            } else {
                // Handle authentication error
            }
        }
}
