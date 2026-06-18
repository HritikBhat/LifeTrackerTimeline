package com.hritik.lifetrackertimeline.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hritik.lifetrackertimeline.data.local.entity.TaskEntity
import com.hritik.lifetrackertimeline.data.local.entity.TimelineEntity
import com.hritik.lifetrackertimeline.data.repository.TaskRepository
import com.hritik.lifetrackertimeline.data.repository.TimelineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class TimelineUiItem(
    val timelineId: Int,
    val timeSlot: String,
    val taskId: Int,
    val title: String,
    val description: String,
    val color: Int,
    val icon: String,
    val isCompleted: Boolean
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val timelineRepository: TimelineRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val availableTasks: StateFlow<List<TaskEntity>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val timelineItems: StateFlow<Map<String, TimelineUiItem>> = combine(
        _selectedDate.flatMapLatest { date -> timelineRepository.getTimelineByDate(date) },
        taskRepository.getAllTasks()
    ) { timelineEntries, tasks ->
        timelineEntries.mapNotNull { entry ->
            tasks.find { it.id == entry.taskId }?.let { task ->
                TimelineUiItem(
                    timelineId = entry.id,
                    timeSlot = entry.timeSlot,
                    taskId = entry.taskId,
                    title = task.title,
                    description = task.notes,
                    color = task.color,
                    icon = task.icon,
                    isCompleted = entry.isCompleted
                )
            }
        }.associateBy { it.timeSlot }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    suspend fun upsertTimelineEntry(timeSlot: String, taskId: Int) {
        // Update last selected time for the task
        taskRepository.updateLastSelectedAt(taskId, System.currentTimeMillis())

        // Check if entry exists for this slot and date, if so update/replace
        timelineRepository.deleteEntryByTimeAndDate(timeSlot, _selectedDate.value)
        timelineRepository.insertTimelineEntry(
            TimelineEntity(
                timeSlot = timeSlot,
                taskId = taskId,
                date = _selectedDate.value
            )
        )
    }

    fun deleteTimelineEntry(timeSlot: String) {
        viewModelScope.launch {
            timelineRepository.deleteEntryByTimeAndDate(timeSlot, _selectedDate.value)
        }
    }
}
