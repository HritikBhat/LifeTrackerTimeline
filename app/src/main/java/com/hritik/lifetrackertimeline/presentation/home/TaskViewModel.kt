package com.hritik.lifetrackertimeline.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hritik.lifetrackertimeline.data.local.entity.TaskEntity
import com.hritik.lifetrackertimeline.data.local.entity.TimelineEntity
import com.hritik.lifetrackertimeline.data.repository.TaskRepository
import com.hritik.lifetrackertimeline.data.repository.TimelineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val timelineRepository: TimelineRepository
) : ViewModel() {

    val allTasks: StateFlow<List<TaskEntity>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun insertTask(task: TaskEntity): Int {
        return repository.insertTask(task).toInt()
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
    
    suspend fun getTaskById(id: Int): TaskEntity? {
        return repository.getTaskById(id)
    }

    suspend fun upsertTimelineEntry(timeSlot: String, taskId: Int, date: String) {
        timelineRepository.deleteEntryByTimeAndDate(timeSlot, date)
        timelineRepository.insertTimelineEntry(
            TimelineEntity(
                timeSlot = timeSlot,
                taskId = taskId,
                date = date
            )
        )
    }
}
