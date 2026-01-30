package com.hetu.app.viewmodel

import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.sdk.core.types.InferenceFramework
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.public.extensions.Models.ModelCategory
import com.runanywhere.sdk.public.extensions.Models.ModelFormat
import com.runanywhere.sdk.public.extensions.generateStream
import com.runanywhere.sdk.public.extensions.loadLLMModel
import com.runanywhere.sdk.public.extensions.loadSTTModel
import com.runanywhere.sdk.public.extensions.registerModel
import com.runanywhere.sdk.public.extensions.unloadLLMModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ViewModel for managing AI models (LLM and STT)
 * Works even when SDK native libs aren't available - just shows discovered models
 */
class ModelViewModel : ViewModel() {

    companion object {
        private const val TAG = "ModelViewModel"
    }

    // UI State
    private val _uiState = MutableStateFlow(ModelUiState())
    val uiState: StateFlow<ModelUiState> = _uiState.asStateFlow()

    // Model loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error messages
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Text generation state
    private val _generatedText = MutableStateFlow("")
    val generatedText: StateFlow<String> = _generatedText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // SDK availability
    private val _sdkAvailable = MutableStateFlow(false)
    val sdkAvailable: StateFlow<Boolean> = _sdkAvailable.asStateFlow()

    init {
        viewModelScope.launch {
            // Scan for models first (always works)
            scanForLocalModels()
            
            // Try to initialize SDK
            checkSDKAvailability()
        }
    }

