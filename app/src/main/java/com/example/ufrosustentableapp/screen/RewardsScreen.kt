package com.example.ufrosustentableapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ufrosustentableapp.R
import com.example.ufrosustentableapp.ScreenRewardConfimation


@Composable
fun RewardsScreen(navController: NavHostController, userPoints: Int, rewards: List<RewardItem>) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(top = 70.dp, bottom = 147.dp)
            .padding(16.dp)
    ) {
        Text(
            text = "Tus Puntos",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface,
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
                    tint = colorScheme.primary,
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
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(rewards) { reward ->
                RewardCard(navController, reward, userPoints)
            }
        }
    }
}

@Composable
fun RewardCard(navController: NavHostController, reward: RewardItem, userPoints: Int) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth().clickable {
                navController.navigate(ScreenRewardConfimation(
                    rewardTitle = reward.title,
                    rewardCost = reward.pointsRequired,
                    userPoints = userPoints
                ))
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_emoji_events_20), // Reemplaza con tu icono de recompensa
                contentDescription = reward.title,
                tint = colorScheme.primary,
                modifier = Modifier.size(48.dp)

            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = reward.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "${reward.pointsRequired} puntos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

data class RewardItem(val title: String, val pointsRequired: Int)

