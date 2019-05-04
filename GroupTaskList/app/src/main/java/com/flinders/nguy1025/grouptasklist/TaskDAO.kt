package com.flinders.nguy1025.grouptasklist

import android.arch.persistence.room.*


@Dao
interface TaskDAO {
    @Query("SELECT * FROM " + TodoListDBContract.TodoListItem.TABLE_NAME)
    fun retrieveTaskList(): List<Task>

    @Query("SELECT * FROM " + TodoListDBContract.TodoListItem.TABLE_NAME
            + " WHERE " + TodoListDBContract.TodoListItem.COLUMN_NAME_COMPLETED + " = 0")
    fun retrieveUnfinishedTaskList(): List<Task>
    @Insert
    fun addNewTask(task: Task): Long
    @Update
    fun updateTask(task: Task)
    @Delete
    fun deleteTask(task: Task)
}