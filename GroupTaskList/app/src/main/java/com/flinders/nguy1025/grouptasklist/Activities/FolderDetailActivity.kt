package com.flinders.nguy1025.grouptasklist.Activities

import NewTaskDialogFragment
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import com.flinders.nguy1025.grouptasklist.Adapters.TaskListAdapter
import com.flinders.nguy1025.grouptasklist.Models.AppDatabase
import com.flinders.nguy1025.grouptasklist.Models.Folder
import com.flinders.nguy1025.grouptasklist.Models.Task
import com.flinders.nguy1025.grouptasklist.R
import kotlinx.android.synthetic.main.activity_folder_detail.*
import java.util.*

class FolderDetailActivity : AppCompatActivity(), NewTaskDialogFragment.NewTaskDialogListener {

    companion object {
        val folderIdExtraKey = "folderId"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private val selectedIndexInvalid = -1
    private val newTaskFragmentTag = "newtask"
    private val updateTaskFragmentTag = "updatetask"
    private val fm = supportFragmentManager

    private var todoListItems = ArrayList<Task>()
    private var listAdapter: TaskListAdapter? = null

    private var listView: ListView? = null
    var hideCompletedItem: MenuItem? = null
    var editItem: MenuItem? = null
    var deleteItem: MenuItem? = null
    var markDoneItem: MenuItem? = null

    private var selectedItem = selectedIndexInvalid

    private var hideCompleted: Boolean? = false

    private var database: AppDatabase? = null

    private var folderId: Long = Long.MIN_VALUE
    private var folder: Folder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_detail)
//        setSupportActionBar(toolbar)

        // point to database object in MainActivity
        database = MainActivity.database

        // find views
        listView = findViewById(R.id.list_view)

        listView?.onItemClickListener = AdapterView.OnItemClickListener(function = { parent, view, position, id ->

            if (selectedItem == position) {

                clearSelected()
                hideMenu()
            } else {
                listView?.setSelector(android.R.color.holo_blue_light)
                showUpdateTaskUI(position)
            }
        })

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

        listAdapter = TaskListAdapter(this, todoListItems)
        listView?.adapter = listAdapter
    }

    private fun showNewTaskUI() {
        val fragment = NewTaskDialogFragment.newInstance(R.string.add_new_task_dialog_title, null)

        fragment.show(fm, newTaskFragmentTag)
    }

    private fun showUpdateTaskUI(selected: Int) {
        selectedItem = selected
        switchMenuMode(1)
        invalidateOptionsMenu()
    }

    private fun switchMenuMode(mode: Int) {
        if (mode == 0) { // normal
            hideCompletedItem?.isVisible = true

            editItem?.isVisible = false
            deleteItem?.isVisible = false
            markDoneItem?.isVisible = false
        } else if (mode == 1) { // edit mode
            hideCompletedItem?.isVisible = false

            editItem?.isVisible = true
            deleteItem?.isVisible = true
            markDoneItem?.isVisible = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.task_list_menu, menu)

        hideCompletedItem = menu.findItem(R.id.hide_completed_item)
        editItem = menu.findItem(R.id.edit_item)
        deleteItem = menu.findItem(R.id.delete_item)
        markDoneItem = menu.findItem(R.id.mark_as_done_item)

        if (selectedItem == selectedIndexInvalid) {
            switchMenuMode(0)
        } else {
            switchMenuMode(1)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.edit_item -> {
                val fragment =
                    NewTaskDialogFragment.newInstance(
                        R.string.update_task_dialog_title,
                        todoListItems[selectedItem]
                    )
                fragment.show(fm, updateTaskFragmentTag)
                return true
            }
            R.id.delete_item -> {
                val selectedTask = todoListItems[selectedItem]
                DeleteTaskAsyncTask(database, selectedTask).execute()

                todoListItems.removeAt(selectedItem)
                listAdapter?.notifyDataSetChanged()

                // reset
                clearSelected()

                showSnackbarMessage(resources.getString(R.string.text_deleted_done), "Action")

                hideMenu()
                return true
            }
            R.id.mark_as_done_item -> {
                // Assume we allow un-done task
                val selectedTask = todoListItems[selectedItem]

                selectedTask.completed = selectedTask.completed?.not()

                UpdateTaskAsyncTask(database, selectedTask).execute()
                listAdapter?.notifyDataSetChanged()

                if (selectedTask.completed == true) {
                    showSnackbarMessage(resources.getString(R.string.text_mark_completed_done), "Action")
                } else {
                    showSnackbarMessage(resources.getString(R.string.text_mark_uncompleted_done), "Action")
                }

                return true
            }
            R.id.hide_completed_item -> {

                // toggle hiding
                hideCompleted = hideCompleted?.not()
                todoListItems.clear()
                todoListItems.addAll(
                    RetrieveTasksAsyncTask(
                        database, folderId, hideCompleted
                    ).execute().get() as ArrayList<Task>
                )
                listAdapter?.notifyDataSetChanged()

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSnackbarMessage(text: String, action: String) {
        Snackbar.make(fab_folder_detail, text, Snackbar.LENGTH_LONG).setAction(action, null).show()
    }

    private fun hideMenu() {
        switchMenuMode(0)
        invalidateOptionsMenu()
    }

    private fun clearSelected() {
        selectedItem = selectedIndexInvalid
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

            hideMenu()

            showSnackbarMessage(resources.getString(R.string.text_created_done), "Action")
        } else if (dialog.tag == updateTaskFragmentTag) {
            todoListItems[selectedItem] = task

            UpdateTaskAsyncTask(database, task).execute()

            listAdapter?.notifyDataSetChanged()

            showSnackbarMessage(resources.getString(R.string.text_updated_done), "Action")

            clearSelected()
            hideMenu()
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
