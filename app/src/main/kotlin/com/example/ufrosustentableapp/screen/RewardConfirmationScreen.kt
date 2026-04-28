package com.example.ufrosustentableapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun RewardConfirmationScreen(
    navController: NavHostController,
    rewardTitle: String,
    rewardCost: Int,
    userPoints: Int
) {
    val colorScheme = MaterialTheme.colorScheme
    val remainingPoints = userPoints - rewardCost

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Confirmar Recompensa",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Estás canjeando:",
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = rewardTitle,
            style = MaterialTheme.typography.headlineLarge,
            color = colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Costo: $rewardCost puntos",
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Puntos restantes: $remainingPoints",
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                // Lógica de confirmación de canje
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirmar")
        }
    }
}