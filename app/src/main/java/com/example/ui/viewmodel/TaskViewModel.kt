package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.Priority
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.data.model.TaskWithSubtasks
import com.example.data.repository.TaskRepository
import com.example.util.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StatusFilter {
    ALL,
    PENDING,
    COMPLETED
}

data class DailyStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val completionPercentage: Float = 0f,
    val totalEstimatedMinutes: Int = 0,
    val completedMinutes: Int = 0
)

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(DateUtils.getTodayString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedCategory = MutableStateFlow<TaskCategory?>(null)
    val selectedCategory: StateFlow<TaskCategory?> = _selectedCategory.asStateFlow()

    private val _statusFilter = MutableStateFlow(StatusFilter.ALL)
    val statusFilter: StateFlow<StatusFilter> = _statusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Tasks for currently selected date
    private val rawTasksForDate = _selectedDate.flatMapLatest { date ->
        repository.getTasksForDate(date)
    }

    // Filtered tasks for selected date
    val tasksForSelectedDate: StateFlow<List<TaskWithSubtasks>> = combine(
        rawTasksForDate,
        _selectedCategory,
        _statusFilter,
        _searchQuery
    ) { tasks, category, status, query ->
        tasks.filter { item ->
            val matchesCategory = category == null || item.task.category == category.name
            val matchesStatus = when (status) {
                StatusFilter.ALL -> true
                StatusFilter.PENDING -> !item.task.isCompleted
                StatusFilter.COMPLETED -> item.task.isCompleted
            }
            val matchesQuery = query.isBlank() ||
                    item.task.title.contains(query, ignoreCase = true) ||
                    item.task.description.contains(query, ignoreCase = true)
            matchesCategory && matchesStatus && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Daily statistics for selected date
    val dailyStats: StateFlow<DailyStats> = rawTasksForDate.map { tasks ->
        val total = tasks.size
        val completed = tasks.count { it.task.isCompleted }
        val pending = total - completed
        val percent = if (total > 0) completed.toFloat() / total.toFloat() else 0f
        val totalMins = tasks.sumOf { it.task.estimatedMinutes }
        val doneMins = tasks.filter { it.task.isCompleted }.sumOf { it.task.estimatedMinutes }
        DailyStats(
            totalTasks = total,
            completedTasks = completed,
            pendingTasks = pending,
            completionPercentage = percent,
            totalEstimatedMinutes = totalMins,
            completedMinutes = doneMins
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyStats()
    )

    // All tasks for Eisenhower Matrix view
    val allTasks: StateFlow<List<TaskWithSubtasks>> = repository.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Habits
    val habits: StateFlow<List<HabitEntity>> = repository.getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Habit logs for today
    val todayHabitLogs: StateFlow<Set<Long>> = repository.getHabitLogsForDate(DateUtils.getTodayString())
        .map { logs -> logs.map { it.habitId }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    // All habit logs for streak analysis
    val allHabitLogs: StateFlow<List<HabitLogEntity>> = repository.getAllHabitLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Focus Timer State
    private val _selectedFocusTaskId = MutableStateFlow<Long?>(null)
    val selectedFocusTaskId: StateFlow<Long?> = _selectedFocusTaskId.asStateFlow()

    private val _timerDurationMinutes = MutableStateFlow(25)
    val timerDurationMinutes: StateFlow<Int> = _timerDurationMinutes.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(25 * 60)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _completedFocusSessions = MutableStateFlow(0)
    val completedFocusSessions: StateFlow<Int> = _completedFocusSessions.asStateFlow()

    private var timerJob: Job? = null

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun setCategoryFilter(category: TaskCategory?) {
        _selectedCategory.value = category
    }

    fun setStatusFilter(filter: StatusFilter) {
        _statusFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun createTask(
        title: String,
        description: String,
        date: String,
        time: String?,
        priority: Priority,
        category: TaskCategory,
        estimatedMinutes: Int,
        subtaskTitles: List<String>
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                date = date,
                time = time?.trim()?.ifBlank { null },
                priority = priority.name,
                priorityRank = priority.rank,
                category = category.name,
                estimatedMinutes = estimatedMinutes,
                isCompleted = false
            )
            repository.insertTaskWithSubtasks(task, subtaskTitles)
        }
    }

    fun toggleTaskCompletion(taskId: Long, currentlyCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(taskId, currentlyCompleted)
        }
    }

    fun toggleTaskPinned(taskId: Long, currentlyPinned: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskPinned(taskId, currentlyPinned)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            if (_selectedFocusTaskId.value == taskId) {
                _selectedFocusTaskId.value = null
            }
            repository.deleteTask(taskId)
        }
    }

    fun addSubtask(taskId: Long, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addSubtask(taskId, title)
        }
    }

    fun toggleSubtaskCompletion(subtaskId: Long, currentlyCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleSubtaskCompletion(subtaskId, currentlyCompleted)
        }
    }

    fun deleteSubtask(subtaskId: Long) {
        viewModelScope.launch {
            repository.deleteSubtask(subtaskId)
        }
    }

    fun createHabit(
        title: String,
        description: String,
        iconKey: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            val habit = HabitEntity(
                title = title.trim(),
                description = description.trim(),
                iconKey = iconKey,
                colorHex = colorHex
            )
            repository.insertHabit(habit)
        }
    }

    fun toggleHabit(habitId: Long, date: String = DateUtils.getTodayString()) {
        viewModelScope.launch {
            repository.toggleHabitForDate(habitId, date)
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    // Timer Controls
    fun setTimerDuration(minutes: Int) {
        if (_isTimerRunning.value) return
        _timerDurationMinutes.value = minutes
        _timerRemainingSeconds.value = minutes * 60
    }

    fun selectFocusTask(taskId: Long?) {
        _selectedFocusTaskId.value = taskId
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value && _timerRemainingSeconds.value > 0) {
                delay(1000)
                _timerRemainingSeconds.value -= 1
            }
            if (_timerRemainingSeconds.value <= 0) {
                _isTimerRunning.value = false
                _completedFocusSessions.value += 1
                _selectedFocusTaskId.value?.let { taskId ->
                    repository.toggleTaskCompletion(taskId, false)
                }
                _timerRemainingSeconds.value = _timerDurationMinutes.value * 60
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTimer() {
        pauseTimer()
        _timerRemainingSeconds.value = _timerDurationMinutes.value * 60
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

class TaskViewModelFactory(
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
