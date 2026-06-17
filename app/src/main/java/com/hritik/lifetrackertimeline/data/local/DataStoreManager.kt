package com.hritik.lifetrackertimeline.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class DataStoreManager @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val NOTIFICATION_INTERVAL = stringPreferencesKey("notification_interval")
    }

    val notificationInterval: Flow<String> = dataStore.data.map { preferences ->
        preferences[NOTIFICATION_INTERVAL] ?: "Never"
    }

    suspend fun saveNotificationInterval(interval: String) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_INTERVAL] = interval
        }
    }
}
