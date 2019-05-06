package com.flinders.nguy1025.grouptasklist.Models

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = arrayOf(Task::class, Folder::class),
    version = TodoListDBContract.DATABASE_VERSION
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDAO
    abstract fun folderDao(): FolderDAO
}