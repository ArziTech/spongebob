package com.example.spongebob.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spongebob.data.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)

    // Show inference time preference as StateFlow
    val showInferenceTime: StateFlow<Boolean> = preferencesManager.showInferenceTime
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    // NNAPI preference as StateFlow
    val useNnapi: StateFlow<Boolean> = preferencesManager.useNnapi
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Update show inference time preference
    fun setShowInferenceTime(show: Boolean) {
        viewModelScope.launch {
            preferencesManager.setShowInferenceTime(show)
        }
    }

    // Update NNAPI preference
    fun setUseNnapi(use: Boolean) {
        viewModelScope.launch {
            preferencesManager.setUseNnapi(use)
        }
    }

    // Mark NNAPI modal as shown
    fun markNnapiModalShown() {
        viewModelScope.launch {
            preferencesManager.setNnapiModalShown()
        }
    }
}
