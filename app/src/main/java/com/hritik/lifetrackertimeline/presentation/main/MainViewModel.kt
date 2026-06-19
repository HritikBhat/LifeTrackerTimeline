package com.hritik.lifetrackertimeline.presentation.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hritik.lifetrackertimeline.data.local.DataStoreManager
import com.hritik.lifetrackertimeline.data.repository.BillingRepository
import com.hritik.lifetrackertimeline.data.repository.UserRepository
import com.hritik.lifetrackertimeline.helper.PremiumManager
import com.hritik.lifetrackertimeline.worker.NotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val userRepository: UserRepository,
    val premiumManager: PremiumManager,
    val billingRepository: BillingRepository,
    val dataStoreManager: DataStoreManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            val interval = dataStoreManager.notificationInterval.first()
            Log.d("MainViewModel", "Initializing notifications with interval: $interval")
            scheduleNotification(interval)
        }
    }

    fun updateNotificationInterval(interval: String) {
        viewModelScope.launch {
            dataStoreManager.saveNotificationInterval(interval)
            scheduleNotification(interval)
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            dataStoreManager.saveLanguage(languageCode)
        }
    }

    private fun scheduleNotification(interval: String) {
        val workManager = WorkManager.getInstance(context)
        if (interval == "Never" || interval == "0") {
            Log.d("MainViewModel", "Cancelling notifications")
            workManager.cancelUniqueWork("timeline_notification")
            return
        }

        val repeatInterval = when (interval) {
            "30", "Every 30 mins" -> 30L
            "60", "Every 1 hour" -> 60L
            "120", "2 hours", "2 hour" -> 120L
            else -> return
        }

        Log.d("MainViewModel", "Scheduling notification every $repeatInterval minutes")

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            repeatInterval, TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "timeline_notification",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
