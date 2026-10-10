package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.SubtaskEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskWithSubtasks
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Transaction
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY isCompleted ASC, isPinned DESC, priorityRank ASC, id ASC")
    fun getTasksWithSubtasksForDate(date: String): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks ORDER BY date DESC, isCompleted ASC")
    fun getAllTasksWithSubtasks(): Flow<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskWithSubtasksById(id: Long): TaskWithSubtasks?

    @Query("SELECT * FROM tasks WHERE isCompleted = 1")
    fun getAllCompletedTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Long, isCompleted: Boolean, completedAt: Long?)

    @Query("UPDATE tasks SET isPinned = :isPinned WHERE id = :taskId")
    suspend fun updateTaskPinned(taskId: Long, isPinned: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Query("DELETE FROM subtasks WHERE id = :subtaskId")
    suspend fun deleteSubtask(subtaskId: Long)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasksForTask(taskId: Long)

    @Query("UPDATE subtasks SET isCompleted = :isCompleted WHERE id = :subtaskId")
    suspend fun updateSubtaskCompletion(subtaskId: Long, isCompleted: Boolean)

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND date = :date")
    fun getCompletedCountForDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE date = :date")
    fun getTotalCountForDate(date: String): Flow<Int>
}
