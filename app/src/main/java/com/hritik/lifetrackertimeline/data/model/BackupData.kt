package com.hritik.lifetrackertimeline.data.model

import com.hritik.lifetrackertimeline.data.local.entity.TaskEntity
import com.hritik.lifetrackertimeline.data.local.entity.TimelineEntity
import com.hritik.lifetrackertimeline.data.local.entity.UserEntity

data class BackupData(
    val version: Int,
    val users: List<UserEntity>,
    val tasks: List<TaskEntity>,
    val timeline: List<TimelineEntity>
)
