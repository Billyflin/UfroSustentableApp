package com.ecosense.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ecosense.R

@Composable
fun RewardConfirmationScreen(
    navController: NavHostController,
    rewardTitle: String,
    rewardCost: Int,
    userPoints: Int
) {
    val colorScheme     = MaterialTheme.colorScheme
    val remainingPoints = userPoints - rewardCost
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier              = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                      slideInVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy,
                                              stiffness    = Spring.StiffnessMediumLow)) { it / 3 }
        ) {
            ElevatedCard(
                shape    = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier            = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ícono trofeo animado
                    AnimatedVisibility(
                        visible = visible,
                        enter   = scaleIn(spring(dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness    = Spring.StiffnessMediumLow))
                    ) {
                        Icon(
                            painter            = painterResource(R.drawable.baseline_emoji_events_20),
                            contentDescription = null,
                            tint               = colorScheme.primary,
                            modifier           = Modifier.size(72.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text      = "Confirmar canje",
                        style     = MaterialTheme.typography.titleMedium,
                        color     = colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text      = rewardTitle,
                        style     = MaterialTheme.typography.headlineMedium,
                        color     = colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Costo del premio", style = MaterialTheme.typography.bodyMedium,
                             color = colorScheme.onSurfaceVariant)
                        Text("$rewardCost pts", style = MaterialTheme.typography.titleSmall,
                             color = colorScheme.onSurface)
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tus puntos actuales", style = MaterialTheme.typography.bodyMedium,
                             color = colorScheme.onSurfaceVariant)
                        Text("$userPoints pts", style = MaterialTheme.typography.titleSmall,
                             color = colorScheme.onSurface)
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Puntos restantes", style = MaterialTheme.typography.titleSmall,
                             color = colorScheme.onSurface)
                        Text(
                            text  = "$remainingPoints pts",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (remainingPoints >= 0) colorScheme.primary else colorScheme.error
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick  = { navController.popBackStack() },
                        shape    = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth(),
                        enabled  = remainingPoints >= 0
                    ) {
                        Text("Confirmar canje")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick  = { navController.popBackStack() },
                        shape    = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}
