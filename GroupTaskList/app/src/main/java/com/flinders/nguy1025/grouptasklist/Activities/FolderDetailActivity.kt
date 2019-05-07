package com.flinders.nguy1025.grouptasklist.Activities

import NewTaskDialogFragment
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.flinders.nguy1025.grouptasklist.Adapters.TaskAdapterListener
import com.flinders.nguy1025.grouptasklist.Adapters.TaskListAdapter
import com.flinders.nguy1025.grouptasklist.Models.AppDatabase
import com.flinders.nguy1025.grouptasklist.Models.Folder
import com.flinders.nguy1025.grouptasklist.Models.Task
import com.flinders.nguy1025.grouptasklist.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_folder_detail.*
import java.util.*

class FolderDetailActivity : AppCompatActivity(), NewTaskDialogFragment.NewTaskDialogListener {

    companion object {
        val folderIdExtraKey = "folderId"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private val newTaskFragmentTag = "newtask"
    private val updateTaskFragmentTag = "updatetask"
    private val fm = supportFragmentManager

    private var todoListItems = ArrayList<Task>()
    private var listAdapter: TaskListAdapter? = null

    private var listView: ListView? = null
    var hideCompletedItem: MenuItem? = null

    private var hideCompleted: Boolean? = false
        set(value) {
            field = value; updateHideCompletedItem()
        }

    private var database: AppDatabase? = null

    private var folderId: Long = Long.MIN_VALUE
    private var folder: Folder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_detail)

        // point to database object in MainActivity
        database = MainActivity.database

        // find views
        listView = findViewById(R.id.list_view)

        fab_folder_detail.setOnClickListener {
            showNewTaskUI()
        }
    }

    override fun onResume() {
        super.onResume()

        // try to get folderId for update state
        this.folderId = intent.getLongExtra(folderIdExtraKey, 0)
        if (this.folderId > 0) {
            // load folder
            this.folder = RetrieveFolderAsyncTask(database, folderId).execute().get() as Folder
            // update title
            supportActionBar?.title = this.folder?.name
        }

        // load data
        populateListView()
    }

    private fun populateListView() {
        todoListItems =
            RetrieveTasksAsyncTask(database, this.folderId).execute().get() as ArrayList<Task>

        val listener = object : TaskAdapterListener {

            override fun onClick(task: Task) {
                val fragment =
                    NewTaskDialogFragment.newInstance(
                        R.string.update_task_dialog_title, task
                    )
                fragment.show(fm, updateTaskFragmentTag)
            }

            override fun onClickEdit(task: Task) {
                val fragment =
                    NewTaskDialogFragment.newInstance(
                        R.string.update_task_dialog_title, task
                    )
                fragment.show(fm, updateTaskFragmentTag)
            }

            override fun onClickDelete(task: Task) {
                DeleteTaskAsyncTask(database, task).execute()

                todoListItems.remove(task)
                listAdapter?.notifyDataSetChanged()

                // reset
                clearSelected()

                showSnackbarMessage(resources.getString(R.string.text_deleted_done), "Action")
            }

            override fun onClickComplete(task: Task) {
                // Assume we allow un-done task
                task.completed = task.completed?.not()

                UpdateTaskAsyncTask(database, task).execute()

                // reload whole data to hide newly completed one
                if (hideCompleted == true && task.completed == true) {
                    forceReloadData()
                } else {
                    listAdapter?.notifyDataSetChanged()
                }

                if (task.completed == true) {
                    showSnackbarMessage(resources.getString(R.string.text_mark_completed_done), "Action")
                } else {
                    showSnackbarMessage(resources.getString(R.string.text_mark_uncompleted_done), "Action")
                }
            }
        }

        listAdapter = TaskListAdapter(this, todoListItems, listener)
        listView?.adapter = listAdapter

    }

    private fun showNewTaskUI() {
        val fragment = NewTaskDialogFragment.newInstance(R.string.add_new_task_dialog_title, null)

        fragment.show(fm, newTaskFragmentTag)
    }

    private fun updateHideCompletedItem() {
        if (hideCompleted == true) {
            hideCompletedItem?.setIcon(R.drawable.ic_show_all)
        } else {
            hideCompletedItem?.setIcon(R.drawable.ic_hide_completed)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.task_list_menu, menu)

        hideCompletedItem = menu.findItem(R.id.hide_completed_item)
        hideCompletedItem?.isVisible = true

        // default to show all tasks, include completed ones
        hideCompleted = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.hide_completed_item -> {

                // toggle hiding
                hideCompleted = hideCompleted?.not()
                forceReloadData()

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun forceReloadData() {
        todoListItems.clear()
        todoListItems.addAll(
            RetrieveTasksAsyncTask(
                database, folderId, hideCompleted
            ).execute().get() as ArrayList<Task>
        )
        listAdapter?.notifyDataSetChanged()
    }

    private fun showSnackbarMessage(text: String, action: String) {
        Snackbar.make(fab_folder_detail, text, Snackbar.LENGTH_LONG).setAction(action, null).show()
    }

    private fun clearSelected() {
        listView?.setSelector(android.R.color.transparent)
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, task: Task?) {

        // validate task description
        if (task?.taskDetails!!.isEmpty()) {
            // show error
            showSnackbarMessage(resources.getString(R.string.text_task_text_required), "Action")
            return
        }

        if (dialog.tag == newTaskFragmentTag) {

            task.folderId = this.folderId
            task.taskId = AddTaskAsyncTask(database, task).execute().get()

            todoListItems.add(task)
            listAdapter?.notifyDataSetChanged()

            clearSelected()

            showSnackbarMessage(resources.getString(R.string.text_created_done), "Action")
        } else if (dialog.tag == updateTaskFragmentTag) {
            UpdateTaskAsyncTask(database, task).execute()

            listAdapter?.notifyDataSetChanged()

            showSnackbarMessage(resources.getString(R.string.text_updated_done), "Action")

            clearSelected()
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {

//        hideMenu()
    }

    private class RetrieveFolderAsyncTask(
        private val database: AppDatabase?,
        private val folderId: Long
    ) : AsyncTask<Void, Void, Folder>() {
        override fun doInBackground(vararg params: Void): Folder? {
            return database?.folderDao()?.getFolder(folderId)
        }
    }

    private class RetrieveTasksAsyncTask(
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

    private class AddTaskAsyncTask(
        private val database: AppDatabase?,
        private val newTask: Task
    ) : AsyncTask<Void, Void, Long>() {
        override fun doInBackground(vararg params: Void): Long? {
            return database?.taskDao()?.addNewTask(newTask)
        }
    }

    private class UpdateTaskAsyncTask(
        private val database: AppDatabase?,
        private val selectedTask: Task
    ) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {
            database?.taskDao()?.updateTask(selectedTask)
            return null
        }
    }

    private class DeleteTaskAsyncTask(
        private val database: AppDatabase?,
        private val selectedTask: Task
    ) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {
            database?.taskDao()?.deleteTask(selectedTask)
            return null
        }
    }
}
