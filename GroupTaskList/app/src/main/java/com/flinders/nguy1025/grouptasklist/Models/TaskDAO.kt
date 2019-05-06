package com.flinders.nguy1025.grouptasklist.Models

import android.arch.persistence.room.*


@Dao
interface TaskDAO {
    @Query("SELECT * FROM " + TodoListDBContract.TodoListItem.TABLE_NAME + " WHERE " + TodoListDBContract.TodoListItem.COLUMN_NAME_FOLDER_ID + " = :folderId")
    fun retrieveTaskList(folderId: Long): List<Task>

    @Query("SELECT * FROM " + TodoListDBContract.TodoListItem.TABLE_NAME
            + " WHERE " + TodoListDBContract.TodoListItem.COLUMN_NAME_COMPLETED + " = 0 AND " + TodoListDBContract.TodoListItem.COLUMN_NAME_FOLDER_ID + " = :folderId")
    fun retrieveUnfinishedTaskList(folderId: Long): List<Task>

    @Insert
    fun addNewTask(task: Task): Long

    @Update
    fun updateTask(task: Task)

    @Delete
    fun deleteTask(task: Task)
}