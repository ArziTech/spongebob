package com.example.spongebob.screens.model

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spongebob.model.ModelType
import com.example.spongebob.viewmodel.ModelSelectionViewModel

/**
 * Screen for displaying model details and confirming selection
 *
 * @param modelId ID of the model to display
 * @param onNavigateBack Callback to navigate back without selecting
 * @param onUseModel Callback when user confirms model selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelDetailScreen(
    modelId: String,
    onNavigateBack: () -> Unit,
    onUseModel: (String) -> Unit,
    viewModel: ModelSelectionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val model = viewModel.getModelById(modelId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Model Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            model == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Model not found")
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ModelInfoCard(model)

                    // Action buttons
                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                viewModel.selectModel(modelId)
                                onUseModel(modelId)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Use Model")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card displaying detailed information about a model
 */
@Composable
private fun ModelInfoCard(model: com.example.spongebob.model.ModelConfig) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name and Type
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                TypeBadgeLarge(model.type)
            }

            Divider()

            // Description
            InfoRow("Description", model.description)

            // Input size
            InfoRow("Input Size", "${model.inputSize} x ${model.inputSize}")

            // Number of classes
            InfoRow("Classes", "${model.classes.size} classes")

            Divider()

            // Class labels
            Text(
                text = "Class Labels",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            model.classes.forEach { className ->
                Row(
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = className,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // File info
            Divider()
            InfoRow("Model File", model.file)
        }
    }
}

/**
 * Large badge showing model type
 */
@Composable
private fun TypeBadgeLarge(type: ModelType) {
    val (text, containerColor, contentColor) = when (type) {
        ModelType.TFLITE -> Triple(
            "TFLITE",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        ModelType.ONNX -> Triple(
            "ONNX",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

/**
 * Row displaying a label and value
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
