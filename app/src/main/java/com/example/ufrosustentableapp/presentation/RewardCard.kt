package com.example.ufrosustentableapp.presentation

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.ufrosustentableapp.screen.RewardItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardCard(navController: NavHostController, reward: RewardItem, userPoints: Int) {
    val colorScheme = MaterialTheme.colorScheme
    val isRedeemable = userPoints >= reward.pointsRequired
    val targetColor = if (isRedeemable) colorScheme.primary else colorScheme.surface
    val transition = rememberInfiniteTransition()
    val containerColor by transition.animateColor(
        initialValue = targetColor,
        targetValue = Color(0x9F73DA80),
        label = "containerColor",
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
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
            }.background(Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRedeemable) containerColor else colorScheme.surface ,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp).background(Color.Transparent)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_emoji_events_20), // Reemplaza con tu icono de recompensa
                contentDescription = reward.title,
                tint = if (isRedeemable) colorScheme.surface else colorScheme.primary,
                modifier = Modifier.size(48.dp).background(Color.Transparent)
            )
            Spacer(Modifier.width(16.dp).background(Color.Transparent))
            Column {
                Text(
                    text = reward.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                    modifier = Modifier.background(Color.Transparent)
                )
                Text(
                    text = "${reward.pointsRequired} puntos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.background(Color.Transparent)
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
