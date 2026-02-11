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

    // GPU preference as StateFlow
    val useGpu: StateFlow<Boolean> = preferencesManager.useGpu
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

    // Update GPU preference
    fun setUseGpu(use: Boolean) {
        viewModelScope.launch {
            preferencesManager.setUseGpu(use)
        }
    }

    // Mark GPU modal as shown
    fun markGpuModalShown() {
        viewModelScope.launch {
            preferencesManager.setGpuModalShown()
        }
    }
}
