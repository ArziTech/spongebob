package com.example.spongebob.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.spongebob.ui.theme.ThemeOption

// DataStore extension
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        // Preference keys
        val SHOW_INFERENCE_TIME_KEY = booleanPreferencesKey("show_inference_time")
        val USE_GPU_KEY = booleanPreferencesKey("use_gpu")
        val GPU_MODAL_SHOWN_KEY = booleanPreferencesKey("gpu_modal_shown")
        val SELECTED_MODEL_ID_KEY = stringPreferencesKey("selected_model_id")
        val THEME_KEY = stringPreferencesKey("theme") // "light", "dark", or "system"
    }

    // Flow for show inference time preference
    val showInferenceTime: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_INFERENCE_TIME_KEY] ?: true // Default: true (show inference time)
    }

    // Flow for GPU preference
    val useGpu: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_GPU_KEY] ?: false // Default: false (CPU for compatibility)
    }

    // Flow for GPU modal shown flag
    val gpuModalShown: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[GPU_MODAL_SHOWN_KEY] ?: false // Default: false (not shown yet)
    }

    // Flow for selected model ID
    val selectedModelId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_MODEL_ID_KEY] ?: "small_3class" // Default: small_3class
    }

    // Flow for theme preference (as ThemeOption enum)
    val themeOption: Flow<ThemeOption> = context.dataStore.data.map { preferences ->
        val themeString = preferences[THEME_KEY]
        ThemeOption.fromString(themeString)
    }

    // Save show inference time preference
    suspend fun setShowInferenceTime(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_INFERENCE_TIME_KEY] = show
        }
    }

    // Save GPU preference
    suspend fun setUseGpu(use: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_GPU_KEY] = use
        }
    }

    // Mark GPU modal as shown
    suspend fun setGpuModalShown() {
        context.dataStore.edit { preferences ->
            preferences[GPU_MODAL_SHOWN_KEY] = true
        }
    }

    // Save selected model ID
    suspend fun setSelectedModelId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_MODEL_ID_KEY] = id
        }
    }

    // Save theme preference
    suspend fun setThemeOption(theme: ThemeOption) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name.lowercase()
        }
    }
}
