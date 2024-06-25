package com.example.ufrosustentableapp.screen

import android.util.Log
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ufrosustentableapp.R
import com.example.ufrosustentableapp.presentation.RewardCard
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun RewardsScreen(navController: NavHostController, userId: String) {
    val colorScheme = MaterialTheme.colorScheme
    var userPoints by remember { mutableIntStateOf(0) }

    val rewards by produceState(initialValue = emptyList<RewardItem>()) {
        fetchRewards { rewardsList ->
            value = rewardsList
        }
    }
 Log.d("RewardsScreen", "Antes del la corrutina for user $userId")
    LaunchedEffect(userId) {
        Log.d("RewardsScreen", "Fetching user points for user $userId")
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)
        userRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("RewardsScreen", "Error fetching user points: ${e.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                Log.d("RewardsScreen", "Current data: ${snapshot.data}")
                userPoints = (snapshot.getLong("points") ?: 0).toInt()
                Log.d("RewardsScreen", "User points: $userPoints")
            } else {
                Log.d("RewardsScreen", "No such document")
            }
        }
    }


    val transition = rememberInfiniteTransition(label = "")
    val containerColor by transition.animateColor(
        initialValue = colorScheme.primary,
        targetValue = colorScheme.primaryContainer,
        label = "containerColor",
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(top = 70.dp, bottom = 110.dp)
            .padding(16.dp)
    ) {
        Text(
            text = "Tus Puntos",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceContainerHigh,
                contentColor = colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_workspace_premium_20), // Reemplaza con tu icono de medalla
                    contentDescription = "Medalla",
                    tint = containerColor,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "$userPoints puntos",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colorScheme.onSurface
                )
            }
        }

        Text(
            text = "Premios Disponibles",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxSize()
        ) {
            items(rewards) { reward ->
                RewardCard(navController, reward, userPoints)
            }
        }
    }
}
fun updateUserPoints(userId: String, pointsToAdd: Int, onComplete: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(userId)

    db.runTransaction { transaction ->
        val snapshot = transaction.get(userRef)
        val currentPoints = snapshot.getLong("points") ?: 0
        val newPoints = currentPoints + pointsToAdd
        transaction.update(userRef, "points", newPoints)
    }.addOnSuccessListener {
        onComplete(true)
    }.addOnFailureListener { e ->
        println("Error actualizando los puntos: ${e.message}")
        onComplete(false)
    }
}



fun fetchRewards(onResult: (List<RewardItem>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("rewards")
        .get()
        .addOnSuccessListener { result ->
            val rewards = result.map { document ->
                RewardItem(
                    title = document.getString("title") ?: "",
                    pointsRequired = document.getLong("pointsRequired")?.toInt() ?: 0
                )
            }
            onResult(rewards)
        }
        .addOnFailureListener {
            onResult(emptyList()) // En caso de error, devolver una lista vacía
        }
}

data class RewardItem(val title: String, val pointsRequired: Int)

