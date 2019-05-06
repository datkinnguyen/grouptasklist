import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.EditText
import com.flinders.nguy1025.grouptasklist.Models.Folder
import com.flinders.nguy1025.grouptasklist.R


class NewFolderDialogFragment : DialogFragment() {

    interface NewFolderDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, folder: Folder?)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    companion object {
        private val TAG = "NewFolderDialogFragment"

        const val argTitle = "dialog_title"
        const val argFolderName = "folder_name"

        fun newInstance(title: Int, folder: Folder?): NewFolderDialogFragment {
            val newDialogFragment = NewFolderDialogFragment()
            val args = Bundle()

            args.putInt(argTitle, title)
            newDialogFragment.arguments = args

            args.putSerializable(argFolderName, folder)

            return newDialogFragment
        }

    }

    private var newTaskDialogListener: NewFolderDialogListener? = null
    private var folder: Folder? = null

    private var tvFolderName: EditText? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val title = arguments!!.getInt(argTitle)
        this.folder = arguments!!.getSerializable(argFolderName) as Folder?

        val builder = AlertDialog.Builder(this.activity!!).setTitle(title)

        val dialogView = activity!!.layoutInflater.inflate(R.layout.dialog_new_folder, null)
        tvFolderName = dialogView.findViewById(R.id.tv_folder_name)

        if (this.folder != null) {

            // fill data
            tvFolderName?.setText(folder?.name)

        } else {
            this.folder = Folder("")
        }

        builder.setView(dialogView).setPositiveButton(R.string.save) { _, _ ->
            // save input data

            this.folder?.name = tvFolderName?.text.toString()
            newTaskDialogListener?.onDialogPositiveClick(this, this.folder)
        }

        builder.setView(dialogView).setNegativeButton(android.R.string.cancel) { _, _ ->
            newTaskDialogListener?.onDialogNegativeClick(this)
        }

        return builder.create()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        try {
            newTaskDialogListener = activity as NewFolderDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement NewFolderDialogListener")
        }

    }
}
