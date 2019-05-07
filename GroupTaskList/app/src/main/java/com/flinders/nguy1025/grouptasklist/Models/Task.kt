package com.flinders.nguy1025.grouptasklist.Models

import android.provider.BaseColumns
import androidx.room.*
import java.io.Serializable
import java.util.*

@Entity(tableName = TodoListDBContract.TodoListItem.TABLE_NAME
    , foreignKeys = arrayOf(
        ForeignKey(entity = Folder::class,
        parentColumns = arrayOf(BaseColumns._ID),
            childColumns = arrayOf(TodoListDBContract.TodoListItem.COLUMN_NAME_FOLDER_ID), onDelete = ForeignKey.CASCADE
        )
    )
)
class Task : Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    var taskId : Long? = null

    @ColumnInfo(name = TodoListDBContract.TodoListItem.COLUMN_NAME_FOLDER_ID)
    var folderId: Long? = null

    @ColumnInfo(name = TodoListDBContract.TodoListItem.COLUMN_NAME_TASK_DETAIL)
    var taskDetails: String?

    @ColumnInfo(name = TodoListDBContract.TodoListItem.COLUMN_NAME_DEADLINE)
    var deadline: Long? = null

    @ColumnInfo(name = TodoListDBContract.TodoListItem.COLUMN_NAME_COMPLETED)
    var completed: Boolean? = false

    @ColumnInfo(name = TodoListDBContract.TodoListItem.COLUMN_NAME_LATITUDE)
    var latitude: Double? = null

    @ColumnInfo(name = TodoListDBContract.TodoListItem.COLUMN_NAME_LONGITUDE)
    var longitude: Double? = null

    @ColumnInfo(name = TodoListDBContract.TodoListItem.COLUMN_NAME_NOTES)
    var notes: String? = null

    @ColumnInfo(name = TodoListDBContract.TodoListItem.COLUMN_NAME_IMAGE)
    var imagePath: String? = null

    @Ignore
    constructor(taskDetails: String?, folderId: Long) {
        this.taskDetails = taskDetails
        this.folderId = folderId
    }

    constructor(taskId: Long?, folderId: Long?, taskDetails: String?, deadline: Long?, notes: String?, completed: Boolean?) {
        this.taskId = taskId
        this.folderId = folderId
        this.taskDetails = taskDetails
        this.notes = notes
        this.completed = completed
        this.deadline = deadline
    }

    /**
     * Set coordinate (latitude, longitude) of task
     */
    fun updateCoordinate(lat: Double, long: Double) {
        this.latitude = lat
        this.longitude = long
    }

    fun coordinateDoubleArray() :  DoubleArray? {
        if (this.latitude != null && this.longitude != null) {
            var coord = DoubleArray(2)
            coord[0] = this.latitude!!
            coord[1] = this.longitude!!

            return coord
        }
        return null
    }

    fun coordinateString(): String {
        if (this.latitude != null && this.longitude != null) {
            return this.latitude.toString() + "," + this.longitude.toString()
        }
        return ""
    }

    fun getDeadlineDate() : Date? {
        return this.deadline?.let { Date(it) }
    }


}