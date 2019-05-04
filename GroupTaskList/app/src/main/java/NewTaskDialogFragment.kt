import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.BuildConfig
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.flinders.nguy1025.grouptasklist.R
import com.flinders.nguy1025.grouptasklist.Task
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class NewTaskDialogFragment : DialogFragment() {

    interface NewTaskDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, task: Task?)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    private val TAG = "NewTaskDialogFragment"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 100

    private var newTaskDialogListener: NewTaskDialogListener? = null
    private var task: Task? = null

    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Represents a geographical location.
     */
    protected var mLastLocation: Location? = null

    var tvTask: EditText? = null
    var tvNotes: EditText? = null
    var tvDeadline: TextView? = null
    var tvGPS: TextView? = null
    var btnDeadline: Button? = null
    var btnGPS: Button? = null

    companion object {
        const val argTitle = "dialog_title"
        const val argTask = "task"

        fun newInstance(title: Int, task: Task?): NewTaskDialogFragment {
            val newTaskDialogFragment = NewTaskDialogFragment()
            val args = Bundle()

            args.putInt(argTitle, title)
            newTaskDialogFragment.arguments = args

            args.putSerializable(argTask, task)

            return newTaskDialogFragment
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val title = arguments!!.getInt(argTitle)
        this.task = arguments!!.getSerializable(argTask) as Task?

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Activity)

        val builder = AlertDialog.Builder(this.activity!!).setTitle(title)

        val dialogView = activity!!.layoutInflater.inflate(R.layout.dialog_new_task, null)

        tvTask = dialogView.findViewById(R.id.tv_task)
        tvNotes = dialogView.findViewById(R.id.tv_notes)

        btnDeadline = dialogView.findViewById(R.id.btn_deadline)
        tvDeadline = dialogView.findViewById(R.id.tv_deadline)

        btnGPS = dialogView.findViewById(R.id.btn_gps)
        tvGPS = dialogView.findViewById(R.id.tv_gps)

        if (this.task != null) {

            // fill data
            tvTask?.setText(task?.taskDetails)
            tvNotes?.setText(task?.notes)
            updateDeadlineText()
            updateGPSText()
        } else {
            this.task = Task("")
        }

        btnGPS?.setOnClickListener { v ->
            run {

                if (!checkPermissions()) {
                    requestPermissions()
                } else {
                    getLastLocation()
                }
            }
        }

        btnDeadline?.setOnClickListener { v ->
            run {

                val c = Calendar.getInstance()
                val date: Date = task?.getDeadlineDate() ?: Date()
                c.time = date

                val timePicker =
                    TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->

                        var currentDate = Date()
                        c.time = currentDate
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        c.set(Calendar.MINUTE, minute)

                        this.task?.deadline = c.time.time
                        updateDeadlineText()

                    }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true)

                timePicker.show()
            }
        }

        builder.setView(dialogView).setPositiveButton(R.string.save) { _, _ ->
            // save input data

            this.task?.taskDetails = tvTask?.text.toString()
            this.task?.notes = tvNotes?.text.toString()

            newTaskDialogListener?.onDialogPositiveClick(this, this.task)
        }

        builder.setView(dialogView).setNegativeButton(android.R.string.cancel) { _, _ ->
            newTaskDialogListener?.onDialogNegativeClick(this)
        }

        return builder.create()
    }

    private fun updateGPSText() {
        tvGPS?.setText(task?.coordinateString())
    }

    private fun updateDeadlineText() {
        tvDeadline?.setText(this.task?.getDeadlineDate().toString())
    }

    private fun showSnackbarMessage(text: String, action: String) {
        Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
//        Snackbar.make(fab, text, Snackbar.LENGTH_LONG).setAction(action, null).show()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        try {
            newTaskDialogListener = activity as NewTaskDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement NewTaskDialogListener")
        }

    }

    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     *
     *
     * Note: this method should be called after location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {

        mFusedLocationClient!!.lastLocation.addOnCompleteListener(activity as Activity) { locationTask ->
            if (locationTask.isSuccessful && locationTask.result != null) {
                mLastLocation = locationTask.result
                this.task?.latitude = mLastLocation?.latitude
                this.task?.longitude = mLastLocation?.longitude
                updateGPSText()
            } else {
                Log.w(TAG, "getLastLocation:exception", locationTask.exception)
                showSnackbarMessage(getString(R.string.no_location_detected), resources.getString(android.R.string.ok))
            }
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            activity as Activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            activity as Activity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            activity as Activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")

            Snackbar.make(fab, R.string.permission_rationale, Snackbar.LENGTH_LONG)
                .setAction(android.R.string.ok, View.OnClickListener {
                    // Request permission
                    startLocationPermissionRequest()
                }).show()

        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest()
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation()
            } else {
                // Permission denied.
                Snackbar.make(fab, R.string.permission_denied_explanation, Snackbar.LENGTH_LONG)
                    .setAction(R.string.settings, View.OnClickListener {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }).show()


            }
        }
    }


}
