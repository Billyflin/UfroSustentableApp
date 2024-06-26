package com.example.ufrosustentableapp.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.testing.TestNavHostController
import com.example.ufrosustentableapp.R
import com.example.ufrosustentableapp.ScreenRewardConfimation
import com.example.ufrosustentableapp.model.RewardItem

@Composable
fun RewardCard(navController: NavHostController, reward: RewardItem, userPoints: Int) {
    val colorScheme = MaterialTheme.colorScheme
    val isRedeemable = userPoints >= reward.pointsRequired

    val containerColor by infiniteColorTransition(
        initialValue = colorScheme.primary,
        targetValue = colorScheme.inversePrimary,
        label = "containerColor"
    )
    val containerColorIcon by infiniteColorTransition(
        initialValue = colorScheme.onPrimary,
        targetValue = colorScheme.onSurfaceVariant,
        label = "containerColorIcon"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isRedeemable) {
                navController.navigate(
                    ScreenRewardConfimation(
                        rewardTitle = reward.title,
                        rewardCost = reward.pointsRequired,
                        userPoints = userPoints
                    )
                )
            }
            .background(Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRedeemable) containerColor else colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Transparent)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_emoji_events_20), // Reemplaza con tu icono de recompensa
                contentDescription = reward.title,
                tint = if (isRedeemable) containerColorIcon else colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(
                Modifier
                    .width(16.dp)
                    .background(Color.Transparent))
            Column {
                Text(
                    text = reward.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isRedeemable) containerColorIcon else colorScheme.onSurface
                )
                Text(
                    text = "${reward.pointsRequired} puntos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isRedeemable) containerColorIcon.copy(alpha = 0.6f) else colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
@Preview
@Composable
fun RewardCardPreview() {
    RewardCard(
        navController = TestNavHostController(LocalContext.current),
        reward = RewardItem("Café Gratis", 500),
        userPoints = 1000
    )
}
