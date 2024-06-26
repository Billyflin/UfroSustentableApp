package com.example.ufrosustentableapp

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.ufrosustentableapp.ui.theme.ContrastLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class PreferencesManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
        val CONTRAST_LEVEL_KEY = stringPreferencesKey("contrast_level")
    }

    val preferencesFlow: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            val darkMode = preferences[DARK_MODE_KEY] ?: false
            val dynamicColor = preferences[DYNAMIC_COLOR_KEY] ?: false
            val contrastLevel = preferences[CONTRAST_LEVEL_KEY] ?: ContrastLevel.NORMAL.name
            UserPreferences(darkMode, dynamicColor, ContrastLevel.valueOf(contrastLevel))
        }

    suspend fun updateDarkMode(darkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = darkMode
        }
    }

    suspend fun updateDynamicColor(dynamicColor: Boolean) {
        dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = dynamicColor
        }
    }

    suspend fun updateContrastLevel(contrastLevel: ContrastLevel) {
        dataStore.edit { preferences ->
            preferences[CONTRAST_LEVEL_KEY] = contrastLevel.name
        }
    }
}

data class UserPreferences(
    val darkMode: Boolean,
    val dynamicColor: Boolean,
    val contrastLevel: ContrastLevel
)
