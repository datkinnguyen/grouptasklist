package com.flinders.nguy1025.grouptasklist.Activities

import NewFolderDialogFragment
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.migration.Migration
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import com.flinders.nguy1025.grouptasklist.Adapters.FolderListAdapter
import com.flinders.nguy1025.grouptasklist.Models.AppDatabase
import com.flinders.nguy1025.grouptasklist.Models.Folder
import com.flinders.nguy1025.grouptasklist.Models.TodoListDBContract
import com.flinders.nguy1025.grouptasklist.R
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), NewFolderDialogFragment.NewFolderDialogListener {

    private val selectedIndexInvalid = -1
    private val newFolderFragmentTag = "new_folder"
    private val updateFolderFragmentTag = "update_folder"
    private val fm = supportFragmentManager

    private var todoListFolders = ArrayList<Folder>()
    private var listAdapter: FolderListAdapter? = null

    private var listView: ListView? = null
    var editItem: MenuItem? = null
    var deleteItem: MenuItem? = null

    private var selectedItem = selectedIndexInvalid

    companion object {
        var database: AppDatabase? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, TodoListDBContract.DATABASE_NAME)
            .addMigrations(object :
                Migration(TodoListDBContract.DATABASE_VERSION - 1, TodoListDBContract.DATABASE_VERSION) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
            .fallbackToDestructiveMigration()
            .build()

        // find views
        listView = findViewById(R.id.list_view)

        populateListView()

        listView?.onItemLongClickListener = AdapterView.OnItemLongClickListener(function = { parent, view, position, id ->

            if (selectedItem == position) {

                clearSelected()
                hideMenu()
            } else {
                listView?.setSelector(android.R.color.holo_blue_light)
                showUpdateUI(position)
                showUpdateUI(position)
            }
        })

        listView?.onItemClickListener = AdapterView.OnItemClickListener(function = { parent, view, position, id ->

            // open folder detail screen
            val intent = Intent(this, FolderDetailActivity::class.java)
            var folderId = this.todoListFolders[position].folderId!!

            intent.putExtra(FolderDetailActivity.folderIdExtraKey, folderId)
            startActivity(intent)

            clearSelected()
            hideMenu()
        })

        fab.setOnClickListener {
            showNewUI()
        }
    }

    private fun populateListView() {
        todoListFolders =
            RetrieveFoldersAsyncTask(database).execute().get() as ArrayList<Folder>

        listAdapter = FolderListAdapter(this, todoListFolders)
        listView?.adapter = listAdapter
    }

    private fun showNewUI() {
        val fragment = NewFolderDialogFragment.newInstance(R.string.add_new_folder_dialog_title, null)

        fragment.show(fm, newFolderFragmentTag)
    }

    private fun showUpdateUI(selected: Int): Boolean {
        selectedItem = selected
        switchMenuMode(1)
        invalidateOptionsMenu()
        return true
    }

    private fun switchMenuMode(mode: Int) {
        if (mode == 0) { // normal
            editItem?.isVisible = false
            deleteItem?.isVisible = false
        } else if (mode == 1) { // edit mode

            editItem?.isVisible = true
            deleteItem?.isVisible = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.folder_list_menu, menu)

        editItem = menu.findItem(R.id.edit_item)
        deleteItem = menu.findItem(R.id.delete_item)

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
                    NewFolderDialogFragment.newInstance(
                        R.string.update_folder_dialog_title,
                        todoListFolders[selectedItem]
                    )
                fragment.show(fm, updateFolderFragmentTag)
                return true
            }
            R.id.delete_item -> {
                val selectedTask = todoListFolders[selectedItem]
                DeleteFolderAsyncTask(database, selectedTask).execute()

                todoListFolders.removeAt(selectedItem)
                listAdapter?.notifyDataSetChanged()

                // reset
                clearSelected()

                showSnackbarMessage(resources.getString(R.string.text_deleted_done), "Action")

                hideMenu()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSnackbarMessage(text: String, action: String) {
        Snackbar.make(fab, text, Snackbar.LENGTH_LONG).setAction(action, null).show()
    }

    private fun hideMenu(): Boolean {
        switchMenuMode(0)
        invalidateOptionsMenu()
        return true
    }

    private fun clearSelected() {
        selectedItem = selectedIndexInvalid
        listView?.setSelector(android.R.color.transparent)
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, folder: Folder?) {

        // validate folder description
        if (folder?.name!!.isEmpty()) {
            // show error
            showSnackbarMessage(resources.getString(R.string.text_task_text_required), "Action")
            return
        }

        if (dialog.tag == newFolderFragmentTag) {

            folder.folderId = AddFolderAsyncTask(database, folder).execute().get()

            todoListFolders.add(folder)
            listAdapter?.notifyDataSetChanged()

            clearSelected()

            hideMenu()

            showSnackbarMessage(resources.getString(R.string.text_created_done), "Action")
        } else if (dialog.tag == updateFolderFragmentTag) {
            todoListFolders[selectedItem] = folder

            UpdateFolderAsyncTask(database, folder).execute()

            listAdapter?.notifyDataSetChanged()

            showSnackbarMessage(resources.getString(R.string.text_updated_done), "Action")

            clearSelected()
            hideMenu()
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {

//        hideMenu()
    }

    private class RetrieveFoldersAsyncTask(private val database: AppDatabase?) : AsyncTask<Void, Void, List<Folder>>() {
        override fun doInBackground(vararg params: Void): List<Folder>? {
            return database?.folderDao()?.retrieveFolderList()
        }
    }

    private class AddFolderAsyncTask(
        private val database: AppDatabase?,
        private val newFolder: Folder
    ) : AsyncTask<Void, Void, Long>() {
        override fun doInBackground(vararg params: Void): Long? {
            return database?.folderDao()?.addNewFolder(newFolder)
        }
    }

    private class UpdateFolderAsyncTask(
        private val database: AppDatabase?,
        private val selectedFolder: Folder
    ) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {
            database?.folderDao()?.updateFolder(selectedFolder)
            return null
        }
    }

    private class DeleteFolderAsyncTask(
        private val database: AppDatabase?,
        private val selectedFolder: Folder
    ) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {
            database?.folderDao()?.deleteFolder(selectedFolder)
            return null
        }
    }
}
