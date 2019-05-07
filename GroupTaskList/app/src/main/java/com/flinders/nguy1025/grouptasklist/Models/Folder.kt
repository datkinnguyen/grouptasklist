package com.flinders.nguy1025.grouptasklist.Models

import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = TodoListDBContract.TodoListFolder.TABLE_NAME)
class Folder(@ColumnInfo(name = TodoListDBContract.TodoListFolder.COLUMN_NAME_NAME) var name: String?) : Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = BaseColumns._ID)
    var folderId: Long? = null

    @Ignore
            /**
             * List of tasks in this group
             */
    var tasks: List<Task>? = null

}