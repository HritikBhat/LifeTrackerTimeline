package com.hritik.lifetrackertimeline.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hritik.lifetrackertimeline.R
import com.hritik.lifetrackertimeline.data.local.entity.TimelineEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val allEntries by viewModel.allTimelineEntries.collectAsState()
    val dayItems by viewModel.selectedDayItems.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF8F9FE)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                CalendarHeader(
                    currentMonth = currentMonth,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
            }

            item {
                ActivityDensityLegend()
            }

            item {
                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    allEntries = allEntries,
                    onDateSelected = { viewModel.selectDate(it) }
                )
            }

            item {
                DayDetailsHeader(selectedDate)
            }

            if (dayItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_activities_logged),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(dayItems) { item ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        TimelineItemRow(item = item, onClick = { /* TODO */ })
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO: Log Activity */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8EAF6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF5C6BC0))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.log_new_activity), color = Color(0xFF5C6BC0), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarHeader(
    currentMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthYearFormat.format(currentMonth.time),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Row {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8EAF6))
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.previous))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8EAF6))
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.next))
                }
            }
        }
        
        Text(
            text = stringResource(R.string.efficiency_message),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ActivityDensityLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.activity_density),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(text = stringResource(R.string.less), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        DensityBox(Color(0xFFE8EAF6))
        DensityBox(Color(0xFFC5CAE9))
        DensityBox(Color(0xFF9FA8DA))
        DensityBox(Color(0xFF3F51B5))
        DensityBox(Color(0xFF1A237E))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = stringResource(R.string.more), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun DensityBox(color: Color) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .size(12.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}

@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    selectedDate: Calendar,
    allEntries: List<TimelineEntity>,
    onDateSelected: (Calendar) -> Unit
) {
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = currentMonth.clone() as Calendar
    firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
    val startDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday

    val days = mutableListOf<Calendar?>()
    
    // Previous month padding
    val prevMonth = currentMonth.clone() as Calendar
    prevMonth.add(Calendar.MONTH, -1)
    val daysInPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (i in 0 until startDayOfWeek) {
        val cal = prevMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, daysInPrevMonth - startDayOfWeek + i + 1)
        days.add(cal)
    }

    // Current month days
    for (i in 1..daysInMonth) {
        val cal = currentMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, i)
        days.add(cal)
    }

    // Next month padding
    val totalCells = if (days.size > 35) 42 else 35
    val nextMonthPadding = totalCells - days.size
    val nextMonth = currentMonth.clone() as Calendar
    nextMonth.add(Calendar.MONTH, 1)
    for (i in 1..nextMonthPadding) {
        val cal = nextMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, i)
        days.add(cal)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Weekdays Header
            Row(modifier = Modifier.fillMaxWidth()) {
                val weekDays = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Days Grid
            days.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        if (day != null) {
                            val isCurrentMonth = day.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)
                            val isSelected = isSameDay(day, selectedDate)
                            val isToday = isSameDay(day, Calendar.getInstance())
                            
                            val entriesCount = allEntries.count { 
                                it.date == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(day.time) 
                            }
                            
                            DayCell(
                                day = day.get(Calendar.DAY_OF_MONTH),
                                isCurrentMonth = isCurrentMonth,
                                isSelected = isSelected,
                                isToday = isToday,
                                entriesCount = entriesCount,
                                onClick = { onDateSelected(day) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    entriesCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected && isToday -> Color.White
        isSelected -> Color.White
        entriesCount >= 8 -> Color(0xFF1A237E)
        entriesCount >= 6 -> Color(0xFF3F51B5)
        entriesCount >= 4 -> Color(0xFF9FA8DA)
        entriesCount >= 2 -> Color(0xFFC5CAE9)
        entriesCount >= 1 -> Color(0xFFE8EAF6)
        else -> Color.Transparent
    }
    
    val contentColor = when {
        isSelected -> Color(0xFF3F51B5)
        entriesCount >= 4 -> Color.White
        isCurrentMonth -> Color.Black
        else -> Color.LightGray
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (isSelected) Modifier.border(2.dp, Color(0xFF3F51B5), RoundedCornerShape(8.dp)) else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isToday && isSelected) {
                Text(
                    text = stringResource(R.string.today),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3F51B5)
                )
            }
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
            if (entriesCount > 0) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (entriesCount >= 4) Color.White else Color(0xFF3F51B5))
                )
            }
        }
    }
}

@Composable
fun DayDetailsHeader(selectedDate: Calendar) {
    val dayFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.day_details),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Surface(
            color = Color(0xFF3F51B5),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = dayFormat.format(selectedDate.time).uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
    
    // Summary Cards
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        DetailRow(stringResource(R.string.productivity_score), "84/100", Color(0xFF3F51B5))
        Spacer(modifier = Modifier.height(8.dp))
        DetailRow(stringResource(R.string.deep_work), stringResource(R.string.hours_suffix, "5.2"), Color(0xFF673AB7))
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color) {
    Surface(
        color = Color(0xFFF0F2FF),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            Text(text = value, color = valueColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
