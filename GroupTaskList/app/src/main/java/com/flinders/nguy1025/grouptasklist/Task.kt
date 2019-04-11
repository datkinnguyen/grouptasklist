package com.flinders.nguy1025.grouptasklist

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.provider.BaseColumns
import java.io.Serializable

@Entity(tableName = TodoListDBContract.TodoListItem.TABLE_NAME)
class Task : Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    var taskId : Long? = null

    @ColumnInfo(name = TodoListDBContract.TodoListItem.COLUMN_NAME_TASK_DETAIL)
    var taskDetails: String?

    @ColumnInfo(name = TodoListDBContract.TodoListItem.COLUMN_NAME_DEADLINE)
    var taskDeadline: String? = null

    @ColumnInfo(name = TodoListDBContract.TodoListItem.COLUMN_NAME_COMPLETED)
    var completed: Boolean? = false

    @ColumnInfo(name = TodoListDBContract.TodoListItem.COLUMN_NAME_LATITUDE)
    var latitude: Float? = null

    @ColumnInfo(name = TodoListDBContract.TodoListItem.COLUMN_NAME_LONGITUDE)
    var longitude: Float? = null

    @Ignore
    constructor(taskDetails: String?) {
        this.taskDetails = taskDetails
    }

    constructor(taskId: Long?, taskDetails: String?, taskDeadline: String?, completed: Boolean?) {
        this.taskId = taskId
        this.taskDetails = taskDetails
        this.taskDeadline = taskDeadline
        this.completed = completed
    }

    /**
     * Set coordinate (latitude, longitude) of task
     */
    fun updateCoordinate(lat: Float, long: Float) {
        this.latitude = lat
        this.longitude = longitude
    }

}