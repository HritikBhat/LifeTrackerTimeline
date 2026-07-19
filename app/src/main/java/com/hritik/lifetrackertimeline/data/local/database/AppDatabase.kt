package com.hritik.lifetrackertimeline.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hritik.lifetrackertimeline.data.local.dao.UserDao
import com.hritik.lifetrackertimeline.data.local.dao.TaskDao
import com.hritik.lifetrackertimeline.data.local.dao.TimelineDao
import com.hritik.lifetrackertimeline.data.local.entity.UserEntity
import com.hritik.lifetrackertimeline.data.local.entity.TaskEntity
import com.hritik.lifetrackertimeline.data.local.entity.TimelineEntity

@Database(entities = [UserEntity::class, TaskEntity::class, TimelineEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun timelineDao(): TimelineDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add description to timeline table (safely)
                if (!columnExists(db, "timeline", "description")) {
                    db.execSQL("ALTER TABLE timeline ADD COLUMN description TEXT NOT NULL DEFAULT ''")
                }

                // 2. Handle tasks table (removing notes column)
                // Check if we even need to migrate the tasks table (if it still has 'notes' or needs new structure)
                // For safety in this specific migration, we'll check if tasks_new already exists from a failed attempt
                db.execSQL("DROP TABLE IF EXISTS tasks_new")
                
                db.execSQL("""
                    CREATE TABLE tasks_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        isUnproductive INTEGER NOT NULL DEFAULT 0,
                        color INTEGER NOT NULL,
                        icon TEXT NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        lastSelectedAt INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // We need to check which columns exist in the old 'tasks' table to avoid errors during INSERT
                val taskColumns = getTableColumns(db, "tasks")
                val columnsToSelect = mutableListOf<String>()
                val targetColumns = listOf("id", "title", "isUnproductive", "color", "icon", "isActive", "lastSelectedAt")
                
                targetColumns.forEach { col ->
                    if (taskColumns.contains(col)) {
                        columnsToSelect.add(col)
                    }
                }
                
                val columnsString = columnsToSelect.joinToString(", ")
                if (columnsString.isNotEmpty()) {
                    db.execSQL("""
                        INSERT INTO tasks_new ($columnsString)
                        SELECT $columnsString FROM tasks
                    """.trimIndent())
                }

                db.execSQL("DROP TABLE tasks")
                db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
            }

            private fun columnExists(db: SupportSQLiteDatabase, tableName: String, columnName: String): Boolean {
                val cursor = db.query("PRAGMA table_info($tableName)")
                val nameIndex = cursor.getColumnIndex("name")
                if (nameIndex == -1) {
                    cursor.close()
                    return false
                }
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIndex) == columnName) {
                        cursor.close()
                        return true
                    }
                }
                cursor.close()
                return false
            }

            private fun getTableColumns(db: SupportSQLiteDatabase, tableName: String): List<String> {
                val columns = mutableListOf<String>()
                val cursor = db.query("PRAGMA table_info($tableName)")
                val nameIndex = cursor.getColumnIndex("name")
                if (nameIndex != -1) {
                    while (cursor.moveToNext()) {
                        columns.add(cursor.getString(nameIndex))
                    }
                }
                cursor.close()
                return columns
            }
        }
    }
}