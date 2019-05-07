package com.flinders.nguy1025.grouptasklist.Adapters

import com.flinders.nguy1025.grouptasklist.Models.Folder

interface FolderAdapterListener {

    fun onClick(folder: Folder)
    fun onClickEdit(folder: Folder)
    fun onClickDelete(folder: Folder)
}
