package com.flinders.nguy1025.grouptasklist.Activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.flinders.nguy1025.grouptasklist.Adapters.TaskAdapterListener
import com.flinders.nguy1025.grouptasklist.Adapters.TaskListAdapter
import com.flinders.nguy1025.grouptasklist.Models.DBTasksHelper
import com.flinders.nguy1025.grouptasklist.Models.Folder
import com.flinders.nguy1025.grouptasklist.Models.Task
import com.flinders.nguy1025.grouptasklist.R
import com.flinders.nguy1025.grouptasklist.Utilities
import kotlinx.android.synthetic.main.activity_folder_detail.*


class FolderDetailActivity : AppCompatActivity() {

    companion object {
        val folderIdExtraKey = "folderId"
    }

    private var todoListItems = ArrayList<Task>()
    private var listAdapter: TaskListAdapter? = null

    private var listView: ListView? = null
    private var hideCompletedItem: MenuItem? = null

    private var hideCompleted: Boolean? = false
        set(value) { // custom setter to auto update UI when this property is updated
            field = value; updateHideCompletedItem()
        }

    private var folderId: Long = Long.MIN_VALUE
    private var folder: Folder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_detail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // find views
        listView = findViewById(R.id.list_view)

        listView?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

            openAddEditTaskScreen(todoListItems[position])
        }

        fab_folder_detail.setOnClickListener {
            showNewTaskUI()
        }

        populateListView()
    }

    private fun openAddEditTaskScreen(task: Task?) {

        val intent = Intent(this, TaskRecordActivity::class.java)

        if (task != null) {
            intent.putExtra(TaskRecordActivity.argTask, task)
            intent.putExtra(TaskRecordActivity.argMode, TaskRecordActivity.RecordMode.VIEW)
        }

        intent.putExtra(TaskRecordActivity.argFolderId, this.folderId)

        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        // try to get folderId for update state
        this.folderId = intent.getLongExtra(folderIdExtraKey, 0)
        if (this.folderId > 0) {
            // load folder
            this.folder =
                DBTasksHelper.RetrieveFolderAsyncTask(MainActivity.database, folderId).execute().get() as Folder
            // update title
            supportActionBar?.title = this.folder?.name
        }

        // load data
        forceReloadData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // save edit mode state
        outState.putSerializable(folderIdExtraKey, this.folderId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        this.folderId = savedInstanceState?.getLong(folderIdExtraKey) as Long
    }

    private fun populateListView() {
        todoListItems = ArrayList()

        val listener = object : TaskAdapterListener {

            override fun onClick(task: Task) {
                openAddEditTaskScreen(task)
            }

            override fun onClickEdit(task: Task) {
                openAddEditTaskScreen(task)
            }

            override fun onClickDelete(task: Task) {

                // confirm before actually delete data
                Utilities.showSimpleDialog(
                    this@FolderDetailActivity,
                    resources.getString(R.string.delete_confirm_title),
                    resources.getString(R.string.delete_confirm_message),
                    resources.getString(R.string.text_yes),
                    DialogInterface.OnClickListener { dialog, which ->
                        DBTasksHelper.DeleteTaskAsyncTask(MainActivity.database, task).execute()

                        todoListItems.remove(task)
                        listAdapter?.notifyDataSetChanged()

                        // reset
                        clearSelected()

                        Utilities.showToast(this@FolderDetailActivity, R.string.text_deleted_done)
                    },
                    resources.getString(R.string.text_no),
                    null
                )
            }

            override fun onClickComplete(task: Task) {
                // Assume we allow un-done task
                task.completed = task.completed?.not()

                DBTasksHelper.UpdateTaskAsyncTask(MainActivity.database, task).execute()

                // reload whole data to hide newly completed one
                if (hideCompleted == true && task.completed == true) {
                    forceReloadData()
                } else {
                    listAdapter?.notifyDataSetChanged()
                }

                if (task.completed == true) {
                    Utilities.showToast(this@FolderDetailActivity, R.string.text_mark_completed_done)
                } else {
                    Utilities.showToast(this@FolderDetailActivity, R.string.text_mark_uncompleted_done)
                }
            }
        }

        listAdapter = TaskListAdapter(this, todoListItems, listener)
        listView?.adapter = listAdapter
    }

    private fun forceReloadData() {
        todoListItems.clear()
        todoListItems.addAll(
            DBTasksHelper.RetrieveTasksAsyncTask(
                MainActivity.database, folderId, hideCompleted
            ).execute().get() as ArrayList<Task>
        )
        listAdapter?.notifyDataSetChanged()
    }

    private fun clearSelected() {
        listView?.setSelector(android.R.color.transparent)
    }

    private fun showNewTaskUI() {
        openAddEditTaskScreen(null)
    }

    private fun updateHideCompletedItem() {
        if (hideCompleted == true) {
            hideCompletedItem?.setIcon(R.drawable.ic_show_all)
            Utilities.showToast(this, R.string.text_hide_completed_task)
        } else {
            hideCompletedItem?.setIcon(R.drawable.ic_hide_completed)
            Utilities.showToast(this, R.string.text_show_all_task)
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
            android.R.id.home -> {
                // dismiss this screen
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
