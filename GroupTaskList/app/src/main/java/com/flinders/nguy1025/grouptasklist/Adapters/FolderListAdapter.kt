package com.flinders.nguy1025.grouptasklist.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.flinders.nguy1025.grouptasklist.Models.Folder
import com.flinders.nguy1025.grouptasklist.R
import java.util.*

class FolderListAdapter(val context: Context, private val folderList: ArrayList<Folder>) : BaseAdapter() {

    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        var view = convertView
        var viewHolder: ViewHolder?

        if (view == null) {
            view = inflater.inflate(R.layout.list_item_folder, parent, false)
            viewHolder = ViewHolder()
            viewHolder.tvFolderName =
                view.findViewById(R.id.tv_folder_name)
            viewHolder.tvFolderTaskCount = view.findViewById(R.id.tv_folder_task_count)
            view.tag = viewHolder

        } else {
            viewHolder = view.tag as ViewHolder?
        }

        // populate value to task item holder
        val folder = getItem(position) as Folder
        viewHolder?.tvFolderName?.text = folder.name
        // hide task count for now
        viewHolder?.tvFolderTaskCount?.text = ""

        return view

    }

    override fun getItem(position: Int): Any {
        return folderList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return folderList.size
    }

    private class ViewHolder {
        var tvFolderName: TextView? = null
        var tvFolderTaskCount: TextView? = null
    }
}