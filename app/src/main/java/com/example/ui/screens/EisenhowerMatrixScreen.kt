package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Priority
import com.example.data.model.TaskWithSubtasks
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.PriorityUrgent
import com.example.ui.viewmodel.TaskViewModel

@Composable
fun EisenhowerMatrixScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val selectedDateTasks by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()

    var selectedScopeIndex by remember { mutableIntStateOf(0) } // 0: Selected Day, 1: All Days

    val currentList = if (selectedScopeIndex == 0) selectedDateTasks else allTasks

    val doFirstTasks = currentList.filter { it.task.priority == Priority.URGENT.name }
    val scheduleTasks = currentList.filter { it.task.priority == Priority.HIGH.name }
    val delegateTasks = currentList.filter { it.task.priority == Priority.MEDIUM.name }
    val eliminateTasks = currentList.filter { it.task.priority == Priority.LOW.name }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("eisenhower_matrix_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Priority Matrix",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Eisenhower 4-quadrant decision framework",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedScopeIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(14.dp)),
                    indicator = {},
                    divider = {}
                ) {
                    Tab(
                        selected = selectedScopeIndex == 0,
                        onClick = { selectedScopeIndex = 0 },
                        text = {
                            Text(
                                "Today's Priorities",
                                fontWeight = if (selectedScopeIndex == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_matrix_today")
                    )
                    Tab(
                        selected = selectedScopeIndex == 1,
                        onClick = { selectedScopeIndex = 1 },
                        text = {
                            Text(
                                "All Tasks Matrix",
                                fontWeight = if (selectedScopeIndex == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_matrix_all")
                    )
                }
            }
        }

        // Q1: Do First (Urgent & Important)
        item {
            MatrixQuadrantCard(
                title = "1. DO FIRST",
                subtitle = "Urgent & High Impact • Do today",
                color = PriorityUrgent,
                tasks = doFirstTasks,
                onToggle = { task -> viewModel.toggleTaskCompletion(task.task.id, task.task.isCompleted) }
            )
        }

        // Q2: Schedule (Not Urgent but Important)
        item {
            MatrixQuadrantCard(
                title = "2. SCHEDULE",
                subtitle = "High Value • Plan dedicated deep focus",
                color = PriorityHigh,
                tasks = scheduleTasks,
                onToggle = { task -> viewModel.toggleTaskCompletion(task.task.id, task.task.isCompleted) }
            )
        }

        // Q3: Delegate / Quick (Urgent, Medium Impact)
        item {
            MatrixQuadrantCard(
                title = "3. DELEGATE / QUICK",
                subtitle = "Medium Impact • Batch or knock out fast",
                color = PriorityMedium,
                tasks = delegateTasks,
                onToggle = { task -> viewModel.toggleTaskCompletion(task.task.id, task.task.isCompleted) }
            )
        }

        // Q4: Eliminate / Low Priority
        item {
            MatrixQuadrantCard(
                title = "4. LOW PRIORITY",
                subtitle = "Nice to have • Do during free time",
                color = PriorityLow,
                tasks = eliminateTasks,
                onToggle = { task -> viewModel.toggleTaskCompletion(task.task.id, task.task.isCompleted) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
private fun MatrixQuadrantCard(
    title: String,
    subtitle: String,
    color: Color,
    tasks: List<TaskWithSubtasks>,
    onToggle: (TaskWithSubtasks) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.06f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = color
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${tasks.count { it.task.isCompleted }}/${tasks.size}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            if (tasks.isEmpty()) {
                Text(
                    text = "No tasks in this quadrant",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                tasks.forEach { taskItem ->
                    val task = taskItem.task
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable { onToggle(taskItem) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (task.isCompleted) color else Color.Transparent)
                                .border(1.5.dp, color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (task.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        task.time?.let { timeStr ->
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
