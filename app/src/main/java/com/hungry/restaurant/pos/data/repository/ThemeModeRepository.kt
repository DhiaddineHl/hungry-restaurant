package com.hungry.restaurant.pos.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hungry.restaurant.pos.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "appearance")
private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

/** Settings > Appearance (Light/Dark/Auto), persisted so it survives app restarts. */
class ThemeModeRepository(private val context: Context) {

    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { prefs ->
        prefs[THEME_MODE_KEY]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.AUTO
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { it[THEME_MODE_KEY] = mode.name }
    }
}
