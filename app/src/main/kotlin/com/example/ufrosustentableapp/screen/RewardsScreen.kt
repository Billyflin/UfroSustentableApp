package com.example.ufrosustentableapp.screen

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ufrosustentableapp.R
import com.example.ufrosustentableapp.presentation.RewardCard
import com.example.ufrosustentableapp.presentation.infiniteColorTransition
import com.example.ufrosustentableapp.viewmodel.RewardsViewModel

@Composable
fun RewardsScreen(
    navController: NavHostController,
    userId: String,
    viewModel: RewardsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(userId) {
        viewModel.initialize(userId)
    }

    val sortedRewards = uiState.rewards.sortedBy { it.pointsRequired > uiState.userPoints }

    val containerColor by infiniteColorTransition(
        initialValue = colorScheme.primary,
        targetValue = colorScheme.inversePrimary,
        label = "containerColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
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
                    painter = painterResource(id = R.drawable.baseline_workspace_premium_20),
                    contentDescription = "Medalla",
                    tint = containerColor,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "${uiState.userPoints} puntos",
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

        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            uiState.error != null -> Text(
                text = "Error: ${uiState.error}",
                color = colorScheme.error
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxSize()
            ) {
                items(sortedRewards) { reward ->
                    RewardCard(navController, reward, uiState.userPoints)
                }
            }
        }
    }
}