    private fun checkSDKAvailability() {
        try {
            com.hetu.app.HetuApplication.ensureSDKInitialized()
            // Try a simple SDK call to verify it works
            val isInitialized = RunAnywhere.isInitialized
            _sdkAvailable.value = isInitialized
            Log.i(TAG, "SDK available: $isInitialized")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "SDK native library not available: ${e.message}")
            _sdkAvailable.value = false
            _error.value = "Native libraries not loaded. Please check JNI setup."
        } catch (e: Exception) {
            Log.e(TAG, "SDK check failed: ${e.message}")
            _sdkAvailable.value = false
        }
    }

    /**
     * Scan common directories for GGUF model files
     */
    fun scanForLocalModels() {
        viewModelScope.launch {
            val discoveredModels = mutableListOf<DiscoveredModel>()
            
            val searchPaths = listOf(
                // Common model locations
                Environment.getExternalStorageDirectory().absolutePath + "/Download",
                Environment.getExternalStorageDirectory().absolutePath + "/Downloads",
                Environment.getExternalStorageDirectory().absolutePath + "/Models",
                Environment.getExternalStorageDirectory().absolutePath + "/AI",
                Environment.getExternalStorageDirectory().absolutePath,
                "/storage/emulated/0/Download",
                "/storage/emulated/0/Downloads",
                "/storage/emulated/0/Models",
            )

            for (path in searchPaths) {
                try {
                    val dir = File(path)
                    if (dir.exists() && dir.isDirectory) {
                        dir.listFiles()?.forEach { file ->
                            when {
                                file.name.endsWith(".gguf", ignoreCase = true) -> {
                                    discoveredModels.add(
                                        DiscoveredModel(
                                            name = file.nameWithoutExtension,
                                            path = file.absolutePath,
                                            format = ModelFormat.GGUF,
                                            category = ModelCategory.LANGUAGE,
                                            sizeBytes = file.length()
                                        )
                                    )
                                }
                                file.name.endsWith(".onnx", ignoreCase = true) ||
                                file.name.contains("whisper", ignoreCase = true) -> {
                                    discoveredModels.add(
                                        DiscoveredModel(
                                            name = file.nameWithoutExtension,
                                            path = file.absolutePath,
                                            format = ModelFormat.ONNX,
                                            category = ModelCategory.SPEECH_RECOGNITION,
                                            sizeBytes = file.length()
                                        )
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to scan $path: ${e.message}")
                }
            }

            Log.i(TAG, "Discovered ${discoveredModels.size} models")
            _uiState.update { it.copy(discoveredModels = discoveredModels.distinctBy { m -> m.path }) }
        }
    }

    fun loadLocalLLMModel(modelPath: String, modelName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!_sdkAvailable.value) {
                _error.value = "SDK not available. Native libraries may not be loaded correctly."
                return@launch
            }

            _isLoading.value = true
            _error.value = null

            try {
                // Register the model with the SDK using extension function
                val modelId = modelName.lowercase().replace(" ", "-").replace("_", "-")
                val modelInfo = RunAnywhere.registerModel(
                    id = modelId,
                    name = modelName,
                    url = "file://$modelPath",
                    framework = InferenceFramework.LLAMA_CPP,
                    modality = ModelCategory.LANGUAGE,
                    supportsThinking = false
                )

                modelInfo.localPath = modelPath

                // Load the model using extension function
                RunAnywhere.loadLLMModel(modelInfo.id)

                _uiState.update { 
                    it.copy(
                        loadedLLMModelId = modelInfo.id,
                        loadedLLMModelName = modelName,
                        isLLMReady = true
                    )
                }

                Log.i(TAG, "Loaded LLM model: $modelName")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load model: ${e.message}", e)
                _error.value = "Failed to load model: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load a local STT model
     */
    fun loadLocalSTTModel(modelPath: String, modelName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!_sdkAvailable.value) {
                _error.value = "SDK not available"
                return@launch
            }

            _isLoading.value = true
            _error.value = null

            try {
                val modelId = modelName.lowercase().replace(" ", "-")
                val modelInfo = RunAnywhere.registerModel(
                    id = modelId,
                    name = modelName,
                    url = "file://$modelPath",
                    framework = InferenceFramework.ONNX,
                    modality = ModelCategory.SPEECH_RECOGNITION
                )

                modelInfo.localPath = modelPath
                RunAnywhere.loadSTTModel(modelInfo.id)

                _uiState.update { 
                    it.copy(
                        loadedSTTModelId = modelInfo.id,
                        loadedSTTModelName = modelName,
                        isSTTReady = true
                    )
                }

            } catch (e: Exception) {
                _error.value = "Failed to load STT model: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Generate text response using LLM (streaming)
     */
    fun generateResponse(prompt: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Add user message to state
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(ChatMessage(prompt, true))
            _uiState.update { it.copy(messages = currentMessages) }

            if (!_uiState.value.isLLMReady || !_sdkAvailable.value) {
                _error.value = "No LLM model loaded"
                val errMessages = _uiState.value.messages.toMutableList()
                errMessages.add(ChatMessage("Please load a model in Settings to start chatting.", false))
                _uiState.update { it.copy(messages = errMessages) }
                return@launch
            }

            _isGenerating.value = true
            _generatedText.value = ""
            _error.value = null

            try {
                RunAnywhere.generateStream(prompt)
                    .collect { token ->
                        _generatedText.update { it + token }
                    }
                
                // Add final bot message to state
                val updatedMessages = _uiState.value.messages.toMutableList()
                updatedMessages.add(ChatMessage(_generatedText.value, false))
                _uiState.update { it.copy(messages = updatedMessages) }
                _generatedText.value = "" // Clear temporary text
                
            } catch (e: Exception) {
                _error.value = "Generation failed: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    /**
     * Unload current LLM model
     */
    fun unloadLLM() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (_sdkAvailable.value) {
                    RunAnywhere.unloadLLMModel()
                }
                _uiState.update { 
                    it.copy(
                        loadedLLMModelId = null,
                        loadedLLMModelName = null,
                        isLLMReady = false
                    )
                }
            } catch (e: Exception) {
                _error.value = "Failed to unload model: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

/**
 * UI State for model management
 */
data class ModelUiState(
    val discoveredModels: List<DiscoveredModel> = emptyList(),
    val loadedLLMModelId: String? = null,
    val loadedLLMModelName: String? = null,
    val loadedSTTModelId: String? = null,
    val loadedSTTModelName: String? = null,
    val isLLMReady: Boolean = false,
    val isSTTReady: Boolean = false,
    val messages: List<ChatMessage> = listOf(
        ChatMessage("Hi, I'm Hetu. I'm here to listen. How are you feeling today?", false)
    )
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

/**
 * Discovered model from device storage
 */
data class DiscoveredModel(
    val name: String,
    val path: String,
    val format: ModelFormat,
    val category: ModelCategory,
    val sizeBytes: Long
) {
    val sizeFormatted: String
        get() {
            val mb = sizeBytes / (1024.0 * 1024.0)
            val gb = mb / 1024.0
            return if (gb >= 1.0) {
                String.format("%.2f GB", gb)
            } else {
                String.format("%.0f MB", mb)
            }
        }
}
