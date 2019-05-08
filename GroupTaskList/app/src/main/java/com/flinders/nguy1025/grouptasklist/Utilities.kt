package com.flinders.nguy1025.grouptasklist

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog


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

    /**
     * Show a simple dialog with a title, message, and 2 buttons with their listeners
     */
    fun showSimpleDialog(
        context: Context,
        title: String?,
        message: String?,
        positiveText: String?,
        positiveListener: DialogInterface.OnClickListener?,
        negativeText: String?,
        negativeListener: DialogInterface.OnClickListener?
    ) {

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(positiveText) { dialog, whichButton ->
                positiveListener?.onClick(dialog, whichButton)
            }
            .setNegativeButton(negativeText) { dialog, which -> negativeListener?.onClick(dialog, which) }
            .show()
    }

    /**
     * Key softkeyboard
     */
    fun hideKeyboard(activity: Activity) {
        // Two different methods to hide the keyboard, to handle most scenarios
        val isHidden = (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(
                activity.window.decorView.windowToken, 0
            )
        if (!isHidden) {
            activity.window
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }

    }
}