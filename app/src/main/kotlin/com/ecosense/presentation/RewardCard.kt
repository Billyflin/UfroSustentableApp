package com.ecosense.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.testing.TestNavHostController
import com.ecosense.R
import com.ecosense.ScreenRewardConfimation
import com.ecosense.model.RewardItem

@Composable
fun RewardCard(navController: NavHostController, reward: RewardItem, userPoints: Int) {
    val colorScheme = MaterialTheme.colorScheme
    val isRedeemable = userPoints >= reward.pointsRequired

    val containerColor by infiniteColorTransition(
        initialValue = colorScheme.primary,
        targetValue = colorScheme.inversePrimary,
        label = "containerColor"
    )
    val iconColor by infiniteColorTransition(
        initialValue = colorScheme.onPrimary,
        targetValue = colorScheme.onSurfaceVariant,
        label = "iconColor"
    )

    ElevatedCard(
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
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isRedeemable) containerColor else colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_emoji_events_20),
                contentDescription = reward.title,
                tint = if (isRedeemable) iconColor else colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isRedeemable) iconColor else colorScheme.onSurface
                )
                Text(
                    text = "${reward.pointsRequired} puntos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isRedeemable) iconColor.copy(alpha = 0.75f) else colorScheme.onSurfaceVariant
                )
            }
            if (!isRedeemable) {
                val needed = reward.pointsRequired - userPoints
                Text(
                    text = "Faltan $needed",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
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
