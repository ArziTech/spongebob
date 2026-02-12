package com.example.spongebob.model

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.InputStream

/**
 * Manager for loading and accessing model configurations.
 *
 * Parses models.yaml from assets and provides access to ModelConfig objects.
 * Caches loaded configurations in memory for efficient access.
 */
class ModelManager(private val context: Context) {

    companion object {
        private const val TAG = "ModelManager"
        private const val MODELS_YAML_FILE = "models.yaml"
    }

    private var cachedConfigs: List<ModelConfig>? = null

    /**
     * Load model configurations from assets/models.yaml
     *
     * @return List of ModelConfig objects
     * @throws Exception if YAML parsing fails or file not found
     */
    suspend fun loadModelConfigs(): List<ModelConfig> = withContext(Dispatchers.IO) {
        // Return cached configs if available
        cachedConfigs?.let { return@withContext it }

        Log.d(TAG, "Loading model configurations from $MODELS_YAML_FILE")

        try {
            val inputStream: InputStream = context.assets.open(MODELS_YAML_FILE)
            val yaml = Yaml(Constructor(ModelsConfig::class.java))
            val config = yaml.load(inputStream) as ModelsConfig

            cachedConfigs = config.models
            Log.d(TAG, "Loaded ${config.models.size} model configurations")

            config.models
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model configurations", e)
            throw RuntimeException("Failed to load models.yaml: ${e.message}", e)
        }
    }

    /**
     * Get a specific model configuration by ID
     *
     * @param id Model ID to lookup
     * @return ModelConfig if found, null otherwise
     */
    fun getModelConfig(id: String): ModelConfig? {
        return cachedConfigs?.find { it.id == id }
    }

    /**
     * Get the currently selected model configuration
     *
     * @param preferencesManager PreferencesManager to read selection from
     * @return Currently selected ModelConfig
     * @throws IllegalStateException if configs not loaded or ID not found
     */
    suspend fun getCurrentModel(preferencesManager: com.example.spongebob.data.PreferencesManager): ModelConfig {
        if (cachedConfigs == null) {
            loadModelConfigs()
        }

        val selectedId = preferencesManager.selectedModelId.first()
        val config = getModelConfig(selectedId)

        return config ?: throw IllegalStateException(
            "Selected model ID '$selectedId' not found in loaded configurations"
        )
    }

    /**
     * Check if configurations are already loaded
     */
    fun isLoaded(): Boolean = cachedConfigs != null

    /**
     * Clear cached configurations (for testing or refresh)
     */
    fun clearCache() {
        cachedConfigs = null
    }
}
