package com.hritik.lifetrackertimeline.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
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
import com.hritik.lifetrackertimeline.data.local.entity.TaskEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskSelectionScreen(
    timeSlot: String,
    date: String,
    onNavigateBack: () -> Unit,
    onTaskSelected: (Int) -> Unit,
    onAddNewTask: (String) -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    val availableTasks by viewModel.availableTasks.collectAsState()
    
    val filteredTasks = remember(searchQuery, availableTasks) {
        if (searchQuery.isBlank()) {
            availableTasks
        } else {
            availableTasks.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    val displayDate = remember(date) {
        val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date) ?: Date()
        SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(parsedDate)
    }

    val endTime = remember(timeSlot) {
        getEndTime(timeSlot)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FE))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.select_task_header),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF5C6BC0),
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$timeSlot - $endTime",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.search_tasks)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFEEEEEE)
                        ),
                        singleLine = true
                    )
                    
                    if (searchQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        FloatingActionButton(
                            onClick = { onAddNewTask(searchQuery) },
                            containerColor = Color(0xFF0047AB),
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_task))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.previous_tasks),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = stringResource(R.string.total_suffix, filteredTasks.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = Color(0xFFF8F9FE)
    ) { paddingValues ->
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                EmptyTaskState()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(filteredTasks) { task ->
                    TaskSelectionItem(
                        task = task,
                        onClick = {
                            scope.launch {
                                viewModel.upsertTimelineEntry(timeSlot, task.id, date)
                                onTaskSelected(task.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyTaskState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFEDE7F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF5C6BC0)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.no_tasks_found),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.no_tasks_found_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun TaskSelectionItem(task: TaskEntity, onClick: () -> Unit) {
    val lastUsedText = when {
        task.lastSelectedAt == 0L -> stringResource(R.string.never_used)
        else -> {
            val now = System.currentTimeMillis()
            val diff = now - task.lastSelectedAt
            val days = diff / (24 * 60 * 60 * 1000)
            
            when {
                diff < 60 * 1000 -> stringResource(R.string.just_now)
                diff < 60 * 60 * 1000 -> stringResource(R.string.minutes_ago, diff / (60 * 1000))
                diff < 24 * 60 * 60 * 1000 -> stringResource(R.string.hours_ago, diff / (60 * 60 * 1000))
                days == 1L -> stringResource(R.string.yesterday)
                days < 7 -> stringResource(R.string.days_ago, days)
                else -> {
                    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                    sdf.format(Date(task.lastSelectedAt))
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Color(task.color))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = if (task.lastSelectedAt == 0L) stringResource(R.string.never_used) else stringResource(R.string.last_used_prefix, lastUsedText),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(16.dp).padding(end = 16.dp)
            )
        }
    }
}

private fun getEndTime(startTime: String): String {
    val parts = startTime.split(":")
    if (parts.size < 2) return ""
    var hour = parts[0].toInt()
    var minute = parts[1].toInt() + 30
    if (minute >= 60) {
        hour += 1
        minute = 0
    }
    if (hour >= 24) hour = 0
    return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}
