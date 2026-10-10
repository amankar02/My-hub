package com.example.data.repository

import com.example.data.local.HabitDao
import com.example.data.local.TaskDao
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.SubtaskEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskWithSubtasks
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val habitDao: HabitDao
) {
    fun getTasksForDate(date: String): Flow<List<TaskWithSubtasks>> =
        taskDao.getTasksWithSubtasksForDate(date)

    fun getAllTasks(): Flow<List<TaskWithSubtasks>> =
        taskDao.getAllTasksWithSubtasks()

    suspend fun getTaskById(taskId: Long): TaskWithSubtasks? =
        taskDao.getTaskWithSubtasksById(taskId)

    suspend fun insertTaskWithSubtasks(
        task: TaskEntity,
        subtaskTitles: List<String>
    ): Long {
        val taskId = taskDao.insertTask(task)
        if (subtaskTitles.isNotEmpty()) {
            val subtasks = subtaskTitles.mapIndexed { index, title ->
                SubtaskEntity(
                    taskId = taskId,
                    title = title.trim(),
                    isCompleted = false,
                    orderIndex = index
                )
            }
            taskDao.insertSubtasks(subtasks)
        }
        return taskId
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun toggleTaskCompletion(taskId: Long, currentlyCompleted: Boolean) {
        val now = if (!currentlyCompleted) System.currentTimeMillis() else null
        taskDao.updateTaskCompletion(taskId, !currentlyCompleted, now)
    }

    suspend fun toggleTaskPinned(taskId: Long, currentlyPinned: Boolean) {
        taskDao.updateTaskPinned(taskId, !currentlyPinned)
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteSubtasksForTask(taskId)
        taskDao.deleteTaskById(taskId)
    }

    suspend fun addSubtask(taskId: Long, title: String) {
        taskDao.insertSubtask(
            SubtaskEntity(
                taskId = taskId,
                title = title.trim(),
                isCompleted = false
            )
        )
    }

    suspend fun toggleSubtaskCompletion(subtaskId: Long, currentlyCompleted: Boolean) {
        taskDao.updateSubtaskCompletion(subtaskId, !currentlyCompleted)
    }

    suspend fun deleteSubtask(subtaskId: Long) {
        taskDao.deleteSubtask(subtaskId)
    }

    // Habits
    fun getAllHabits(): Flow<List<HabitEntity>> =
        habitDao.getAllHabits()

    fun getHabitLogsForDate(date: String): Flow<List<HabitLogEntity>> =
        habitDao.getHabitLogsForDate(date)

    fun getAllHabitLogs(): Flow<List<HabitLogEntity>> =
        habitDao.getAllHabitLogs()

    suspend fun insertHabit(habit: HabitEntity): Long =
        habitDao.insertHabit(habit)

    suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteHabitById(habitId)
    }

    suspend fun toggleHabitForDate(habitId: Long, date: String) {
        val count = habitDao.isHabitCompletedOnDate(habitId, date)
        if (count > 0) {
            habitDao.deleteHabitLog(habitId, date)
        } else {
            habitDao.insertHabitLog(
                HabitLogEntity(
                    habitId = habitId,
                    date = date
                )
            )
        }
    }
}
