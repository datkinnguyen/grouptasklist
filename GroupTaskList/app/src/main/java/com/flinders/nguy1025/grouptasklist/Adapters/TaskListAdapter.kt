package com.flinders.nguy1025.grouptasklist.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.flinders.nguy1025.grouptasklist.Models.Task
import com.flinders.nguy1025.grouptasklist.R
import com.github.zawadz88.materialpopupmenu.popupMenu
import java.util.*

class TaskListAdapter(
    val context: Context,
    private val taskList: ArrayList<Task>,
    private val taskAdapterListener: TaskAdapterListener
) : BaseAdapter() {

    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        var view = convertView
        var viewHolder: ViewHolder?

        if (view == null) {
            view = inflater.inflate(R.layout.list_item_task, parent, false)
            viewHolder = ViewHolder()
            viewHolder.taskDescriptionTextView =
                view.findViewById(R.id.task_item_description)
            viewHolder.notesTextView = view.findViewById(R.id.tv_notes)
            viewHolder.deadlineTextView = view.findViewById(R.id.tv_deadline)
            viewHolder.statusTextView = view.findViewById(R.id.task_item_status)
            viewHolder.bgView = view.findViewById(R.id.bg_view)
            viewHolder.btnOptions = view.findViewById(R.id.btn_option)
            view.tag = viewHolder

        } else {
            viewHolder = view.tag as ViewHolder?
        }

        // populate value to task item holder
        val task = getItem(position) as Task
        viewHolder?.taskDescriptionTextView?.text = task.taskDetails
        viewHolder?.notesTextView?.text = task.notes

        viewHolder?.btnOptions?.setOnClickListener { view -> showOptionsMenu(view, task) }
        viewHolder?.bgView?.setOnClickListener { view -> taskAdapterListener.onClick(task) }

        if (task.getDeadlineDate() != null) {
            var passDeadline = ""
            if (task.getDeadlineDate() != null && Date().after(task.getDeadlineDate())) {
                passDeadline = context.resources.getString(R.string.deadline_passed)
            }
            viewHolder?.deadlineTextView?.text = "${task.getDeadlineDate()?.toString()}$passDeadline"
        } else {
            viewHolder?.deadlineTextView?.text = null
        }

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

        return view
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

    fun showOptionsMenu(view: View, task: Task) {
        val popupMenu = popupMenu {
            section {
                item {
                    labelRes = if (task.completed == false) {
                        R.string.mark_as_done
                    } else {
                        R.string.mark_as_not_done
                    }
                    icon = if (task.completed == false) {
                        R.drawable.check
                    } else {
                        R.drawable.check_box_empty
                    }
                    callback = {
                        //optional
                        taskAdapterListener.onClickComplete(task)
                    }
                }
                item {
                    labelRes = R.string.edit
                    icon = android.R.drawable.ic_menu_edit
                    callback = {
                        //optional
                        taskAdapterListener.onClickEdit(task)
                    }
                }
                item {
                    labelRes = R.string.delete
                    icon = android.R.drawable.ic_delete
                    callback = {
                        //optional
                        taskAdapterListener.onClickDelete(task)
                    }
                }
            }
        }

        popupMenu.show(context, view)
    }

    private class ViewHolder {
        var bgView: View? = null
        var btnOptions: ImageView? = null
        var taskDescriptionTextView: TextView? = null
        var deadlineTextView: TextView? = null
        var notesTextView: TextView? = null
        var statusTextView: TextView? = null
    }
}