package com.flinders.nguy1025.grouptasklist

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.migration.Migration
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView

import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity(), NewTaskDialogFragment.NewTaskDialogListener {

    private val selectedIndexInvalid = -1
    private val newTaskFragmentTag = "newtask"
    private val updateTaskFragmentTag = "updatetask"
    private val fm = supportFragmentManager

    private var todoListItems = ArrayList<Task>()
    private var listAdapter: TaskListAdapter? = null

    private var listView: ListView? = null

    private var showMenuItems = false
    private var selectedItem = selectedIndexInvalid

    private var database: AppDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, TodoListDBContract.DATABASE_NAME)
            .addMigrations(object :
                Migration(TodoListDBContract.DATABASE_VERSION - 1, TodoListDBContract.DATABASE_VERSION) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
            .build()

        // find views
        listView = findViewById(R.id.list_view)

        populateListView()

        listView?.onItemClickListener = AdapterView.OnItemClickListener(function = { parent, view, position, id ->

            if (selectedItem == position) {

                clearSelected()
                hideMenu()
            } else {
                listView?.setSelector(android.R.color.holo_blue_light)
                showUpdateTaskUI(position)
            }
        })

        fab.setOnClickListener {
            showNewTaskUI()
        }
    }

    private fun populateListView() {
        todoListItems =
            RetrieveTasksAsyncTask(database).execute().get() as ArrayList<Task>

        listAdapter = TaskListAdapter(this, todoListItems)
        listView?.adapter = listAdapter
    }

    private fun showNewTaskUI() {
        val fragment = NewTaskDialogFragment.newInstance(R.string.add_new_task_dialog_title, null)

        fragment.show(fm, newTaskFragmentTag)
    }

    private fun showUpdateTaskUI(selected: Int) {
        selectedItem = selected
        showMenuItems = true
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.task_list_menu, menu)

        val editItem = menu.findItem(R.id.edit_item)
        val deleteItem = menu.findItem(R.id.delete_item)
        val markDoneItem = menu.findItem(R.id.mark_as_done_item)

        editItem.isVisible = showMenuItems
        deleteItem.isVisible = showMenuItems
        markDoneItem.isVisible = showMenuItems

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
                        todoListItems[selectedItem].taskDetails
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSnackbarMessage(text: String, action: String) {
        Snackbar.make(fab, text, Snackbar.LENGTH_LONG).setAction(action, null).show()
    }

    private fun hideMenu() {
        showMenuItems = false
        invalidateOptionsMenu()
    }

    private fun clearSelected() {
        selectedItem = selectedIndexInvalid
        listView?.setSelector(android.R.color.transparent)
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, taskDesc: String) {

        // validate task description
        if (taskDesc.isEmpty()) {
            // show error
            showSnackbarMessage(resources.getString(R.string.text_task_text_required), "Action")
            return
        }

        if (dialog.tag == newTaskFragmentTag) {
            val addNewTask = Task(taskDesc)

            addNewTask.taskId = AddTaskAsyncTask(database, addNewTask).execute().get()

            todoListItems.add(addNewTask)
            listAdapter?.notifyDataSetChanged()

            clearSelected()

            hideMenu()

            showSnackbarMessage(resources.getString(R.string.text_created_done), "Action")
        } else if (dialog.tag == updateTaskFragmentTag) {
            val selectedTask = todoListItems[selectedItem]
            selectedTask.taskDetails = taskDesc

            UpdateTaskAsyncTask(database, selectedTask).execute()

            listAdapter?.notifyDataSetChanged()

            showSnackbarMessage(resources.getString(R.string.text_updated_done), "Action")

            clearSelected()
            hideMenu()
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {

//        hideMenu()
    }

    private class RetrieveTasksAsyncTask(private val database: AppDatabase?) : AsyncTask<Void, Void, List<Task>>() {
        override fun doInBackground(vararg params: Void): List<Task>? {
            return database?.taskDao()?.retrieveTaskList()
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
