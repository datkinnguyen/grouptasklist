package com.flinders.nguy1025.grouptasklist.Models

import android.provider.BaseColumns
import androidx.room.*

@Dao
interface FolderDAO {
    @Query("SELECT * FROM " + TodoListDBContract.TodoListFolder.TABLE_NAME)
    fun retrieveFolderList(): List<Folder>

    @Query("SELECT * FROM " + TodoListDBContract.TodoListFolder.TABLE_NAME + " WHERE " + BaseColumns._ID + " = :id")
    fun getFolder(id: Long): Folder

    @Insert
    fun addNewFolder(folder: Folder): Long

    @Update
    fun updateFolder(folder: Folder)

    @Delete
    fun deleteFolder(folder: Folder)

    @Query("SELECT * FROM " + TodoListDBContract.TodoListItem.TABLE_NAME + " WHERE " + TodoListDBContract.TodoListItem.COLUMN_NAME_FOLDER_ID + " = :folderId")
    fun getTasks(folderId: Long): List<Task>

//    fun getFolderWithTasks(id: Long): Folder {
//        val folder = getFolder(id)
//        val tasks = getTasks(id)
//        folder.tasks = tasks
//        return folder
//    }
}