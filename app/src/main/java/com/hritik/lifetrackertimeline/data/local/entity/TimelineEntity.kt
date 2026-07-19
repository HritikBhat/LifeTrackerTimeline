package com.hritik.lifetrackertimeline.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timeline")
data class TimelineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timeSlot: String, // e.g., "08:00"
    val taskId: Int,
    val date: String, // e.g., "yyyy-MM-dd"
    val description: String = "",
    val isCompleted: Boolean = false
)