package com.flinders.nguy1025.grouptasklist.Models

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Task::class, Folder::class),
    version = TodoListDBContract.DATABASE_VERSION
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDAO
    abstract fun folderDao(): FolderDAO
}