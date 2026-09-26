package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CribScreen
import com.example.ui.screens.MemoryProfileScreen
import com.example.ui.screens.PersonaScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.theme.KaraAccentPink
import com.example.ui.theme.KaraPrimary
import kotlinx.coroutines.flow.collectLatest

enum class KaraNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    CHAT("Chat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline, "tab_chat"),
    CRIB("Crib", Icons.Filled.Home, Icons.Outlined.Home, "tab_crib"),
    SCHEDULE("Schedule", Icons.AutoMirrored.Filled.EventNote, Icons.AutoMirrored.Outlined.EventNote, "tab_schedule"),
    PERSONA("Bond", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "tab_persona"),
    PROFILE("Memory", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, "tab_profile")
}

@Composable
fun KaraApp(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(KaraNavTab.CHAT) }
    val snackbarHostState = remember { SnackbarHostState() }
    val pendingTasksCount by viewModel.pendingTasksCount.collectAsState()

    // Handle back button: if not on Chat, navigate back to Chat
    BackHandler(enabled = currentTab != KaraNavTab.CHAT) {
        currentTab = KaraNavTab.CHAT
    }

    // Listen for toast events
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("kara_bottom_navigation")
            ) {
                KaraNavTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            if (tab == KaraNavTab.SCHEDULE && pendingTasksCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = KaraAccentPink) {
                                            Text(text = "$pendingTasksCount")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            }
                        },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = KaraPrimary,
                            indicatorColor = KaraPrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "nav_transition"
            ) { target ->
                when (target) {
                    KaraNavTab.CHAT -> ChatScreen(
                        viewModel = viewModel,
                        onNavigateToPersona = { currentTab = KaraNavTab.PERSONA },
                        onNavigateToCrib = { currentTab = KaraNavTab.CRIB }
                    )
                    KaraNavTab.CRIB -> CribScreen(viewModel = viewModel)
                    KaraNavTab.SCHEDULE -> ScheduleScreen(viewModel = viewModel)
                    KaraNavTab.PERSONA -> PersonaScreen(viewModel = viewModel)
                    KaraNavTab.PROFILE -> MemoryProfileScreen(viewModel = viewModel)
                }
            }
        }
    }
}
