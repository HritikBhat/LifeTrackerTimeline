package com.hritik.lifetrackertimeline.presentation.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getIconByName(name: String): ImageVector {
    return when (name) {
        "Work" -> Icons.Default.Work
        "Fitness" -> Icons.Default.FitnessCenter
        "Food" -> Icons.Default.Restaurant
        "Study" -> Icons.Default.MenuBook
        "Code" -> Icons.Default.Code
        "Art" -> Icons.Default.Palette
        "Music" -> Icons.Default.MusicNote
        "Sleep" -> Icons.Default.Bedtime
        "Shop" -> Icons.Default.ShoppingCart
        "Travel" -> Icons.Default.AirplanemodeActive
        else -> Icons.Default.Task
    }
}