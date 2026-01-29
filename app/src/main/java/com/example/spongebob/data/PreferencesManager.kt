package com.example.spongebob.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore extension
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        // Preference keys
        val SHOW_INFERENCE_TIME_KEY = booleanPreferencesKey("show_inference_time")
        val USE_NNAPI_KEY = booleanPreferencesKey("use_nnapi")
        val NNAPI_MODAL_SHOWN_KEY = booleanPreferencesKey("nnapi_modal_shown")
    }

    // Flow for show inference time preference
    val showInferenceTime: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_INFERENCE_TIME_KEY] ?: true // Default: true (show inference time)
    }

    // Flow for NNAPI preference
    val useNnapi: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_NNAPI_KEY] ?: false // Default: false (CPU for compatibility)
    }

    // Flow for NNAPI modal shown flag
    val nnapiModalShown: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NNAPI_MODAL_SHOWN_KEY] ?: false // Default: false (not shown yet)
    }

    // Save show inference time preference
    suspend fun setShowInferenceTime(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_INFERENCE_TIME_KEY] = show
        }
    }

    // Save NNAPI preference
    suspend fun setUseNnapi(use: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_NNAPI_KEY] = use
        }
    }

    // Mark NNAPI modal as shown
    suspend fun setNnapiModalShown() {
        context.dataStore.edit { preferences ->
            preferences[NNAPI_MODAL_SHOWN_KEY] = true
        }
    }
}
