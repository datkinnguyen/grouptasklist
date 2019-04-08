import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.EditText
import com.flinders.nguy1025.grouptasklist.R
import java.lang.ClassCastException


class NewTaskDialogFragment : DialogFragment() {

    interface NewTaskDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, taskDesc: String)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    private var newTaskDialogListener: NewTaskDialogListener? = null

    companion object {
        const val argTitle = "dialog_title"
        const val argSelected = "selected_item"

        fun newInstance(title: Int, selected: String?): NewTaskDialogFragment {
            val newTaskDialogFragment = NewTaskDialogFragment()
            val args = Bundle()

            args.putInt(argTitle, title)
            args.putString(argSelected, selected)
            newTaskDialogFragment.arguments = args

            return newTaskDialogFragment
        }


    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val title = arguments!!.getInt(argTitle)
        val selectedText = arguments!!.getString(argSelected)

        val builder = AlertDialog.Builder(this.activity!!).setTitle(title)

        val dialogView = activity!!.layoutInflater.inflate(R.layout.dialog_new_task, null)
        val task = dialogView.findViewById<EditText>(R.id.task)
        task.setText(selectedText)

        builder.setView(dialogView).setPositiveButton(R.string.save) { _, _ ->
            newTaskDialogListener?.onDialogPositiveClick(this, task.text.toString())
        }

        builder.setView(dialogView).setNegativeButton(android.R.string.cancel) { _, _ ->
            newTaskDialogListener?.onDialogNegativeClick(this)
        }

        return builder.create()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        try {
            newTaskDialogListener = activity as NewTaskDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement NewTaskDialogListener")
        }

    }
}