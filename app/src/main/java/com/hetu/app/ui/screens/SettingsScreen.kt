package com.hetu.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.hetu.app.ui.theme.HetuTheme
import com.hetu.app.viewmodel.DiscoveredModel
import com.hetu.app.viewmodel.ModelViewModel
import com.runanywhere.sdk.public.extensions.Models.ModelCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modelViewModel: ModelViewModel
) {
    val uiState by modelViewModel.uiState.collectAsState()
    val isLoading by modelViewModel.isLoading.collectAsState()
    val error by modelViewModel.error.collectAsState()
    val sdkAvailable by modelViewModel.sdkAvailable.collectAsState()
    val context = LocalContext.current

    // Check storage permission based on Android version
    var hasStoragePermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                ContextCompat.checkSelfPermission(
                    context, 
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    // Permission launcher for older Android versions
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasStoragePermission = granted
        if (granted) {
            modelViewModel.scanForLocalModels()
        }
    }

    // For Android 11+ we need to use MANAGE_EXTERNAL_STORAGE
    val manageStorageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasStoragePermission = Environment.isExternalStorageManager()
            if (hasStoragePermission) {
                modelViewModel.scanForLocalModels()
            }
        }
    }

    fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ needs special permission
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${context.packageName}")
                manageStorageLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                manageStorageLauncher.launch(intent)
            }
        } else {
            // Android 10 and below
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    var showModelPicker by remember { mutableStateOf(false) }
    var modelPickerCategory by remember { mutableStateOf(ModelCategory.LANGUAGE) }

    LaunchedEffect(hasStoragePermission) {
        if (hasStoragePermission) {
            modelViewModel.scanForLocalModels()
        }
    }

    LaunchedEffect(isLoading) {
        if (isLoading) {
             android.widget.Toast.makeText(context, "Loading AI Model...", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    LaunchedEffect(uiState.isLLMReady) {
        if (uiState.isLLMReady) {
             android.widget.Toast.makeText(context, "Model Loaded Successfully!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    LaunchedEffect(error) {
        error?.let {
             android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = HetuTheme.colors.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        color = HetuTheme.colors.onBackground,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = HetuTheme.colors.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HetuTheme.colors.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            item {
                SectionHeader(title = "Appearance")
            }
            
            item {
                SettingsCard(
                    icon = Icons.Outlined.DarkMode,
                    title = "Theme",
                    subtitle = "Dark Mode",
                    onClick = { }
                )
            }
            
            // Notifications Section
            item {
                SectionHeader(title = "Notifications")
            }
            
            item {
                SettingsCard(
                    icon = Icons.Outlined.Notifications,
                    title = "Frequency",
                    subtitle = "Daily",
                    onClick = { }
                )
            }
            
            item {
                SettingsCard(
                    icon = Icons.Outlined.Person,
                    title = "Personality",
                    subtitle = "Friendly",
                    onClick = { }
                )
            }

            // Privacy Section
            item {
                SectionHeader(title = "Privacy")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = HetuTheme.colors.primary.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = "Privacy",
                            tint = HetuTheme.colors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "100% Offline",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HetuTheme.colors.onBackground
                            )
                            Text(
                                text = "Your data never leaves this device. No cloud, no sync, no tracking.",
                                fontSize = 14.sp,
                                color = HetuTheme.colors.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // About Section
            item {
                SectionHeader(title = "About")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = HetuTheme.colors.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "हेतु",
                            fontSize = 48.sp,
                            color = HetuTheme.colors.onBackground
                        )
                        Text(
                            text = "Hetu",
                            fontSize = 18.sp,
                            color = HetuTheme.colors.onSurface
                        )
                        Text(
                            text = "Version 1.1.0",
                            fontSize = 14.sp,
                            color = HetuTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // AI Model Section
            item {
                SectionHeader(title = "AI Model")
            }

            // SDK Status banner
            item {
                if (!sdkAvailable) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF443322)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFFFFAA55),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AI Engine Loading...",
                                    color = Color(0xFFFFAA55),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Native libraries initializing",
                                    color = Color(0xFFCCAA88),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Error Card
            if (error != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE), // Red 50
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SDK Initialization Error",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                )
                                Text(
                                    text = error!!,
                                    color = Color(0xFFB71C1C),
                                    fontSize = 13.sp
                                )
                            }
                            IconButton(onClick = { modelViewModel.clearError() }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }
                }
            }

            item {
                ModelStatusCard(
                    title = "Language Model (LLM)",
                    subtitle = if (uiState.isLLMReady) {
                        uiState.loadedLLMModelName ?: "Model loaded"
                    } else {
                        "Tap to load a GGUF model"
                    },
                    isLoaded = uiState.isLLMReady,
                    isLoading = isLoading,
                    icon = Icons.Outlined.Psychology,
                    onClick = {
                        if (!hasStoragePermission) {
                            requestStoragePermission()
                        } else {
                            modelPickerCategory = ModelCategory.LANGUAGE
                            showModelPicker = true
                        }
                    },
                    onUnload = if (uiState.isLLMReady) {
                        { modelViewModel.unloadLLM() }
                    } else null
                )
            }

            // STT Model Section
            item {
                ModelStatusCard(
                    title = "Speech Recognition (STT)",
                    subtitle = if (uiState.isSTTReady) {
                        uiState.loadedSTTModelName ?: "Whisper loaded"
                    } else {
                        "Tap to load Whisper/Silero model"
                    },
                    isLoaded = uiState.isSTTReady,
                    isLoading = false,
                    icon = Icons.Outlined.Mic,
                    onClick = {
                        if (!hasStoragePermission) {
                            requestStoragePermission()
                        } else {
                            modelPickerCategory = ModelCategory.SPEECH_RECOGNITION
                            showModelPicker = true
                        }
                    },
                    onUnload = null
                )
            }

            // Storage Permission Card
            if (!hasStoragePermission) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { requestStoragePermission() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = HetuTheme.colors.accent.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Folder,
                                contentDescription = null,
                                tint = HetuTheme.colors.accent
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Storage Access Required",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = HetuTheme.colors.onBackground
                                )
                                Text(
                                    text = "Tap to grant permission to scan for models",
                                    fontSize = 12.sp,
                                    color = HetuTheme.colors.onSurface
                                )
                            }
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = HetuTheme.colors.accent
                            )
                        }
                    }
                }
            }

            // Discovered Models
            if (uiState.discoveredModels.isNotEmpty()) {
                item {
                    SectionHeader(title = "Discovered Models (${uiState.discoveredModels.size})")
                }

                items(uiState.discoveredModels) { model ->
                    DiscoveredModelCard(
                        model = model,
                        isLoading = isLoading,
                        sdkAvailable = sdkAvailable,
                        onClick = {
                            when (model.category) {
                                ModelCategory.LANGUAGE -> 
                                    modelViewModel.loadLocalLLMModel(model.path, model.name)
                                ModelCategory.SPEECH_RECOGNITION -> 
                                    modelViewModel.loadLocalSTTModel(model.path, model.name)
                                else -> {}
                            }
                        }
                    )
                }
            } else if (hasStoragePermission) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = HetuTheme.colors.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.FolderOpen,
                                contentDescription = null,
                                tint = HetuTheme.colors.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No models found",
                                fontSize = 14.sp,
                                color = HetuTheme.colors.onSurface
                            )
                            Text(
                                text = "Place .gguf files in your Download folder",
                                fontSize = 12.sp,
                                color = HetuTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { modelViewModel.scanForLocalModels() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HetuTheme.colors.primary
                                )
                            ) {
                                Text("Scan Again")
                            }
                        }
                    }
                }
            }

            // Bottom padding
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Model Picker Bottom Sheet
    if (showModelPicker) {
        ModelPickerSheet(
            category = modelPickerCategory,
            models = uiState.discoveredModels.filter { it.category == modelPickerCategory },
            sdkAvailable = sdkAvailable,
            onDismiss = { showModelPicker = false },
            onSelectModel = { model ->
                when (model.category) {
                    ModelCategory.LANGUAGE -> 
                        modelViewModel.loadLocalLLMModel(model.path, model.name)
                    ModelCategory.SPEECH_RECOGNITION -> 
                        modelViewModel.loadLocalSTTModel(model.path, model.name)
                    else -> {}
                }
                showModelPicker = false
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = HetuTheme.colors.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HetuTheme.colors.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HetuTheme.colors.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = HetuTheme.colors.onBackground
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = HetuTheme.colors.onSurface
                )
            }
            Icon(
                Icons.Outlined.MoreVert,
                contentDescription = "More",
                tint = HetuTheme.colors.onSurface
            )
        }
    }
}

