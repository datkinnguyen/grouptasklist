package com.flinders.nguy1025.grouptasklist.Adapters

import com.flinders.nguy1025.grouptasklist.Models.Task

interface TaskAdapterListener {

    fun onClick(task: Task)
    fun onClickEdit(task: Task)
    fun onClickDelete(task: Task)
    fun onClickComplete(task: Task)
}
