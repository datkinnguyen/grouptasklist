import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.flinders.nguy1025.grouptasklist.R
import kotlinx.android.synthetic.main.activity_main.*
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

//    // Acquire a reference to the system Location Manager
//    val locationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//    // Define a listener that responds to location updates
//    val locationListener = object : LocationListener {
//
//        override fun onLocationChanged(location: Location) {
//            // Called when a new location is found by the network location provider.
////            makeUseOfNewLocation(location)
//        }
//
//        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
//        }
//
//        override fun onProviderEnabled(provider: String) {
//        }
//
//        override fun onProviderDisabled(provider: String) {
//        }
//    }

//    private fun checkPermissions(): Boolean {
//        val permissionState = ActivityCompat.checkSelfPermission(this,
//            Manifest.permission.ACCESS_COARSE_LOCATION)
//        return permissionState == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun startLocationPermissionRequest() {
//        ActivityCompat.requestPermissions(this@MainActivity,
//            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
//            REQUEST_PERMISSIONS_REQUEST_CODE)
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val title = arguments!!.getInt(argTitle)
        val selectedText = arguments!!.getString(argSelected)

        val builder = AlertDialog.Builder(this.activity!!).setTitle(title)

        val dialogView = activity!!.layoutInflater.inflate(R.layout.dialog_new_task, null)
        val task = dialogView.findViewById<EditText>(R.id.task)
        val tvIncludeGPS = dialogView.findViewById<Button>(R.id.tv_gps)

        task.setText(selectedText)

        tvIncludeGPS.setOnClickListener { v ->
            run {

                Toast.makeText(context, "TODO: attach GPS", Toast.LENGTH_SHORT).show()
//                Snackbar.make(fab, "TODO: attach GPS", Snackbar.LENGTH_LONG).setAction("action", null).show()
                // Register the listener with the Location Manager to receive location updates
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
            }
        }

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