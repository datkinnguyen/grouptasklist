package com.flinders.nguy1025.grouptasklist

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView

import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity(), NewTaskDialogFragment.NewTaskDialogListener {

    private val selectedIndexInvalid = -1
    private val newTaskFragmentTag = "newtask"
    private val updateTaskFragmentTag = "updatetask"
    private val fm = supportFragmentManager

    private var todoListItems = ArrayList<String>()
    private var listAdapter: ArrayAdapter<String>? = null

    private var listView: ListView? = null

    private var showMenuItems = false
    private var selectedItem = selectedIndexInvalid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // find views
        listView = findViewById(R.id.list_view)

        populateListView()

        listView?.onItemClickListener = AdapterView.OnItemClickListener(function = { parent, view, position, id ->

            if (selectedItem == position) {
                selectedItem = selectedIndexInvalid

                listView?.setSelector(android.R.color.transparent)

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
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, todoListItems)
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

        editItem.isVisible = showMenuItems
        deleteItem.isVisible = showMenuItems

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.edit_item -> {
                val fragment =
                    NewTaskDialogFragment.newInstance(R.string.update_task_dialog_title, todoListItems[selectedItem])
                fragment.show(fm, updateTaskFragmentTag)
                return true
            }
            R.id.delete_item -> {
                todoListItems.removeAt(selectedItem)
                listView?.clearChoices()
                listAdapter?.notifyDataSetChanged()

                // reset
                selectedItem = selectedIndexInvalid



                showSnackbarMessage("Task deleted successfully!", "Action")

                hideMenu()
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

    override fun onDialogPositiveClick(dialog: DialogFragment, task: String) {

        if (dialog.tag == newTaskFragmentTag) {
            todoListItems.add(task)
            listAdapter?.notifyDataSetChanged()
            selectedItem = selectedIndexInvalid

            showSnackbarMessage("Task Added Successfully!", "Action")
        } else if (dialog.tag == updateTaskFragmentTag) {
            todoListItems[selectedItem] = task
            listView?.clearChoices()
            listAdapter?.notifyDataSetChanged()
            selectedItem = selectedIndexInvalid

            showSnackbarMessage("Task Updated Successfully!", "Action")

            hideMenu()
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {

        hideMenu()
    }
}
