package com.hritik.lifetrackertimeline.presentation.main

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.hritik.lifetrackertimeline.data.local.DataStoreManager
import com.hritik.lifetrackertimeline.data.repository.BackupRepository
import com.hritik.lifetrackertimeline.data.repository.BillingRepository
import com.hritik.lifetrackertimeline.data.repository.UserRepository
import com.hritik.lifetrackertimeline.helper.PremiumManager
import com.hritik.lifetrackertimeline.worker.NotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val dataStoreManager: DataStoreManager,
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _isBackupLoading = MutableStateFlow(false)
    val isBackupLoading: StateFlow<Boolean> = _isBackupLoading.asStateFlow()

    private val _backupMessage = MutableSharedFlow<String>()
    val backupMessage: SharedFlow<String> = _backupMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            val interval = dataStoreManager.notificationInterval.first()
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
            workManager.cancelUniqueWork("timeline_notification")
            return
        }

        val repeatInterval = when (interval) {
            "30", "Every 30 mins" -> 30L
            "60", "Every 1 hour" -> 60L
            "120", "2 hours", "2 hour" -> 120L
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

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _isBackupLoading.value = true
            val result = backupRepository.exportData(uri)
            _isBackupLoading.value = false
            if (result.isSuccess) {
                _backupMessage.emit("Data exported successfully")
            } else {
                _backupMessage.emit("Export failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _isBackupLoading.value = true
            val result = backupRepository.importData(uri)
            _isBackupLoading.value = false
            if (result.isSuccess) {
                _backupMessage.emit("Data imported successfully")
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                if (error == "Invalid backup file") {
                    _backupMessage.emit("Invalid backup file. Please select a valid backup exported from this app.")
                } else {
                    _backupMessage.emit("Import failed: $error")
                }
            }
        }
    }
}
