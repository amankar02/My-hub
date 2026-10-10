package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Priority
import com.example.data.model.TaskCategory
import com.example.ui.components.CreateTaskBottomSheet
import com.example.ui.components.DailyProgressCard
import com.example.ui.components.DateStripBar
import com.example.ui.components.TaskItemCard
import com.example.ui.viewmodel.DailyStats
import com.example.ui.viewmodel.StatusFilter
import com.example.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TaskViewModel,
    onNavigateToFocus: (taskId: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val tasks by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
    val dailyStats by viewModel.dailyStats.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showCreateSheet by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    var quickTaskTitle by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_task")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Task",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Top App Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DayFlow",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Daily Action & Routine Planner",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Streak badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "5d streak",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { isSearchVisible = !isSearchVisible },
                            modifier = Modifier.testTag("toggle_search_button")
                        ) {
                            Icon(
                                imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search Tasks",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Search input field (collapsible)
            item {
                AnimatedVisibility(visible = isSearchVisible) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search today's tasks...") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_tasks_input"),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            // Date Strip
            item {
                DateStripBar(
                    selectedDate = selectedDate,
                    onDateSelected = { viewModel.selectDate(it) }
                )
            }

            // Progress Overview Card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    DailyProgressCard(
                        dateString = selectedDate,
                        stats = dailyStats
                    )
                }
            }

            // Quick Add Input Row
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = quickTaskTitle,
                            onValueChange = { quickTaskTitle = it },
                            placeholder = { Text("+ Quick add task for today...", fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_add_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (quickTaskTitle.isNotBlank()) {
                                    viewModel.createTask(
                                        title = quickTaskTitle,
                                        description = "",
                                        date = selectedDate,
                                        time = null,
                                        priority = Priority.MEDIUM,
                                        category = TaskCategory.WORK,
                                        estimatedMinutes = 25,
                                        subtaskTitles = emptyList()
                                    )
                                    quickTaskTitle = ""
                                }
                            },
                            enabled = quickTaskTitle.isNotBlank(),
                            modifier = Modifier.testTag("quick_add_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Quick Task",
                                tint = if (quickTaskTitle.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    item {
                        CategoryPill(
                            label = "All Categories",
                            isSelected = selectedCategory == null,
                            onClick = { viewModel.setCategoryFilter(null) },
                            testTag = "filter_category_all"
                        )
                    }
                    items(TaskCategory.values()) { category ->
                        CategoryPill(
                            label = category.label,
                            isSelected = selectedCategory == category,
                            onClick = { viewModel.setCategoryFilter(category) },
                            testTag = "filter_category_${category.name}"
                        )
                    }
                }
            }

            // Status Tabs (All / Pending / Completed)
            item {
                TabRow(
                    selectedTabIndex = statusFilter.ordinal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[statusFilter.ordinal]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    StatusFilter.values().forEach { filter ->
                        Tab(
                            selected = statusFilter == filter,
                            onClick = { viewModel.setStatusFilter(filter) },
                            text = {
                                Text(
                                    text = when (filter) {
                                        StatusFilter.ALL -> "All (${dailyStats.totalTasks})"
                                        StatusFilter.PENDING -> "Pending (${dailyStats.pendingTasks})"
                                        StatusFilter.COMPLETED -> "Done (${dailyStats.completedTasks})"
                                    },
                                    fontWeight = if (statusFilter == filter) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier.testTag("tab_status_${filter.name}")
                        )
                    }
                }
            }

            // Empty State
            if (tasks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (statusFilter == StatusFilter.COMPLETED) "No completed tasks yet" else "No tasks for this date",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button to schedule your daily priorities.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                items(tasks, key = { it.task.id }) { taskWithSubtasks ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        TaskItemCard(
                            taskItem = taskWithSubtasks,
                            onToggleCompletion = { currentlyDone ->
                                viewModel.toggleTaskCompletion(taskWithSubtasks.task.id, currentlyDone)
                            },
                            onTogglePin = { currentlyPinned ->
                                viewModel.toggleTaskPinned(taskWithSubtasks.task.id, currentlyPinned)
                            },
                            onDelete = {
                                viewModel.deleteTask(taskWithSubtasks.task.id)
                            },
                            onStartFocus = {
                                viewModel.selectFocusTask(taskWithSubtasks.task.id)
                                viewModel.setTimerDuration(taskWithSubtasks.task.estimatedMinutes)
                                onNavigateToFocus(taskWithSubtasks.task.id)
                            },
                            onToggleSubtask = { subtaskId, isCompleted ->
                                viewModel.toggleSubtaskCompletion(subtaskId, isCompleted)
                            },
                            onAddSubtask = { title ->
                                viewModel.addSubtask(taskWithSubtasks.task.id, title)
                            },
                            onDeleteSubtask = { subtaskId ->
                                viewModel.deleteSubtask(subtaskId)
                            }
                        )
                    }
                }
            }
        }

        // Create Task Bottom Sheet
        if (showCreateSheet) {
            CreateTaskBottomSheet(
                initialDate = selectedDate,
                onDismiss = { showCreateSheet = false },
                onCreateTask = { title, desc, date, time, priority, category, mins, subtasksList ->
                    viewModel.createTask(
                        title = title,
                        description = desc,
                        date = date,
                        time = time,
                        priority = priority,
                        category = category,
                        estimatedMinutes = mins,
                        subtaskTitles = subtasksList
                    )
                }
            )
        }
    }
}

@Composable
private fun CategoryPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}
