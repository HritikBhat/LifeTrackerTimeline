package com.hritik.lifetrackertimeline.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hritik.lifetrackertimeline.data.local.entity.TaskEntity
import com.hritik.lifetrackertimeline.data.repository.TaskRepository
import com.hritik.lifetrackertimeline.data.repository.TimelineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val timelineRepository: TimelineRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(Calendar.getInstance())
    val selectedDate: StateFlow<Calendar> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(Calendar.getInstance())
    val currentMonth: StateFlow<Calendar> = _currentMonth.asStateFlow()

    val allTimelineEntries = timelineRepository.getAllTimelineEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDayItems: StateFlow<List<TimelineUiItem>> = combine(
        _selectedDate,
        allTimelineEntries,
        tasks
    ) { date, entries, allTasks ->
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
        entries.filter { it.date == dateString }.mapNotNull { entry ->
            allTasks.find { it.id == entry.taskId }?.let { task ->
                TimelineUiItem(
                    timelineId = entry.id,
                    timeSlot = entry.timeSlot,
                    taskId = entry.taskId,
                    title = task.title,
                    description = entry.description,
                    color = task.color,
                    icon = task.icon,
                    isCompleted = entry.isCompleted
                )
            }
        }.sortedBy { it.timeSlot }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(calendar: Calendar) {
        _selectedDate.value = calendar.clone() as Calendar
    }

    fun nextMonth() {
        val next = _currentMonth.value.clone() as Calendar
        next.add(Calendar.MONTH, 1)
        _currentMonth.value = next
    }

    fun previousMonth() {
        val prev = _currentMonth.value.clone() as Calendar
        prev.add(Calendar.MONTH, -1)
        _currentMonth.value = prev
    }
}