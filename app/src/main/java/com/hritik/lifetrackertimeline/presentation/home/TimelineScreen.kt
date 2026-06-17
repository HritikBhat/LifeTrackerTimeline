package com.hritik.lifetrackertimeline.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TimelineScreen() {
    val timeSlots = remember { generateTimeSlots() }
    val itemsState = remember { mutableStateListOf(*initialTimelineItems.toTypedArray()) }
    
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTimeSlot by remember { mutableStateOf("") }
    var itemToEdit by remember { mutableStateOf<TimelineItem?>(null) }

    // Dynamic current time
    var currentTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    
    // Update time every minute
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            kotlinx.coroutines.delay(60000)
        }
    }

    if (showEditDialog) {
        EditTimelineBlockDialog(
            timeSlot = selectedTimeSlot,
            initialItem = itemToEdit,
            onDismiss = { showEditDialog = false },
            onSave = { newItem ->
                itemsState.removeAll { it.time == selectedTimeSlot }
                itemsState.add(newItem)
                showEditDialog = false
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FE)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                // Header Section
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(Date()).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF5C6BC0),
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Today's Focus",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            items(timeSlots) { time ->
                val item = itemsState.find { it.time == time }
                val nextSlot = getNextSlot(time)
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .drawBehind {
                            // Continuous vertical line segment
                            // X position: 54 (time) + 8 (spacer) + 5 (center of dots) = 67dp
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
                        
                        // Show current time indicator if it falls between this slot and the next
                        if (isTimeBetween(currentTime, time, nextSlot)) {
                            CurrentTimeIndicator(currentTime)
                        }
                    }
                }
            }
        }
    }
}

private fun isTimeBetween(target: String, start: String, end: String): Boolean {
    // Basic string comparison works for HH:mm in 24h format
    // Special case for the very last slot (23:30 to 00:00)
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
    initialItem: TimelineItem?,
    onDismiss: () -> Unit,
    onSave: (TimelineItem) -> Unit
) {
    val templates = remember { getTaskTemplates() }
    var selectedTemplate by remember { 
        mutableStateOf(
            templates.find { it.title == initialItem?.title } ?: templates.first()
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
                
                // Preview Card
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
                                .background(selectedTemplate.color)
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
                                CategoryTag(category = selectedTemplate.category, color = selectedTemplate.color)
                            }
                            Text(
                                text = selectedTemplate.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = selectedTemplate.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
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
                
                // Dropdown Selector
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
                                text = selectedTemplate.title,
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
                        templates.forEach { template ->
                            DropdownMenuItem(
                                text = { Text(template.title) },
                                onClick = {
                                    selectedTemplate = template
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF0047BB), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            onSave(
                                TimelineItem(
                                    time = timeSlot,
                                    title = selectedTemplate.title,
                                    description = selectedTemplate.description,
                                    category = selectedTemplate.category,
                                    color = selectedTemplate.color,
                                    isCompleted = initialItem?.isCompleted ?: false
                                )
                            )
                        },
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

@Composable
fun TimelineItemRow(item: TimelineItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .clickable { onClick() }
    ) {
        // Time Column
        Text(
            text = item.time,
            fontSize = 13.sp,
            color = Color(0xFF9E9E9E),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(54.dp)
                .padding(top = 10.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        // Indicator Circle (Filled)
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(item.color)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Task Card
        Card(
            modifier = Modifier.weight(1.0f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                // Vertical accent line
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(item.color)
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
                        
                        CategoryTag(category = item.category, color = item.color)
                    }
                    
                    if (item.location != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    if (item.description.isNotEmpty()) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 16.sp
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
            .padding(bottom = 20.dp)
            .clickable { onClick() }
    ) {
        // Time Column
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

        // Small empty dot indicator - Aligned on the vertical line
        Box(
            modifier = Modifier
                .padding(top = 14.dp)
                .size(10.dp), // Container same size as filled dot for alignment
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
        
        // Placeholder Card
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
fun CategoryTag(category: String, color: Color) {
    val isMeeting = category == "MEETING"
    val isDeepWork = category == "DEEP WORK"
    
    val backgroundColor = if (isMeeting) Color(0xFF1E88E5) else if (isDeepWork) color.copy(alpha = 0.12f) else color.copy(alpha = 0.15f)
    val textColor = if (isMeeting) Color.White else color
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

@Composable
fun CurrentTimeIndicator(time: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        Text(
            text = time,
            fontSize = 13.sp,
            color = Color(0xFF0047BB),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.width(54.dp)
        )
        
        Spacer(modifier = Modifier.width(5.dp))

        // Large Blue indicator for current time
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

data class TimelineItem(
    val time: String,
    val title: String,
    val description: String,
    val category: String,
    val color: Color,
    val location: String? = null,
    val isCompleted: Boolean = false
)

data class TaskTemplate(
    val title: String,
    val description: String,
    val category: String,
    val color: Color
)

fun getTaskTemplates() = listOf(
    TaskTemplate("Morning Deep Work", "Focus session on project architecture.", "DEEP WORK", Color(0xFF7E57C2)),
    TaskTemplate("Daily Standup", "Google Meet sync with the team.", "MEETING", Color(0xFF1E88E5)),
    TaskTemplate("Inbox Zero Sprint", "Clear urgent emails and slack notifications.", "URGENT", Color(0xFFEF5350)),
    TaskTemplate("System Maintenance", "Update documentation and logs.", "ADMIN", Color(0xFF9E9E9E)),
    TaskTemplate("Coffee Break", "Short 15m walk to recharge.", "RELAX", Color(0xFF7E57C2)),
    TaskTemplate("Quick Call", "Sync with lead about current blockers.", "CALL", Color(0xFF7E57C2)),
    TaskTemplate("Gym Session", "Health and fitness routine.", "HEALTH", Color(0xFF66BB6A))
)

val initialTimelineItems = listOf(
    TimelineItem(
        time = "08:00", 
        title = "Morning Deep Work", 
        description = "Focus session on project architecture.", 
        category = "DEEP WORK", 
        color = Color(0xFF7E57C2),
        isCompleted = true
    ),
    TimelineItem(
        time = "09:00", 
        title = "Daily Standup", 
        description = "Google Meet sync with the team.", 
        location = "Google Meet",
        category = "MEETING", 
        color = Color(0xFF1E88E5),
        isCompleted = true
    )
)
