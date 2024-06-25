package com.example.ufrosustentableapp.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ufrosustentableapp.R


@Composable
fun GoogleSignInButton(
    onClick: () -> Unit, // Función a ejecutar al hacer clic
    shape: Shape = ButtonDefaults.shape,
    theme: GoogleButtonTheme = if (isSystemInDarkTheme()) GoogleButtonTheme.Dark else GoogleButtonTheme.Light,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = when (theme) {
            GoogleButtonTheme.Light -> Color.White
            GoogleButtonTheme.Dark -> Color(0xFF131314)
            GoogleButtonTheme.Neutral -> Color(0xFFF2F2F2)
        },
        contentColor = when (theme) {
            GoogleButtonTheme.Dark -> Color(0xFFE3E3E3)
            else -> Color(0xFF1F1F1F)
        },
    ),
    border: BorderStroke? = when (theme) {
        GoogleButtonTheme.Light -> BorderStroke(
            width = 1.dp,
            color = Color(0xFF747775),
        )

        GoogleButtonTheme.Dark -> BorderStroke(
            width = 1.dp,
            color = Color(0xFF8E918F),
        )

        GoogleButtonTheme.Neutral -> null

    },
    iconSize: Int = 50
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = shape,
        colors = colors,
        contentPadding = PaddingValues(horizontal = 9.5.dp),
        border = border,
    ) {
        Box(
            modifier = Modifier
                .padding(end = 0.dp)
                .size(iconSize.dp)
                .paint(
                    painter = painterResource(id = R.drawable.google_logo)
                )
        )
    }

}

@Preview
@Composable
fun DarkGoogleSignInButtonPreview() {
    GoogleSignInButton(
        onClick = {},
        theme = GoogleButtonTheme.Dark
    )
}

@Preview
@Composable
fun LightGoogleSignInButtonPreview() {
    GoogleSignInButton(
        onClick = {},
        theme = GoogleButtonTheme.Light
    )
}