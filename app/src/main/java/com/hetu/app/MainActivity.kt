package com.hetu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hetu.app.ui.screens.SettingsScreen
import com.hetu.app.ui.theme.HetuTheme
import com.hetu.app.viewmodel.ChatMessage
import com.hetu.app.viewmodel.ModelViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HetuTheme {
                HetuApp()
            }
        }
    }
}

@Composable
fun HetuApp() {
    val navController = rememberNavController()
    val modelViewModel: ModelViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToChat = { navController.navigate("chat") },
                onNavigateToTrack = { navController.navigate("track") },
                modelViewModel = modelViewModel
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                modelViewModel = modelViewModel
            )
        }
        composable("chat") {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() },
                modelViewModel = modelViewModel
            )
        }
        composable("track") {
            TrackScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToTrack: () -> Unit,
    modelViewModel: ModelViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val uiState by modelViewModel.uiState.collectAsState()
    val sdkAvailable by modelViewModel.sdkAvailable.collectAsState()
    
    Scaffold(
        containerColor = HetuTheme.colors.background,
        bottomBar = {
            HetuBottomNavigation(
                selectedIndex = selectedTab,
                onItemSelected = { selectedTab = it }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> onNavigateToChat()
                        3 -> onNavigateToTrack()
                        else -> onNavigateToTrack()
                    }
                },
                containerColor = HetuTheme.colors.accent,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = when (selectedTab) {
                        0 -> Icons.Filled.Chat
                        else -> Icons.Filled.Add
                    },
                    contentDescription = "Action"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToChat = onNavigateToChat,
                    isLLMReady = uiState.isLLMReady,
                    llmModelName = uiState.loadedLLMModelName,
                    sdkAvailable = sdkAvailable,
                    modelViewModel = modelViewModel
                )
                1 -> FeedScreen()
                2 -> TimelineScreen()
                3 -> InsightsScreen()
            }
        }
    }
}

