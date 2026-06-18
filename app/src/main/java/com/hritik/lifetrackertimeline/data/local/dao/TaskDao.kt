package com.hritik.lifetrackertimeline.data.local.dao

import androidx.room.*
import com.hritik.lifetrackertimeline.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY lastSelectedAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): TaskEntity?

    @Query("UPDATE tasks SET lastSelectedAt = :timestamp WHERE id = :taskId")
    suspend fun updateLastSelectedAt(taskId: Int, timestamp: Long)
}