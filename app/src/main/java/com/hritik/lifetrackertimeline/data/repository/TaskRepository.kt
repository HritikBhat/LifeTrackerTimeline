package com.hritik.lifetrackertimeline.data.repository

import com.hritik.lifetrackertimeline.data.local.dao.TaskDao
import com.hritik.lifetrackertimeline.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun getTaskById(taskId: Int): TaskEntity? = taskDao.getTaskById(taskId)
}