@Composable
fun HetuBottomNavigation(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val items = listOf(
        BottomNavItem("Home", Icons.Outlined.Home, Icons.Filled.Home),
        BottomNavItem("Feed", Icons.Outlined.Image, Icons.Filled.Image),
        BottomNavItem("Timeline", Icons.Outlined.Schedule, Icons.Filled.Schedule),
        BottomNavItem("Insights", Icons.Outlined.Lightbulb, Icons.Filled.Lightbulb)
    )
    
    NavigationBar(
        containerColor = HetuTheme.colors.background,
        contentColor = HetuTheme.colors.onBackground
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selectedIndex == index) item.selectedIcon else item.icon,
                        contentDescription = item.label,
                        tint = if (selectedIndex == index) HetuTheme.colors.accent else HetuTheme.colors.onSurface
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (selectedIndex == index) HetuTheme.colors.accent else HetuTheme.colors.onSurface,
                        fontSize = 12.sp
                    )
                },
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = HetuTheme.colors.surface
                )
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToChat: () -> Unit,
    isLLMReady: Boolean,
    llmModelName: String?,
    sdkAvailable: Boolean,
    modelViewModel: ModelViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header with Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = HetuTheme.colors.onBackground
                )
                Text(
                    text = "How are you feeling today?",
                    fontSize = 16.sp,
                    color = HetuTheme.colors.onSurface
                )
            }
            
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(HetuTheme.colors.surface)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = HetuTheme.colors.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Model Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToSettings() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isLLMReady -> HetuTheme.colors.accent.copy(alpha = 0.15f)
                    else -> HetuTheme.colors.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        isLLMReady -> Icons.Filled.CheckCircle
                        !sdkAvailable -> Icons.Outlined.HourglassEmpty
                        else -> Icons.Outlined.Warning
                    },
                    contentDescription = null,
                    tint = when {
                        isLLMReady -> HetuTheme.colors.accent
                        else -> HetuTheme.colors.primary
                    },
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            isLLMReady -> "Hetu is Ready"
                            !sdkAvailable -> "Loading AI Engine..."
                            else -> "Setup Required"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HetuTheme.colors.onBackground
                    )
                    Text(
                        text = when {
                            isLLMReady -> llmModelName ?: "Tap to start chatting"
                            !sdkAvailable -> "Please wait..."
                            else -> "Tap to load a model"
                        },
                        fontSize = 14.sp,
                        color = HetuTheme.colors.onSurface
                    )
                }
                
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = HetuTheme.colors.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Chat Preview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToChat() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = HetuTheme.colors.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Chat,
                        contentDescription = null,
                        tint = HetuTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hetu Journal",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = HetuTheme.colors.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Hi, I'm Hetu. I'm here to listen. How are you feeling today?",
                    fontSize = 15.sp,
                    color = HetuTheme.colors.onSurface,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onNavigateToChat,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HetuTheme.colors.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Start Journaling")
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Privacy Footer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
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
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Privacy",
                    tint = HetuTheme.colors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "100% Offline",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = HetuTheme.colors.onSurface
                    )
                    Text(
                        text = "Your data never leaves this device",
                        fontSize = 11.sp,
                        color = HetuTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    modelViewModel: ModelViewModel
) {
    val generatedText by modelViewModel.generatedText.collectAsState()
    val isGenerating by modelViewModel.isGenerating.collectAsState()
    val uiState by modelViewModel.uiState.collectAsState()
    val sdkAvailable by modelViewModel.sdkAvailable.collectAsState()
    var inputText by remember { mutableStateOf("") }
    
    val messages = uiState.messages

    Scaffold(
        containerColor = HetuTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Hetu Journal",
                            fontWeight = FontWeight.Bold,
                            color = HetuTheme.colors.onBackground
                        )
                        Text(
                            text = if (uiState.isLLMReady) "Ready" else "Waiting for model...",
                            fontSize = 12.sp,
                            color = HetuTheme.colors.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = HetuTheme.colors.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Clear",
                            tint = HetuTheme.colors.accent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HetuTheme.colors.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    ChatBubble(message)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (generatedText.isNotEmpty()) {
                    item {
                        ChatBubble(ChatMessage(generatedText, false))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else if (isGenerating) {
                    item {
                        ChatBubble(ChatMessage("Thinking...", false))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Error handling
            val error by modelViewModel.error.collectAsState()
            val context = LocalContext.current
            LaunchedEffect(error) {
                error?.let {
                    if (it.isNotEmpty()) {
                         android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
                         modelViewModel.clearError()
                    }
                }
            }
            
            // Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Button
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(HetuTheme.colors.accent)
                ) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = "Voice",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Text Input
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Type your thoughts...",
                            color = HetuTheme.colors.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = HetuTheme.colors.surface,
                        unfocusedContainerColor = HetuTheme.colors.surface,
                        focusedTextColor = HetuTheme.colors.onBackground,
                        unfocusedTextColor = HetuTheme.colors.onBackground
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isGenerating) {
                            modelViewModel.generateResponse(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank()) HetuTheme.colors.surfaceVariant
                            else HetuTheme.colors.surface
                        )
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = HetuTheme.colors.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank())
                                HetuTheme.colors.primary
                            else
                                HetuTheme.colors.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser)
                    HetuTheme.colors.primary.copy(alpha = 0.2f)
                else
                    HetuTheme.colors.surface
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = HetuTheme.colors.onBackground,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var whatTried by remember { mutableStateOf("") }
    var expectedOutcome by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }
    var reminderDays by remember { mutableIntStateOf(3) }
    
    val categories = listOf(
        "🍎 Food", "😴 Sleep", "🏃 Exercise", 
        "🧘 Wellness", "💊 Supplement", "🎯 Other"
    )

    Scaffold(
        containerColor = HetuTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Track Something",
                        fontWeight = FontWeight.Bold,
                        color = HetuTheme.colors.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
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
            item {
                // Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { selectedTab = 0 },
                        label = { Text("What I tried") },
                        selected = selectedTab == 0,
                        leadingIcon = { Icon(Icons.Filled.Add, null, Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HetuTheme.colors.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        onClick = { selectedTab = 1 },
                        label = { Text("How I feel") },
                        selected = selectedTab == 1,
                        leadingIcon = { Icon(Icons.Outlined.EmojiEmotions, null, Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HetuTheme.colors.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            
            item {
                // Date Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = HetuTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Today",
                                fontWeight = FontWeight.Medium,
                                color = HetuTheme.colors.onBackground
                            )
                            Text(
                                "Thursday, January 30",
                                fontSize = 14.sp,
                                color = HetuTheme.colors.onSurface
                            )
                        }
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = HetuTheme.colors.onSurface
                        )
                    }
                }
            }
            
            item {
                // Category
                Text(
                    "Category",
                    fontWeight = FontWeight.Medium,
                    color = HetuTheme.colors.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            selected = selectedCategory == category,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HetuTheme.colors.surfaceVariant
                            )
                        )
                    }
                }
            }
            
            item {
                // What did you try
                OutlinedTextField(
                    value = whatTried,
                    onValueChange = { whatTried = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("What did you try?") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HetuTheme.colors.primary,
                        unfocusedBorderColor = HetuTheme.colors.surface,
                        focusedContainerColor = HetuTheme.colors.surface,
                        unfocusedContainerColor = HetuTheme.colors.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )
            }
            
            item {
                // Expected outcome
                Text(
                    "What do you expect?",
                    fontWeight = FontWeight.Medium,
                    color = HetuTheme.colors.onBackground
                )
                Text(
                    "This helps track if your predictions match reality",
                    fontSize = 12.sp,
                    color = HetuTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = expectedOutcome,
                    onValueChange = { expectedOutcome = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Better sleep quality in 2-3 days") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HetuTheme.colors.primary,
                        unfocusedBorderColor = HetuTheme.colors.surface,
                        focusedContainerColor = HetuTheme.colors.surface,
                        unfocusedContainerColor = HetuTheme.colors.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            item {
                // Reminder
                Text(
                    "Remind me to check in after",
                    fontWeight = FontWeight.Medium,
                    color = HetuTheme.colors.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 3, 7).forEach { days ->
                        FilterChip(
                            onClick = { reminderDays = days },
                            label = { Text("$days day${if (days > 1) "s" else ""}") },
                            selected = reminderDays == days,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HetuTheme.colors.surfaceVariant
                            )
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onNavigateBack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HetuTheme.colors.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Entry", modifier = Modifier.padding(vertical = 8.dp))
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun FeedScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Feed",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = HetuTheme.colors.onBackground
            )
            Text(
                text = "Your journal entries",
                fontSize = 14.sp,
                color = HetuTheme.colors.onSurface
            )
        }
        
        items(3) { index ->
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
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = HetuTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "January ${30 - index}",
                            fontSize = 12.sp,
                            color = HetuTheme.colors.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (index) {
                            0 -> "Felt more energetic today after morning walk"
                            1 -> "Trying new sleep schedule - 10pm bedtime"
                            else -> "Started meditation practice"
                        },
                        fontSize = 15.sp,
                        color = HetuTheme.colors.onBackground,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        AssistChip(
                            onClick = { },
                            label = { Text(if (index == 1) "😴 Sleep" else "🧘 Wellness", fontSize = 12.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = HetuTheme.colors.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = HetuTheme.colors.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = null,
                        tint = HetuTheme.colors.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Add more entries",
                        fontSize = 14.sp,
                        color = HetuTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.Timeline,
                contentDescription = null,
                tint = HetuTheme.colors.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Timeline",
                color = HetuTheme.colors.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Coming soon",
                color = HetuTheme.colors.onSurface,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun InsightsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Insights",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = HetuTheme.colors.onBackground
        )
        Text(
            text = "Your personal stats",
            fontSize = 14.sp,
            color = HetuTheme.colors.onSurface
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
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
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = "Insights",
                    tint = HetuTheme.colors.onSurface,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Not enough data yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = HetuTheme.colors.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Keep tracking actions and outcomes. Once you have at least 5 entries, we'll start looking for patterns.",
                    fontSize = 14.sp,
                    color = HetuTheme.colors.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
