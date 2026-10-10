package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class Priority(val label: String, val rank: Int) {
    URGENT("Urgent", 1),
    HIGH("High", 2),
    MEDIUM("Medium", 3),
    LOW("Low", 4)
}

enum class TaskCategory(val label: String) {
    WORK("Work"),
    PERSONAL("Personal"),
    HEALTH("Health"),
    STUDY("Study"),
    FINANCE("Finance"),
    CREATIVE("Creative")
}

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["date"]), Index(value = ["isCompleted"])]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: String, // YYYY-MM-DD
    val time: String? = null, // e.g. "09:30 AM"
    val priority: String = Priority.MEDIUM.name,
    val priorityRank: Int = Priority.MEDIUM.rank,
    val category: String = TaskCategory.WORK.name,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val estimatedMinutes: Int = 25,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"])]
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)

data class TaskWithSubtasks(
    @Embedded
    val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubtaskEntity> = emptyList()
) {
    val totalSubtasks: Int get() = subtasks.size
    val completedSubtasks: Int get() = subtasks.count { it.isCompleted }
    val progress: Float get() = if (subtasks.isEmpty()) {
        if (task.isCompleted) 1f else 0f
    } else {
        completedSubtasks.toFloat() / totalSubtasks.toFloat()
    }
}

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val iconKey: String = "check", // water, run, book, meditate, code, sleep, check
    val colorHex: String = "#4F46E5",
    val targetDaysPerWeek: Int = 7,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "habit_logs",
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String, // YYYY-MM-DD
    val completedAt: Long = System.currentTimeMillis()
)
