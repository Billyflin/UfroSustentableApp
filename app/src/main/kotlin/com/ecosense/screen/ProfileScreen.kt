package com.ecosense.screen

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ecosense.R
import com.ecosense.repository.SeedRepository
import com.ecosense.ui.theme.ContrastLevel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

private sealed interface SeedState {
    data object Idle    : SeedState
    data object Loading : SeedState
    data class Done(val count: Int) : SeedState
    data class Error(val msg: String) : SeedState
}

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
    val scope       = rememberCoroutineScope()
    val seedRepo    = remember { SeedRepository() }
    var seedState   by remember { mutableStateOf<SeedState>(SeedState.Idle) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (user?.photoUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.photoUrl)
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = user?.displayName ?: "Usuario",
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.onBackground
        )
        val email = user?.email
        if (!email.isNullOrEmpty()) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))

        // Cerrar sesión
        Button(
            onClick = onLogout,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.errorContainer,
                contentColor   = colorScheme.onErrorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }

        Spacer(Modifier.height(16.dp))

        // ──── Datos de prueba ────────────────────────────────────────
        ElevatedCard(
            shape    = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text  = "Datos de prueba",
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text  = "Carga solicitudes de reciclaje de ejemplo en tu cuenta para probar la app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                AnimatedContent(
                    targetState = seedState,
                    transitionSpec = {
                        fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                        fadeOut(spring(stiffness = Spring.StiffnessMediumLow))
                    },
                    label = "seedState"
                ) { state ->
                    when (state) {
                        is SeedState.Idle -> {
                            OutlinedButton(
                                onClick = {
                                    val uid = user?.uid ?: return@OutlinedButton
                                    seedState = SeedState.Loading
                                    scope.launch {
                                        seedRepo.seedRequestsForUser(uid)
                                            .onSuccess { n ->
                                                seedState = if (n == 0)
                                                    SeedState.Done(-1)   // ya existían
                                                else
                                                    SeedState.Done(n)
                                            }
                                            .onFailure { e ->
                                                seedState = SeedState.Error(e.message ?: "Error desconocido")
                                            }
                                    }
                                },
                                shape    = MaterialTheme.shapes.large,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cargar solicitudes de prueba")
                            }
                        }

                        is SeedState.Loading -> {
                            Row(
                                verticalAlignment    = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.size(10.dp))
                                Text("Cargando datos...", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        is SeedState.Done -> {
                            Row(
                                verticalAlignment    = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    text = if (state.count == -1)
                                        "Los datos ya estaban cargados"
                                    else
                                        "${state.count} solicitudes añadidas a tu historial",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colorScheme.primary
                                )
                            }
                        }

                        is SeedState.Error -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text      = "Error: ${state.msg}",
                                    style     = MaterialTheme.typography.bodySmall,
                                    color     = colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick  = { seedState = SeedState.Idle },
                                    shape    = MaterialTheme.shapes.large,
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Reintentar") }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ──── Apariencia ─────────────────────────────────────────────
        ElevatedCard(
            shape    = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text  = "Apariencia",
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SettingRow(
                    title          = "Modo oscuro",
                    isChecked      = isDarkMode,
                    onCheckedChange = onToggleDarkMode
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SettingRow(
                        title          = "Colores dinámicos",
                        isChecked      = isDynamicColor,
                        onCheckedChange = onToggleDynamicColor
                    )
                }
            }
        }

        if (!isDynamicColor) {
            Spacer(Modifier.height(16.dp))
            ElevatedCard(
                shape    = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text  = "Nivel de contraste",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick  = { onChangeContrastLevel(ContrastLevel.NORMAL) },
                            enabled  = contrastLevel != ContrastLevel.NORMAL,
                            shape    = MaterialTheme.shapes.large,
                            modifier = Modifier.weight(1f)
                        ) { Text("Normal") }
                        Button(
                            onClick  = { onChangeContrastLevel(ContrastLevel.MEDIUM) },
                            enabled  = contrastLevel != ContrastLevel.MEDIUM,
                            shape    = MaterialTheme.shapes.large,
                            modifier = Modifier.weight(1f)
                        ) { Text("Medio") }
                        Button(
                            onClick  = { onChangeContrastLevel(ContrastLevel.HIGH) },
                            enabled  = contrastLevel != ContrastLevel.HIGH,
                            shape    = MaterialTheme.shapes.large,
                            modifier = Modifier.weight(1f)
                        ) { Text("Alto") }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun SettingRow(
    title: String,
    isChecked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text     = title,
            style    = MaterialTheme.typography.bodyLarge,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked         = isChecked,
            onCheckedChange = { onCheckedChange() }
        )
    }
}

