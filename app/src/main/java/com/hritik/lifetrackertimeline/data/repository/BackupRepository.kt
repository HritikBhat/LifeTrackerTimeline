package com.hritik.lifetrackertimeline.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.hritik.lifetrackertimeline.data.local.database.AppDatabase
import com.hritik.lifetrackertimeline.data.model.BackupData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val gson: Gson
) {
    private val TAG = "BackupRepository"

    suspend fun exportData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backupData = BackupData(
                version = 1,
                users = database.userDao().getAllUsersList(),
                tasks = database.taskDao().getAllTasksList(),
                timeline = database.timelineDao().getAllTimelineEntriesList()
            )
            val json = gson.toJson(backupData)
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                }
            }
            Log.d(TAG, "Data exported successfully to $uri")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Export failed", e)
            Result.failure(e)
        }
    }

    suspend fun importData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext Result.failure(Exception("Could not read file"))

            val backupData = try {
                gson.fromJson(json, BackupData::class.java)
            } catch (e: JsonSyntaxException) {
                null
            }

            // Validation
            if (backupData == null || backupData.version < 1) {
                return@withContext Result.failure(Exception("Invalid backup file"))
            }

            // Check if lists are null (Gson might set them to null if missing in JSON)
            if (backupData.users == null || backupData.tasks == null || backupData.timeline == null) {
                return@withContext Result.failure(Exception("Invalid backup file"))
            }

            // Perform Import in a transaction
            database.withTransaction {
                database.userDao().clearUser()
                database.taskDao().clearTasks()
                database.timelineDao().clearTimeline()

                database.userDao().insertUsers(backupData.users)
                database.taskDao().insertTasks(backupData.tasks)
                database.timelineDao().insertTimelineEntries(backupData.timeline)
            }

            Log.d(TAG, "Data imported successfully from $uri")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Import failed", e)
            Result.failure(e)
        }
    }
}
