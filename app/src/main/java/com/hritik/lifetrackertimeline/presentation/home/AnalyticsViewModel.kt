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

enum class AnalyticsPeriod {
    WEEK, MONTH
}

data class AnalyticsState(
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.WEEK,
    val trendData: List<Pair<String, Int>> = emptyList(),
    val totalHours: String = "0h 0m",
    val totalHoursDelta: String = "0%",
    val dailyAvg: String = "0h",
    val dailyAvgProgress: Float = 0f,
    val peakProductivity: List<Pair<String, Int>> = emptyList(),
    val peakUnproductivity: List<Pair<String, Int>> = emptyList(),
    val peakTimeRange: String = "--",
    val focusAllocation: List<FocusAllocationItem> = emptyList(),
    val monthlyHighlights: List<MonthlyHighlightItem> = emptyList(),
    val unproductiveActivities: List<UnproductiveItem> = emptyList()
)

data class FocusAllocationItem(
    val title: String,
    val hours: Double,
    val color: Int,
    val percentage: Float
)

data class UnproductiveItem(
    val title: String,
    val hours: Double,
    val color: Int
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

    private val _selectedPeriod = MutableStateFlow(AnalyticsPeriod.WEEK)
    val selectedPeriod: StateFlow<AnalyticsPeriod> = _selectedPeriod.asStateFlow()

    val uiState: StateFlow<AnalyticsState> = combine(
        timelineRepository.getAllTimelineEntries(),
        taskRepository.getAllTasks(),
        _selectedPeriod
    ) { entries, tasks, period ->
        calculateAnalytics(entries, tasks, period)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsState())

    fun setPeriod(period: AnalyticsPeriod) {
        _selectedPeriod.value = period
    }

    private fun calculateAnalytics(
        entries: List<TimelineEntity>,
        tasks: List<TaskEntity>,
        period: AnalyticsPeriod
    ): AnalyticsState {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val daysCount = if (period == AnalyticsPeriod.WEEK) 7 else 30
        val dateRange = (0 until daysCount).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            sdf.format(cal.time)
        }.reversed()

        val filteredEntries = entries.filter { it.date in dateRange }
        val productiveEntries = filteredEntries.filter { entry ->
            tasks.find { it.id == entry.taskId }?.isUnproductive == false
        }
        val unproductiveEntries = filteredEntries.filter { entry ->
            tasks.find { it.id == entry.taskId }?.isUnproductive == true
        }

        // Trend Data
        val trend = dateRange.map { date ->
            val cal = Calendar.getInstance()
            cal.time = sdf.parse(date)!!
            val label = if (period == AnalyticsPeriod.WEEK) {
                SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
            } else {
                SimpleDateFormat("d", Locale.getDefault()).format(cal.time)
            }
            val count = productiveEntries.count { it.date == date }
            label to count
        }

        // Total Hours
        val currentMinutes = productiveEntries.size * 30
        val currentHours = currentMinutes / 60
        val currentMins = currentMinutes % 60
        val totalHoursStr = "${currentHours}h ${currentMins}m"

        // Calculate Delta (vs previous period)
        val prevDateRange = (daysCount until 2 * daysCount).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            sdf.format(cal.time)
        }
        val prevProductiveEntries = entries.filter { it.date in prevDateRange && tasks.find { t -> t.id == it.taskId }?.isUnproductive == false }
        val prevMinutes = prevProductiveEntries.size * 30
        val delta = if (prevMinutes > 0) {
            val d = ((currentMinutes - prevMinutes).toFloat() / prevMinutes * 100).toInt()
            (if (d >= 0) "+" else "") + "$d%"
        } else if (currentMinutes > 0) "+100%" else "0%"

        // Daily Avg
        val dailyAvgHours = if (dateRange.isNotEmpty()) (productiveEntries.size * 0.5) / daysCount else 0.0
        val dailyAvgStr = String.format(Locale.getDefault(), "%.1fh", dailyAvgHours)
        val dailyAvgProgress = (dailyAvgHours / 8.0).coerceIn(0.0, 1.0).toFloat()

        // Peak Productivity
        val timeGroups = mapOf(
            "6 AM - 11 AM" to (6..11),
            "12 PM - 2 PM" to (12..14),
            "3 PM - 5 PM" to (15..17),
            "6 PM - 11 PM" to (18..23)
        )
        val peakProdChart = listOf("Morning", "Noon", "Afternoon", "Evening").zip(
            timeGroups.values.map { range ->
                productiveEntries.count { entry ->
                    val hour = entry.timeSlot.split(":")[0].toInt()
                    hour in range
                }
            }
        )
        
        // Peak Unproductivity
        val peakUnprodChart = listOf("Morning", "Noon", "Afternoon", "Evening").zip(
            timeGroups.values.map { range ->
                unproductiveEntries.count { entry ->
                    val hour = entry.timeSlot.split(":")[0].toInt()
                    hour in range
                }
            }
        )

        val maxPeak = peakProdChart.maxByOrNull { it.second }
        val peakTimeRangeLabel = if (maxPeak != null && maxPeak.second > 0) {
            timeGroups.keys.elementAt(listOf("Morning", "Noon", "Afternoon", "Evening").indexOf(maxPeak.first))
        } else "--"

        // Focus Allocation
        val taskGroups = productiveEntries.groupBy { it.taskId }
        val totalProductiveEntries = productiveEntries.size.coerceAtLeast(1)
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

        // Unproductive Activities
        val unproductiveGroups = unproductiveEntries.groupBy { it.taskId }
        val unproductiveItems = unproductiveGroups.mapNotNull { (taskId, taskEntries) ->
            tasks.find { it.id == taskId }?.let { task ->
                UnproductiveItem(
                    title = task.title,
                    hours = taskEntries.size * 0.5,
                    color = task.color
                )
            }
        }.sortedByDescending { it.hours }

        // Monthly Highlights
        val cal30 = Calendar.getInstance()
        cal30.add(Calendar.DAY_OF_YEAR, -30)
        val startDate30 = sdf.format(cal30.time)
        val highlightsEntries = entries.filter { it.date >= startDate30 }
        
        val monthlyHighlights = highlightsEntries.filter { entry ->
            tasks.find { it.id == entry.taskId }?.isUnproductive == false
        }.groupBy { it.taskId }.mapNotNull { (taskId, taskEntries) ->
            tasks.find { it.id == taskId }?.let { task ->
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
            selectedPeriod = period,
            trendData = trend,
            totalHours = totalHoursStr,
            totalHoursDelta = delta,
            dailyAvg = dailyAvgStr,
            dailyAvgProgress = dailyAvgProgress,
            peakProductivity = peakProdChart.map { it.first.take(3) to it.second }, // Short labels for chart
            peakUnproductivity = peakUnprodChart.map { it.first.take(3) to it.second },
            peakTimeRange = peakTimeRangeLabel,
            focusAllocation = focusItems,
            monthlyHighlights = monthlyHighlights,
            unproductiveActivities = unproductiveItems
        )
    }
}