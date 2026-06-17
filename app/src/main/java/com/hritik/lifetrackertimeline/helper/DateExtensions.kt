package com.hritik.lifetrackertimeline.helper

import java.time.LocalDate
import java.time.ZoneId

object DateExtensions {

    fun startOfToday(): Long {
        return LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun endOfToday(): Long {
        return LocalDate.now()
            .plusDays(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli() - 1
    }

    fun isToday(timestamp: Long?): Boolean {
        if (timestamp == null) return false

        return timestamp in startOfToday()..endOfToday()
    }
}