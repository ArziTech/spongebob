package com.example.spongebob.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.spongebob.data.entity.EvaluationEntity
import com.example.spongebob.data.entity.EvaluationGroupEntity
import com.example.spongebob.data.dao.ConfusionMatrixEntry
import com.example.spongebob.data.dao.GroupAccuracyStats
import com.example.spongebob.data.repository.EvaluationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean = true,
    val groupName: String? = null,
    val evaluations: List<EvaluationEntity> = emptyList(),
    val groupAccuracyStats: GroupAccuracyStats? = null,
    val confusionMatrixData: List<ConfusionMatrixEntry>? = null
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EvaluationRepository(application)

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun loadGroup(groupId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load group info
            val group = repository.getGroupById(groupId)

            // Load evaluations
            repository.getEvaluationsByGroup(groupId).collect { evaluations ->
                // Load stats
                val stats = repository.getGroupAccuracyStats(groupId)
                val confusionData = repository.getConfusionMatrixData(groupId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        groupName = group?.groupName,
                        evaluations = evaluations,
                        groupAccuracyStats = stats,
                        confusionMatrixData = confusionData
                    )
                }
            }
        }
    }

    fun deleteEvaluation(evaluationId: Long) {
        viewModelScope.launch {
            repository.deleteEvaluation(evaluationId)
            // The flow will automatically update
        }
    }
}
