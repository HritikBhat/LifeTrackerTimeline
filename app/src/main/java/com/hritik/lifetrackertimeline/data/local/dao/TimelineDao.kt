package com.hritik.lifetrackertimeline.data.local.dao

import androidx.room.*
import com.hritik.lifetrackertimeline.data.local.entity.TimelineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineDao {
    @Query("SELECT * FROM timeline WHERE date = :date")
    fun getTimelineByDate(date: String): Flow<List<TimelineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEntry(entry: TimelineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEntries(entries: List<TimelineEntity>)

    @Delete
    suspend fun deleteTimelineEntry(entry: TimelineEntity)
    
    @Query("DELETE FROM timeline WHERE timeSlot = :timeSlot AND date = :date")
    suspend fun deleteEntryByTimeAndDate(timeSlot: String, date: String)

    @Query("SELECT * FROM timeline")
    fun getAllTimelineEntries(): Flow<List<TimelineEntity>>

    @Query("SELECT * FROM timeline")
    suspend fun getAllTimelineEntriesList(): List<TimelineEntity>

    @Query("DELETE FROM timeline")
    suspend fun clearTimeline()
}