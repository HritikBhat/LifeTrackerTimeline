package com.hritik.lifetrackertimeline.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Main : Screen("main")
    object Premium : Screen("premium")
    object AddEditTask : Screen("add_edit_task/{taskId}?taskName={taskName}&timeSlot={timeSlot}&date={date}") {
        fun createRoute(taskId: Int, taskName: String? = null, timeSlot: String? = null, date: String? = null): String {
            val builder = StringBuilder("add_edit_task/$taskId")
            var hasParam = false
            
            if (taskName != null) {
                builder.append("?taskName=$taskName")
                hasParam = true
            }
            
            if (timeSlot != null) {
                builder.append(if (hasParam) "&" else "?").append("timeSlot=$timeSlot")
                hasParam = true
            }
            
            if (date != null) {
                builder.append(if (hasParam) "&" else "?").append("date=$date")
            }
            
            return builder.toString()
        }
    }
    object TaskSelection : Screen("task_selection/{timeSlot}/{date}") {
        fun createRoute(timeSlot: String, date: String) = "task_selection/$timeSlot/$date"
    }
}

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Timeline : BottomBarScreen(
        route = "timeline",
        title = "Timeline",
        icon = Icons.Default.Timeline
    )

    object Tasks : BottomBarScreen(
        route = "task",
        title = "Tasks",
        icon = Icons.Default.Task
    )

    object Analytics : BottomBarScreen(
        route = "analytics",
        title = "Analytics",
        icon = Icons.Default.Analytics
    )

    object Calendar : BottomBarScreen(
        route = "calendar",
        title = "Calendar",
        icon = Icons.Default.CalendarMonth
    )

    object Profile : BottomBarScreen(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person
    )
}
