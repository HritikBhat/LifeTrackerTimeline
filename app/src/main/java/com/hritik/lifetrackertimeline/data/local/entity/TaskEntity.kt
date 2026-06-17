package com.hritik.lifetrackertimeline.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val notes: String,
    val isUnproductive: Boolean = false,
    val color: Int, // Store as ARGB Int
    val icon: String,
    val isActive: Boolean = true
)