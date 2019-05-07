package com.flinders.nguy1025.grouptasklist.Activities

import NewFolderDialogFragment
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.flinders.nguy1025.grouptasklist.Adapters.FolderAdapterListener
import com.flinders.nguy1025.grouptasklist.Adapters.FolderListAdapter
import com.flinders.nguy1025.grouptasklist.Models.AppDatabase
import com.flinders.nguy1025.grouptasklist.Models.Folder
import com.flinders.nguy1025.grouptasklist.Models.TodoListDBContract
import com.flinders.nguy1025.grouptasklist.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), NewFolderDialogFragment.NewFolderDialogListener {

    private val newFolderFragmentTag = "new_folder"
    private val updateFolderFragmentTag = "update_folder"
    private val fm = supportFragmentManager

    private var todoListFolders = ArrayList<Folder>()
    private var listAdapter: FolderListAdapter? = null

    private var listView: ListView? = null

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

        fab.setOnClickListener {
            showNewUI()
        }
    }

    private fun populateListView() {
        todoListFolders =
            RetrieveFoldersAsyncTask(database).execute().get() as ArrayList<Folder>

        val listener = object : FolderAdapterListener {

            override fun onClick(folder: Folder) {
                // open folder detail screen
                val intent = Intent(this@MainActivity, FolderDetailActivity::class.java)
                var folderId = folder.folderId!!

                intent.putExtra(FolderDetailActivity.folderIdExtraKey, folderId)
                startActivity(intent)

                clearSelected()
            }

            override fun onClickEdit(folder: Folder) {
                val fragment =
                    NewFolderDialogFragment.newInstance(
                        R.string.update_folder_dialog_title, folder
                    )
                fragment.show(fm, updateFolderFragmentTag)
            }

            override fun onClickDelete(folder: Folder) {
                DeleteFolderAsyncTask(database, folder).execute()

                todoListFolders.remove(folder)
                listAdapter?.notifyDataSetChanged()

                // reset
                clearSelected()

                showSnackbarMessage(resources.getString(R.string.text_deleted_done), "Action")
            }
        }
        listAdapter = FolderListAdapter(this, todoListFolders, listener)
        listView?.adapter = listAdapter
    }

    private fun showNewUI() {
        val fragment = NewFolderDialogFragment.newInstance(R.string.add_new_folder_dialog_title, null)

        fragment.show(fm, newFolderFragmentTag)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        return true
    }

    private fun showSnackbarMessage(text: String, action: String) {
        Snackbar.make(fab, text, Snackbar.LENGTH_LONG).setAction(action, null).show()
    }

    private fun clearSelected() {
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

            showSnackbarMessage(resources.getString(R.string.text_created_done), "Action")
        } else if (dialog.tag == updateFolderFragmentTag) {
//            todoListFolders[selectedItem] = folder

            UpdateFolderAsyncTask(database, folder).execute()

            listAdapter?.notifyDataSetChanged()

            showSnackbarMessage(resources.getString(R.string.text_updated_done), "Action")

            clearSelected()
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
