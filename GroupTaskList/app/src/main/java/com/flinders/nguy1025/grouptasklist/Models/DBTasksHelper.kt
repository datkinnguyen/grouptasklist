package com.flinders.nguy1025.grouptasklist.Models

import android.os.AsyncTask

/**
 * Class containing helper methods to call database DAO level
 */
class DBTasksHelper {

    /**
     * Retrieve list of Folders from database
     * @param database: Room's AppDatabase to query data
     */
    class RetrieveFoldersAsyncTask(private val database: AppDatabase?) : AsyncTask<Void, Void, List<Folder>>() {
        override fun doInBackground(vararg params: Void): List<Folder>? {
            return database?.folderDao()?.retrieveFolderList()
        }
    }

    /**
     * Async task to Add new folder to database
     * @param database: Room's AppDatabase to query data
     *          newFolder: Folder: the folder to be inserted into database
     *
     */
    class AddFolderAsyncTask(
        private val database: AppDatabase?,
        private val newFolder: Folder
    ) : AsyncTask<Void, Void, Long>() {
        override fun doInBackground(vararg params: Void): Long? {
            return database?.folderDao()?.addNewFolder(newFolder)
        }
    }

    /**
     * Update Folder async task
     * @param database: Room's AppDatabase to query data
     *      selectedFolder: Folder: the folder to be updated in database
     */
    class UpdateFolderAsyncTask(
        private val database: AppDatabase?,
        private val selectedFolder: Folder
    ) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {
            database?.folderDao()?.updateFolder(selectedFolder)
            return null
        }
    }

    /**
     * Async task to delete Folder from database
     * @param database: Room's AppDatabase to query data
     *          selectedFolder: Folder: the folder to be deleted from database
     */
    class DeleteFolderAsyncTask(
        private val database: AppDatabase?,
        private val selectedFolder: Folder
    ) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {
            database?.folderDao()?.deleteFolder(selectedFolder)
            return null
        }
    }

    /**
     * Retrieve a specific Folder from database by its folderId
     * @param database: Room's AppDatabase to query data
     *          folderId: id of folder to get
     */
    class RetrieveFolderAsyncTask(
        private val database: AppDatabase?,
        private val folderId: Long
    ) : AsyncTask<Void, Void, Folder>() {
        override fun doInBackground(vararg params: Void): Folder? {
            return database?.folderDao()?.getFolder(folderId)
        }
    }

    /**
     * Retrieve list of Tasks from database
     * @param database: Room's AppDatabase to query data
     *          folderId: if of folder to find tasks in
     *          hideCompleted: boolean flag indicating whether to retrieve commpleted tasks
     */
    class RetrieveTasksAsyncTask(
        private val database: AppDatabase?,
        private val folderId: Long,
        private val hideCompleted: Boolean? = false
    ) : AsyncTask<Void, Void, List<Task>>() {
        override fun doInBackground(vararg params: Void): List<Task>? {
            if (hideCompleted == true) {
                return database?.taskDao()?.retrieveUnfinishedTaskList(folderId)
            } else {
                return database?.taskDao()?.retrieveTaskList(folderId)
            }
        }
    }

    /**
     * Async task to delete task from database
     * @param database: Room's AppDatabase to query data
     *          selectedTask: task to delete from database
     */
    class DeleteTaskAsyncTask(
        private val database: AppDatabase?,
        private val selectedTask: Task
    ) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {
            database?.taskDao()?.deleteTask(selectedTask)
            return null
        }
    }

    /**
     * Async task to add task to database
     * @param database: Room's AppDatabase to query data
     *          newTask: the task to be inserted into database
     */
    class AddTaskAsyncTask(
        private val database: AppDatabase?,
        private val newTask: Task
    ) : AsyncTask<Void, Void, Long>() {
        override fun doInBackground(vararg params: Void): Long? {
            return database?.taskDao()?.addNewTask(newTask)
        }
    }

    /**
     * Async task to update task in database
     * @param database: Room's AppDatabase to query data
     *          selectedTask: the task to be updated
     */
    class UpdateTaskAsyncTask(
        private val database: AppDatabase?,
        private val selectedTask: Task
    ) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {
            database?.taskDao()?.updateTask(selectedTask)
            return null
        }
    }
}