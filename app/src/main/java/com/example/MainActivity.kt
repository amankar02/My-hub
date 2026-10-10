package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.TaskRepository
import com.example.ui.screens.EisenhowerMatrixScreen
import com.example.ui.screens.FocusTimerScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TaskViewModel
import com.example.ui.viewmodel.TaskViewModelFactory

enum class AppScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    TODAY("Today", Icons.Filled.Today, Icons.Outlined.Today, "nav_item_today"),
    MATRIX("Matrix", Icons.Filled.DashboardCustomize, Icons.Outlined.DashboardCustomize, "nav_item_matrix"),
    HABITS("Habits", Icons.Filled.Repeat, Icons.Outlined.Repeat, "nav_item_habits"),
    FOCUS("Focus", Icons.Filled.Timer, Icons.Outlined.Timer, "nav_item_focus")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DayFlowApp()
            }
        }
    }
}

@Composable
fun DayFlowApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val repository = remember { TaskRepository(database.taskDao(), database.habitDao()) }
    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(repository)
    )

    var currentScreen by remember { mutableStateOf(AppScreen.TODAY) }

    // If on a secondary screen, pressing back returns to the Today screen
    if (currentScreen != AppScreen.TODAY) {
        BackHandler {
            currentScreen = AppScreen.TODAY
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AppScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(screen.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "screen_transition"
        ) { targetScreen ->
            when (targetScreen) {
                AppScreen.TODAY -> {
                    TodayScreen(
                        viewModel = viewModel,
                        onNavigateToFocus = {
                            currentScreen = AppScreen.FOCUS
                        }
                    )
                }
                AppScreen.MATRIX -> {
                    EisenhowerMatrixScreen(
                        viewModel = viewModel
                    )
                }
                AppScreen.HABITS -> {
                    HabitsScreen(
                        viewModel = viewModel
                    )
                }
                AppScreen.FOCUS -> {
                    FocusTimerScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
