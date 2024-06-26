package com.example.ufrosustentableapp.presentation

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color

@Composable
fun infiniteColorTransition(
    initialValue: Color,
    targetValue: Color,
    label: String
): State<Color> {
    val transition = rememberInfiniteTransition(label = label)
    return transition.animateColor(
        initialValue = initialValue,
        targetValue = targetValue,
        label = label,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
}