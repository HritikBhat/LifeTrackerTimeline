package com.hritik.lifetrackertimeline.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.hritik.lifetrackertimeline.data.local.entity.TaskEntity
import com.hritik.lifetrackertimeline.presentation.components.AdBanner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTaskScreen(
    taskId: Int,
    taskName: String? = null,
    timeSlot: String? = null,
    date: String? = null,
    navController: NavController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf(taskName ?: "") }
    var notes by remember { mutableStateOf("") }
    var isUnproductive by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(colors[0]) }
    var selectedIcon by remember { mutableStateOf(icons[0].name) }

    LaunchedEffect(taskId) {
        if (taskId != -1) {
            viewModel.getTaskById(taskId)?.let { task ->
                title = task.title
                notes = task.notes
                isUnproductive = task.isUnproductive
                selectedColor = Color(task.color)
                selectedIcon = task.icon
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == -1) "Add Task" else "Edit Task", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            scope.launch {
                                val task = TaskEntity(
                                    id = if (taskId == -1) 0 else taskId,
                                    title = title,
                                    notes = notes,
                                    isUnproductive = isUnproductive,
                                    color = selectedColor.toArgb(),
                                    icon = selectedIcon
                                )
                                
                                if (taskId == -1) {
                                    val newTaskId = viewModel.insertTask(task)
                                    if (timeSlot != null && date != null) {
                                        viewModel.upsertTimelineEntry(timeSlot, newTaskId, date)
                                        // Set a flag to signal the parent to pop TaskSelectionScreen
                                        navController.previousBackStackEntry?.savedStateHandle?.set("task_created", true)
                                    }
                                } else {
                                    viewModel.updateTask(task)
                                }
                                navController.popBackStack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0047AB)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FE))
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FE))
                    .navigationBarsPadding()
            ) {
                AdBanner()
            }
        },
        containerColor = Color(0xFFF8F9FE)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SectionContainer(title = "Task Details") {
                Text("TASK NAME", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("NOTES", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isUnproductive,
                        onCheckedChange = { isUnproductive = it }
                    )
                    Text("Mark as Unproductive", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionContainer(title = "IDENTIFIER COLOR") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(colors) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == color) 2.dp else 0.dp,
                                    color = if (selectedColor == color) Color.Black else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionContainer(title = "TASK ICON") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    icons.forEach { iconData ->
                        val isSelected = selectedIcon == iconData.name
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFFDDE3FF) else Color.Transparent)
                                .clickable { selectedIcon = iconData.name },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconData.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF0047AB) else Color.DarkGray
                            )
                        }
                    }
                }
            }

            if (taskId != -1) {
                Spacer(modifier = Modifier.height(32.dp))
                TextButton(
                    onClick = {
                        val task = TaskEntity(
                            id = taskId,
                            title = title,
                            notes = notes,
                            isUnproductive = isUnproductive,
                            color = selectedColor.toArgb(),
                            icon = selectedIcon
                        )
                        viewModel.deleteTask(task)
                        navController.popBackStack()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Task", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun SectionContainer(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0047AB)
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

private val colors = listOf(
    Color(0xFF0047AB), Color(0xFF7E3FF2), Color(0xFFC62828), Color(0xFF00BFA5), Color(0xFFFFA000),
    Color(0xFF5C6BC0), Color(0xFFEC407A), Color(0xFF00BCD4), Color(0xFF757575), Color(0xFF263238)
)

private val icons = listOf(
    IconData("Work", Icons.Default.Work),
    IconData("Fitness", Icons.Default.FitnessCenter),
    IconData("Food", Icons.Default.Restaurant),
    IconData("Study", Icons.Default.MenuBook),
    IconData("Code", Icons.Default.Code),
    IconData("Art", Icons.Default.Palette),
    IconData("Music", Icons.Default.MusicNote),
    IconData("Sleep", Icons.Default.Bedtime),
    IconData("Shop", Icons.Default.ShoppingCart),
    IconData("Travel", Icons.Default.AirplanemodeActive)
)

data class IconData(val name: String, val icon: ImageVector)
