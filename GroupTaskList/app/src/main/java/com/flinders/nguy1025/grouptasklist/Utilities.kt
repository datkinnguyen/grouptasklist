package com.flinders.nguy1025.grouptasklist

import android.content.Context
import android.widget.Toast

object Utilities {

    /**
     * Show simple toast message
     */
    fun showToast(context: Context, resourceId: Int) {
        Toast.makeText(context, context.resources.getString(resourceId), Toast.LENGTH_LONG).show()
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}