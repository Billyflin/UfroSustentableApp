package com.ecosense.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ecosense.R
import com.ecosense.model.GoogleButtonTheme

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    theme: GoogleButtonTheme = if (isSystemInDarkTheme()) GoogleButtonTheme.Dark else GoogleButtonTheme.Light
) {
    val containerColor = when (theme) {
        GoogleButtonTheme.Light   -> Color.White
        GoogleButtonTheme.Dark    -> Color(0xFF131314)
        GoogleButtonTheme.Neutral -> Color(0xFFF2F2F2)
    }
    val contentColor = when (theme) {
        GoogleButtonTheme.Dark -> Color(0xFFE3E3E3)
        else                   -> Color(0xFF1F1F1F)
    }
    val borderColor = when (theme) {
        GoogleButtonTheme.Light -> Color(0xFF747775)
        GoogleButtonTheme.Dark  -> Color(0xFF8E918F)
        else                    -> Color.Transparent
    }

    OutlinedButton(
        onClick      = onClick,
        modifier     = Modifier.fillMaxWidth(),
        shape        = MaterialTheme.shapes.large,
        border       = BorderStroke(1.dp, borderColor),
        colors       = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor   = contentColor
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter            = painterResource(id = R.drawable.google_logo),
                contentDescription = null,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text  = "Continuar con Google",
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}

@Preview
@Composable
private fun LightPreview() {
    GoogleSignInButton(onClick = {}, theme = GoogleButtonTheme.Light)
}

@Preview
@Composable
private fun DarkPreview() {
    GoogleSignInButton(onClick = {}, theme = GoogleButtonTheme.Dark)
}
