package com.hritik.lifetrackertimeline.presentation.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hritik.lifetrackertimeline.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onNavigateToTaskSelection: (String, String) -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val timeSlots = remember { generateTimeSlots() }
    val timelineItems by viewModel.timelineItems.collectAsState()
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    
    var showDatePicker by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            kotlinx.coroutines.delay(60000)
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDateStr)?.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Date(it)
                        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                        viewModel.setSelectedDate(formattedDate)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val displayDate = remember(selectedDateStr) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDateStr) ?: Date()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (selectedDateStr == today) null // Special case for localized "Today's Focus"
        else SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(date)
    }
    
    val todayLabel = stringResource(R.string.todays_focus)
    val actualDisplayDate = displayDate ?: todayLabel

    val headerDate = remember(selectedDateStr) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDateStr) ?: Date()
        SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(date).uppercase()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FE))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = headerDate,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF5C6BC0),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = actualDisplayDate,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = stringResource(R.string.change_date), tint = Color.Gray)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            items(timeSlots, key = { it }) { time ->
                val item = timelineItems[time]
                val nextSlot = remember(time) { getNextSlot(time) }
                val isCurrent = remember(currentTime, time, nextSlot) { isTimeBetween(currentTime, time, nextSlot) }
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .drawBehind {
                            val lineX = 67.dp.toPx()
                            drawLine(
                                color = Color(0xFFE0E0E0),
                                start = Offset(lineX, 0f),
                                end = Offset(lineX, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                ) {
                    Column {
                        if (item != null) {
                            TimelineItemRow(
                                item = item,
                                onClick = { onNavigateToTaskSelection(time, selectedDateStr) }
                            )
                        } else {
                            EmptyTimelineItemRow(
                                time = time,
                                onClick = { onNavigateToTaskSelection(time, selectedDateStr) }
                            )
                        }
                        
                        if (isCurrent && selectedDateStr == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) {
                            CurrentTimeIndicator(currentTime)
                        }
                    }
                }
            }
        }
    }
}

private fun isTimeBetween(target: String, start: String, end: String): Boolean {
    if (end == "00:00" && start == "23:30") {
        return target >= start || target < end
    }
    return target >= start && target < end
}

private fun getNextSlot(current: String): String {
    val parts = current.split(":")
    var hour = parts[0].toInt()
    var minute = parts[1].toInt() + 30
    if (minute >= 60) {
        hour += 1
        minute = 0
    }
    if (hour >= 24) hour = 0
    return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}

@Composable
fun TimelineItemRow(item: TimelineUiItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = item.timeSlot,
            fontSize = 13.sp,
            color = Color(0xFF9E9E9E),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(54.dp)
                .padding(top = 10.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(item.color))
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Card(
            modifier = Modifier.weight(1.0f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(Color(item.color))
                )
                
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        
                        Box(
                            modifier = Modifier
                                .background(Color(item.color).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = getIconByName(item.icon),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color(item.color)
                            )
                        }
                    }
                    
                    if (item.description.isNotEmpty()) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 16.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTimelineItemRow(time: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = time,
            fontSize = 13.sp,
            color = Color(0xFFBDBDBD),
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .width(54.dp)
                .padding(top = 10.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .padding(top = 14.dp)
                .size(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0))
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Box(
            modifier = Modifier
                .weight(1.0f)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.5f))
                .border(
                    width = 1.dp,
                    color = Color(0xFFEEEEEE),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFBDBDBD)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.select_task),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFBDBDBD),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CurrentTimeIndicator(time: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = time,
            fontSize = 13.sp,
            color = Color(0xFF0047BB),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.width(54.dp)
        )
        
        Spacer(modifier = Modifier.width(5.dp))

        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF0047BB))
        )
        
        Spacer(modifier = Modifier.width(5.dp))

        Canvas(modifier = Modifier.weight(1f).height(1.dp)) {
            drawLine(
                color = Color(0xFF0047BB),
                start = Offset(0f, 0.5f),
                end = Offset(size.width, 0.5f),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                strokeWidth = 1.2.dp.toPx()
            )
        }
    }
}

private fun generateTimeSlots(): List<String> {
    val slots = mutableListOf<String>()
    for (hour in 0..23) {
        slots.add(String.format(Locale.getDefault(), "%02d:00", hour))
        slots.add(String.format(Locale.getDefault(), "%02d:30", hour))
    }
    return slots
}
