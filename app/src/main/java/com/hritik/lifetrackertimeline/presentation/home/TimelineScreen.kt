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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.hritik.lifetrackertimeline.data.local.entity.TaskEntity
import com.hritik.lifetrackertimeline.presentation.home.TimelineUiItem
import com.hritik.lifetrackertimeline.presentation.home.getIconByName
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val timeSlots = remember { generateTimeSlots() }
    val timelineItems by viewModel.timelineItems.collectAsState()
    val availableTasks by viewModel.availableTasks.collectAsState()
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTimeSlot by remember { mutableStateOf("") }
    var itemToEdit by remember { mutableStateOf<TimelineUiItem?>(null) }
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
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEditDialog) {
        EditTimelineBlockDialog(
            timeSlot = selectedTimeSlot,
            initialItem = itemToEdit,
            availableTasks = availableTasks,
            onDismiss = { showEditDialog = false },
            onSave = { taskId ->
                viewModel.upsertTimelineEntry(selectedTimeSlot, taskId)
                showEditDialog = false
            },
            onDelete = {
                viewModel.deleteTimelineEntry(selectedTimeSlot)
                showEditDialog = false
            }
        )
    }

    val displayDate = remember(selectedDateStr) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDateStr) ?: Date()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (selectedDateStr == today) "Today's Focus"
        else SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(date)
    }

    val headerDate = remember(selectedDateStr) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDateStr) ?: Date()
        SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(date).uppercase()
    }

    // Removed outer Scaffold to prevent double padding and redundant layout logic
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FE))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // Reduced from 100dp
        ) {
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    Spacer(modifier = Modifier.height(16.dp)) // Reduced top space
                    
                    Text(
                        text = headerDate,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF5C6BC0),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayDate,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Change Date", tint = Color.Gray)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp)) // Reduced space before items
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
                                onClick = {
                                    selectedTimeSlot = time
                                    itemToEdit = item
                                    showEditDialog = true
                                }
                            )
                        } else {
                            EmptyTimelineItemRow(
                                time = time,
                                onClick = {
                                    selectedTimeSlot = time
                                    itemToEdit = null
                                    showEditDialog = true
                                }
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
fun EditTimelineBlockDialog(
    timeSlot: String,
    initialItem: TimelineUiItem?,
    availableTasks: List<TaskEntity>,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit,
    onDelete: () -> Unit
) {
    var selectedTask by remember { 
        mutableStateOf(
            availableTasks.find { it.id == initialItem?.taskId } ?: availableTasks.firstOrNull()
        )
    }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = if (initialItem == null) "Add Timeline Block" else "Edit Timeline Block",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (selectedTask != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF8F9FE))
                            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                    ) {
                        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(Color(selectedTask!!.color))
                            )
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$timeSlot - ${getEndTime(timeSlot)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = selectedTask!!.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = selectedTask!!.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text("No tasks available. Please add tasks in the Task List first.", color = Color.Red)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "SELECT TASK",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box {
                    OutlinedCard(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFFBFBFF)),
                        border = CardDefaults.outlinedCardBorder().copy(width = 0.5.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedTask?.title ?: "Select a task",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF1A1A1A)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                        }
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.7f).background(Color.White)
                    ) {
                        availableTasks.forEach { task ->
                            DropdownMenuItem(
                                text = { Text(task.title) },
                                onClick = {
                                    selectedTask = task
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (initialItem != null) {
                        TextButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove", color = Color.Red)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color(0xFF0047BB), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                selectedTask?.let { onSave(it.id) }
                            },
                            enabled = selectedTask != null,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0047BB)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineItemRow(item: TimelineUiItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp) // Reduced padding
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
            .padding(bottom = 16.dp) // Reduced padding
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
                    text = "Select Task",
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
            .padding(bottom = 16.dp) // Aligned with items
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

private fun getEndTime(startTime: String): String {
    val parts = startTime.split(":")
    var hour = parts[0].toInt()
    var minute = parts[1].toInt() + 30
    if (minute >= 60) {
        hour += 1
        minute = 0
    }
    if (hour >= 24) hour = 0
    return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}
