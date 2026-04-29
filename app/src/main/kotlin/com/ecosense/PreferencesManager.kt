package com.ecosense

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.ecosense.ui.theme.ContrastLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class PreferencesManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        // String key so absent = null = "follow system" (never overwrites isSystemInDarkTheme())
        val DARK_MODE_KEY     = stringPreferencesKey("dark_mode_override")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        val CONTRAST_LEVEL_KEY = stringPreferencesKey("contrast_level")
    }

    val preferencesFlow: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            val darkModeOverride = preferences[DARK_MODE_KEY]?.toBooleanStrictOrNull()
            val dynamicColor     = preferences[DYNAMIC_COLOR_KEY] ?: false
            val contrastLevel    = preferences[CONTRAST_LEVEL_KEY] ?: ContrastLevel.NORMAL.name
            UserPreferences(darkModeOverride, dynamicColor, ContrastLevel.valueOf(contrastLevel))
        }

    suspend fun updateDarkMode(darkMode: Boolean) {
        dataStore.edit { it[DARK_MODE_KEY] = darkMode.toString() }
    }

    suspend fun updateDynamicColor(dynamicColor: Boolean) {
        dataStore.edit { it[DYNAMIC_COLOR_KEY] = dynamicColor }
    }

    suspend fun updateContrastLevel(contrastLevel: ContrastLevel) {
        dataStore.edit { it[CONTRAST_LEVEL_KEY] = contrastLevel.name }
    }
}

data class UserPreferences(
    /** null means "not yet set — follow system dark theme" */
    val darkModeOverride: Boolean?,
    val dynamicColor: Boolean,
    val contrastLevel: ContrastLevel
)
