package com.hritik.lifetrackertimeline.presentation.main

import android.content.Context
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

    fun updateNotificationInterval(interval: String) {
        viewModelScope.launch {
            dataStoreManager.saveNotificationInterval(interval)
            scheduleNotification(interval)
        }
    }

    private fun scheduleNotification(interval: String) {
        val workManager = WorkManager.getInstance(context)
        if (interval == "Never") {
            workManager.cancelUniqueWork("timeline_notification")
            return
        }

        val repeatInterval = when (interval) {
            "Every 30 mins" -> 30L
            "Every 1 hour" -> 60L
            "2 hour" -> 120L
            else -> return
        }

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
