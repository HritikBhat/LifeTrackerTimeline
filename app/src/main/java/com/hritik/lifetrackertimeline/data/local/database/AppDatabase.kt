package com.hritik.lifetrackertimeline.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hritik.lifetrackertimeline.data.local.dao.UserDao
import com.hritik.lifetrackertimeline.data.local.dao.TaskDao
import com.hritik.lifetrackertimeline.data.local.dao.TimelineDao
import com.hritik.lifetrackertimeline.data.local.entity.UserEntity
import com.hritik.lifetrackertimeline.data.local.entity.TaskEntity
import com.hritik.lifetrackertimeline.data.local.entity.TimelineEntity

@Database(entities = [UserEntity::class, TaskEntity::class, TimelineEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun timelineDao(): TimelineDao
}