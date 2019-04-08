package com.flinders.nguy1025.grouptasklist

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class TaskListAdapter(val context: Context, private val taskList: ArrayList<Task>) : BaseAdapter() {

    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        var view = convertView
        var viewHolder: ViewHolder?

        if (view == null) {
            view = inflater.inflate(R.layout.list_item_task, parent, false)
            viewHolder = ViewHolder()
            viewHolder.taskDescriptionTextView =
                view.findViewById(R.id.task_item_description)
            viewHolder.deadlineTextView = view.findViewById(R.id.task_item_deadline)
            viewHolder.statusTextView = view.findViewById(R.id.task_item_status)
            view.tag = viewHolder

        } else {
            viewHolder = view.tag as ViewHolder?
        }

        // populate value to task item holder
        val task = getItem(position) as Task
        viewHolder?.taskDescriptionTextView?.text = task.taskDetails
        viewHolder?.deadlineTextView?.text = task.taskDeadline

        if (null != task.completed && true == task.completed) {
            viewHolder?.statusTextView?.text = (context.getString(R.string.completed))
            viewHolder?.statusTextView?.setTextColor(
                ResourcesCompat.getColor(
                    context.resources, android.R.color.holo_green_light, null
                )
            )
        } else {
            viewHolder?.statusTextView?.text = (context.getString(R.string.not_completed))
            viewHolder?.statusTextView?.setTextColor(
                ResourcesCompat.getColor(
                    context.resources, android.R.color.holo_red_light, null
                )
            )
        }


        return view;

    }

    override fun getItem(position: Int): Any {
        return taskList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return taskList.size
    }

    private class ViewHolder {
        var taskDescriptionTextView: TextView? = null
        var deadlineTextView: TextView? = null
        var statusTextView: TextView? = null
    }
}