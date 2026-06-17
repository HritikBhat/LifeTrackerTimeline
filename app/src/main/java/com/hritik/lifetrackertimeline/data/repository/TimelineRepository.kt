package com.hritik.lifetrackertimeline.data.repository

import com.hritik.lifetrackertimeline.data.local.dao.TimelineDao
import com.hritik.lifetrackertimeline.data.local.entity.TimelineEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimelineRepository @Inject constructor(
    private val timelineDao: TimelineDao
) {
    fun getTimelineByDate(date: String): Flow<List<TimelineEntity>> = 
        timelineDao.getTimelineByDate(date)

    fun getAllTimelineEntries(): Flow<List<TimelineEntity>> =
        timelineDao.getAllTimelineEntries()

    suspend fun insertTimelineEntry(entry: TimelineEntity) = 
        timelineDao.insertTimelineEntry(entry)

    suspend fun deleteTimelineEntry(entry: TimelineEntity) = 
        timelineDao.deleteTimelineEntry(entry)

    suspend fun deleteEntryByTimeAndDate(timeSlot: String, date: String) =
        timelineDao.deleteEntryByTimeAndDate(timeSlot, date)
}