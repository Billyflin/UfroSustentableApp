package com.ecosense.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import com.ecosense.R
import com.ecosense.presentation.RewardCard
import com.ecosense.presentation.infiniteColorTransition
import com.ecosense.viewmodel.RewardsViewModel

@Composable
fun RewardsScreen(
    onNavigateToConfirmation: (title: String, cost: Int, points: Int) -> Unit,
    userId: String,
    viewModel: RewardsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(userId) {
        viewModel.initialize(userId)
    }

    val sortedRewards = uiState.rewards.sortedBy { it.pointsRequired > uiState.userPoints }

    val medalColor by infiniteColorTransition(
        initialValue = colorScheme.primary,
        targetValue = colorScheme.tertiary,
        label = "medalColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "Recompensas",
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onBackground
            )
        }

        // Points hero card
        ElevatedCard(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Tus puntos",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${uiState.userPoints}",
                        style = MaterialTheme.typography.displaySmall,
                        color = colorScheme.primary
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.baseline_workspace_premium_20),
                    contentDescription = null,
                    tint = medalColor,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Text(
            text = "Premios disponibles",
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        AnimatedContent(
            targetState = Triple(uiState.isLoading, uiState.error, sortedRewards),
            transitionSpec = {
                fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                        scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow), initialScale = 0.95f) togetherWith
                        fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
            },
            label = "rewardsState"
        ) { (isLoading, error, rewards) ->
            when {
                isLoading -> Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
                error != null -> Text(
                    text = "Error: $error",
                    color = colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(rewards, key = { it.title }) { reward ->
                        RewardCard(onNavigateToConfirmation, reward, uiState.userPoints)
                    }
                }
            }
        }
    }
}
