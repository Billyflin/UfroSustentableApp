package com.example.ufrosustentableapp.screen

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ufrosustentableapp.ui.theme.ContrastLevel
import com.google.firebase.auth.FirebaseUser


@Composable
fun ProfileScreen(
    user: FirebaseUser?,
    onLogout: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleDynamicColor: () -> Unit,
    isDarkMode: Boolean,
    isDynamicColor: Boolean,
    onChangeContrastLevel: (ContrastLevel) -> Unit,
    contrastLevel: ContrastLevel,
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            if (user?.photoUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.photoUrl)
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = user?.displayName ?: "Usuario",
                color = colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onLogout() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                )
            ) {
                Text("Cerrar sesión")
            }
            Spacer(Modifier.height(16.dp))
            if (!isDynamicColor) {
                Text(text = "Contraste", color = colorScheme.onBackground)
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Button(
                        onClick = { onChangeContrastLevel(ContrastLevel.NORMAL) },
                        enabled = contrastLevel != ContrastLevel.NORMAL
                    ) {
                        Text(text = "Normal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onChangeContrastLevel(ContrastLevel.MEDIUM) },
                        enabled = contrastLevel != ContrastLevel.MEDIUM
                    ) {
                        Text(text = "Medio")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onChangeContrastLevel(ContrastLevel.HIGH) },
                        enabled = contrastLevel != ContrastLevel.HIGH
                    ) {
                        Text(text = "Alto")
                    }
                }
            }
            SwitchSetting(
                title = "Modo oscuro",
                isChecked = isDarkMode,
                onCheckedChange = onToggleDarkMode,
                colorScheme = colorScheme
            )
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Spacer(modifier = Modifier.height(8.dp))
                SwitchSetting(
                    title = "Colores dinámicos",
                    isChecked = isDynamicColor,
                    onCheckedChange = onToggleDynamicColor,
                    colorScheme = colorScheme
                )
            }
        }
    }
}

@Composable
fun SwitchSetting(
    title: String,
    isChecked: Boolean,
    onCheckedChange: () -> Unit,
    colorScheme: ColorScheme
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(colorScheme.surface, shape = RoundedCornerShape(8.dp))
    ) {
        Text(
            text = title,
            color = colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isChecked,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorScheme.primary,
                uncheckedThumbColor = colorScheme.onSurface,
                checkedTrackColor = colorScheme.primary.copy(alpha = 0.5f),
                uncheckedTrackColor = colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )
    }
}
