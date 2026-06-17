package com.hritik.lifetrackertimeline.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hritik.lifetrackertimeline.data.local.entity.TaskEntity
import com.hritik.lifetrackertimeline.data.local.entity.TimelineEntity
import com.hritik.lifetrackertimeline.data.repository.TaskRepository
import com.hritik.lifetrackertimeline.data.repository.TimelineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class AnalyticsState(
    val weeklyTrend: List<Pair<String, Int>> = emptyList(),
    val totalHoursThisWeek: String = "0h 0m",
    val dailyAvg: String = "0h",
    val peakProductivity: List<Pair<String, Int>> = emptyList(),
    val focusAllocation: List<FocusAllocationItem> = emptyList(),
    val monthlyHighlights: List<MonthlyHighlightItem> = emptyList()
)

data class FocusAllocationItem(
    val title: String,
    val hours: Double,
    val color: Int,
    val percentage: Float
)

data class MonthlyHighlightItem(
    val title: String,
    val hours: Double,
    val trend: List<Int>,
    val color: Int,
    val icon: String
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val timelineRepository: TimelineRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    val uiState: StateFlow<AnalyticsState> = combine(
        timelineRepository.getAllTimelineEntries(),
        taskRepository.getAllTasks()
    ) { entries, tasks ->
        calculateAnalytics(entries, tasks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsState())

    private fun calculateAnalytics(entries: List<TimelineEntity>, tasks: List<TaskEntity>): AnalyticsState {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        // Filter for last 7 days for weekly trend and total hours
        val last7Days = (0..6).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            sdf.format(cal.time)
        }.reversed()

        val weeklyEntries = entries.filter { it.date in last7Days }
        val productiveWeeklyEntries = weeklyEntries.filter { entry ->
            tasks.find { it.id == entry.taskId }?.isUnproductive == false
        }

        // Weekly Trend
        val trend = last7Days.map { date ->
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(sdf.parse(date)!!)
            val count = productiveWeeklyEntries.count { it.date == date }
            dayName to count
        }

        // Total Hours (Each entry is 30 mins = 0.5 hours)
        val totalMinutes = productiveWeeklyEntries.size * 30
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        val totalHoursStr = "${hours}h ${mins}m"

        // Daily Avg
        val dailyAvgHours = if (last7Days.isNotEmpty()) (productiveWeeklyEntries.size * 0.5) / 7 else 0.0
        val dailyAvgStr = String.format(Locale.getDefault(), "%.1fh", dailyAvgHours)

        // Peak Productivity (by time of day)
        val timeGroups = mapOf(
            "Morning" to (6..11),
            "Noon" to (12..14),
            "Afternoon" to (15..17),
            "Evening" to (18..23)
        )
        val peakProd = timeGroups.map { (label, range) ->
            val count = productiveWeeklyEntries.count { entry ->
                val hour = entry.timeSlot.split(":")[0].toInt()
                hour in range
            }
            label to count
        }

        // Focus Allocation
        val taskGroups = productiveWeeklyEntries.groupBy { it.taskId }
        val totalProductiveEntries = productiveWeeklyEntries.size.coerceAtLeast(1)
        val focusItems = taskGroups.mapNotNull { (taskId, taskEntries) ->
            tasks.find { it.id == taskId }?.let { task ->
                FocusAllocationItem(
                    title = task.title,
                    hours = taskEntries.size * 0.5,
                    color = task.color,
                    percentage = taskEntries.size.toFloat() / totalProductiveEntries
                )
            }
        }.sortedByDescending { it.hours }.take(4)

        // Monthly Highlights (Last 30 days)
        val cal30 = Calendar.getInstance()
        cal30.add(Calendar.DAY_OF_YEAR, -30)
        val startDate30 = sdf.format(cal30.time)
        val monthlyEntries = entries.filter { it.date >= startDate30 }
        
        val monthlyHighlights = monthlyEntries.groupBy { it.taskId }.mapNotNull { (taskId, taskEntries) ->
            tasks.find { it.id == taskId }?.let { task ->
                // Simple trend: count per week for last 4 weeks
                val trendData = (0..3).map { week ->
                    val wStart = Calendar.getInstance()
                    wStart.add(Calendar.DAY_OF_YEAR, -(week + 1) * 7)
                    val wEnd = Calendar.getInstance()
                    wEnd.add(Calendar.DAY_OF_YEAR, -week * 7)
                    taskEntries.count { 
                        val d = sdf.parse(it.date)!!
                        d.after(wStart.time) && d.before(wEnd.time)
                    }
                }.reversed()

                MonthlyHighlightItem(
                    title = task.title,
                    hours = taskEntries.size * 0.5,
                    trend = trendData,
                    color = task.color,
                    icon = task.icon
                )
            }
        }.sortedByDescending { it.hours }.take(3)

        return AnalyticsState(
            weeklyTrend = trend,
            totalHoursThisWeek = totalHoursStr,
            dailyAvg = dailyAvgStr,
            peakProductivity = peakProd,
            focusAllocation = focusItems,
            monthlyHighlights = monthlyHighlights
        )
    }
}