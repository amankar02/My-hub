package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.Priority
import com.example.data.model.SubtaskEntity
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TaskEntity::class,
        SubtaskEntity::class,
        HabitEntity::class,
        HabitLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dayflow_task_database"
                )
                    .addCallback(DatabasePrepopulateCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabasePrepopulateCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = INSTANCE ?: return@launch
                val taskDao = database.taskDao()
                val habitDao = database.habitDao()
                val today = DateUtils.getTodayString()

                // Insert Starter Tasks
                val taskId1 = taskDao.insertTask(
                    TaskEntity(
                        title = "Review Q4 product roadmap & deliverables",
                        description = "Align priority milestones with the product engineering squad",
                        date = today,
                        time = "09:30 AM",
                        priority = Priority.URGENT.name,
                        priorityRank = Priority.URGENT.rank,
                        category = TaskCategory.WORK.name,
                        isCompleted = false,
                        estimatedMinutes = 45,
                        isPinned = true
                    )
                )
                taskDao.insertSubtasks(
                    listOf(
                        SubtaskEntity(taskId = taskId1, title = "Check feature branch readiness", isCompleted = true, orderIndex = 0),
                        SubtaskEntity(taskId = taskId1, title = "Draft release retrospective notes", isCompleted = false, orderIndex = 1),
                        SubtaskEntity(taskId = taskId1, title = "Share summary in squad channel", isCompleted = false, orderIndex = 2)
                    )
                )

                val taskId2 = taskDao.insertTask(
                    TaskEntity(
                        title = "30-minute cardio & strength routine",
                        description = "HIIT session and light mobility stretching",
                        date = today,
                        time = "07:00 AM",
                        priority = Priority.HIGH.name,
                        priorityRank = Priority.HIGH.rank,
                        category = TaskCategory.HEALTH.name,
                        isCompleted = true,
                        completedAt = System.currentTimeMillis() - 3600000,
                        estimatedMinutes = 30
                    )
                )
                taskDao.insertSubtasks(
                    listOf(
                        SubtaskEntity(taskId = taskId2, title = "Warm-up stretches (5 min)", isCompleted = true, orderIndex = 0),
                        SubtaskEntity(taskId = taskId2, title = "Core workout routine (20 min)", isCompleted = true, orderIndex = 1),
                        SubtaskEntity(taskId = taskId2, title = "Cool-down and hydrate", isCompleted = true, orderIndex = 2)
                    )
                )

                val taskId3 = taskDao.insertTask(
                    TaskEntity(
                        title = "Prepare monthly budget review",
                        description = "Reconcile subscription expenses and calculate emergency fund allocation",
                        date = today,
                        time = "02:00 PM",
                        priority = Priority.MEDIUM.name,
                        priorityRank = Priority.MEDIUM.rank,
                        category = TaskCategory.FINANCE.name,
                        isCompleted = false,
                        estimatedMinutes = 25
                    )
                )
                taskDao.insertSubtasks(
                    listOf(
                        SubtaskEntity(taskId = taskId3, title = "Export bank statements", isCompleted = true, orderIndex = 0),
                        SubtaskEntity(taskId = taskId3, title = "Tag irregular line items", isCompleted = false, orderIndex = 1)
                    )
                )

                val taskId4 = taskDao.insertTask(
                    TaskEntity(
                        title = "Read 15 pages of deep work book",
                        description = "Cal Newport's rules for focused success in a distracted world",
                        date = today,
                        time = "08:30 PM",
                        priority = Priority.LOW.name,
                        priorityRank = Priority.LOW.rank,
                        category = TaskCategory.STUDY.name,
                        isCompleted = false,
                        estimatedMinutes = 20
                    )
                )

                // Insert Starter Habits
                val habit1 = habitDao.insertHabit(
                    HabitEntity(
                        title = "Drink 2.5L Water",
                        description = "Stay hydrated throughout the workday",
                        iconKey = "water",
                        colorHex = "#06B6D4",
                        currentStreak = 5,
                        bestStreak = 12
                    )
                )
                val habit2 = habitDao.insertHabit(
                    HabitEntity(
                        title = "Morning Meditation",
                        description = "10 minutes of mindfulness breathing",
                        iconKey = "meditate",
                        colorHex = "#8B5CF6",
                        currentStreak = 3,
                        bestStreak = 7
                    )
                )
                val habit3 = habitDao.insertHabit(
                    HabitEntity(
                        title = "Daily Code / Reading",
                        description = "At least 30 mins learning new concepts",
                        iconKey = "code",
                        colorHex = "#10B981",
                        currentStreak = 8,
                        bestStreak = 14
                    )
                )

                // Log today for habit1 to show active streak
                habitDao.insertHabitLog(
                    HabitLogEntity(
                        habitId = habit1,
                        date = today
                    )
                )
            }
        }
    }
}