@Composable
fun ModelStatusCard(
    title: String,
    subtitle: String,
    isLoaded: Boolean,
    isLoading: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    onUnload: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLoaded) 
                HetuTheme.colors.primary.copy(alpha = 0.15f)
            else 
                HetuTheme.colors.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isLoaded) HetuTheme.colors.accent.copy(alpha = 0.2f)
                        else HetuTheme.colors.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = HetuTheme.colors.primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isLoaded) HetuTheme.colors.accent else HetuTheme.colors.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HetuTheme.colors.onBackground
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = if (isLoaded) HetuTheme.colors.accent else HetuTheme.colors.onSurface
                )
            }

            if (isLoaded && onUnload != null) {
                IconButton(onClick = onUnload) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Unload",
                        tint = HetuTheme.colors.onSurface
                    )
                }
            } else {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "Select",
                    tint = HetuTheme.colors.onSurface
                )
            }
        }
    }
}

@Composable
fun DiscoveredModelCard(
    model: DiscoveredModel,
    isLoading: Boolean,
    sdkAvailable: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading && sdkAvailable) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = HetuTheme.colors.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (model.category) {
                    ModelCategory.LANGUAGE -> Icons.Outlined.Psychology
                    ModelCategory.SPEECH_RECOGNITION -> Icons.Outlined.Mic
                    else -> Icons.Outlined.Memory
                },
                contentDescription = null,
                tint = HetuTheme.colors.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = HetuTheme.colors.onBackground
                )
                Text(
                    text = "${model.format.value.uppercase()} • ${model.sizeFormatted}",
                    fontSize = 12.sp,
                    color = HetuTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }

            TextButton(
                onClick = onClick, 
                enabled = !isLoading && sdkAvailable
            ) {
                Text(
                    text = if (sdkAvailable) "Load" else "...",
                    color = if (sdkAvailable) HetuTheme.colors.primary else HetuTheme.colors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelPickerSheet(
    category: ModelCategory,
    models: List<DiscoveredModel>,
    sdkAvailable: Boolean,
    onDismiss: () -> Unit,
    onSelectModel: (DiscoveredModel) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = HetuTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = when (category) {
                    ModelCategory.LANGUAGE -> "Select LLM Model"
                    ModelCategory.SPEECH_RECOGNITION -> "Select STT Model"
                    else -> "Select Model"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = HetuTheme.colors.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (models.isEmpty()) {
                Text(
                    text = when (category) {
                        ModelCategory.LANGUAGE -> 
                            "No GGUF models found.\n\nPlace models in:\n• Download folder\n• Models folder"
                        ModelCategory.SPEECH_RECOGNITION -> 
                            "No Whisper/ONNX models found.\n\nPlace whisper models in:\n• Download folder"
                        else -> "No models found"
                    },
                    fontSize = 14.sp,
                    color = HetuTheme.colors.onSurface,
                    lineHeight = 22.sp
                )
            } else {
                models.forEach { model ->
                    DiscoveredModelCard(
                        model = model,
                        isLoading = false,
                        sdkAvailable = sdkAvailable,
                        onClick = { onSelectModel(model) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
