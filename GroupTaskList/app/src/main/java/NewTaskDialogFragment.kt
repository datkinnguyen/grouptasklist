import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.*
import com.flinders.nguy1025.grouptasklist.MapsActivity
import com.flinders.nguy1025.grouptasklist.R
import com.flinders.nguy1025.grouptasklist.Task
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.*


class NewTaskDialogFragment : DialogFragment() {

    interface NewTaskDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, task: Task?)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    companion object {
        val REQUEST_MAPS_GPS= 11
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

    private val TAG = "NewTaskDialogFragment"

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 100
    private val REQUEST_CAMERA_REQUEST_CODE = 10


    private var newTaskDialogListener: NewTaskDialogListener? = null
    private var task: Task? = null
    private var imageFile: File? = null

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
    var btnImage: Button? = null
    var imvImage: ImageView? = null

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

        btnImage = dialogView.findViewById(R.id.btn_image)
        imvImage = dialogView.findViewById(R.id.imv_image)

        if (this.task != null) {

            // fill data
            tvTask?.setText(task?.taskDetails)
            tvNotes?.setText(task?.notes)
            updateDeadlineText()
            updateGPSText()
            updateImage()
        } else {
            this.task = Task("")
        }

        btnImage?.setOnClickListener { v ->
            run {

                if (!checkCameraersmission()) {
                    requestCameraPermission()
                } else {
                    openImagePicker()
                }
            }
        }

        btnGPS?.setOnClickListener { v ->
            run {

                val intent = Intent(activity, MapsActivity::class.java)

                var coord = this.task?.coordinateDoubleArray()
                if (coord != null) {
                    intent.putExtra(MapsActivity.coordinateKey, coord)
                }

                startActivityForResult(intent, REQUEST_MAPS_GPS)
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

    private fun openImagePicker() {
        EasyImage.openChooserWithGallery(
            this as Fragment,
            resources.getString(R.string.image_select), 0)
    }

    private fun updateImage() {

        if (task?.imagePath != null) {
            // create Uri from path
            imvImage!!.setImageBitmap(BitmapFactory.decodeFile(task?.imagePath))
        }
    }

    private fun checkCameraersmission(): Boolean {
        return (ContextCompat.checkSelfPermission(activity as Activity, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(activity as Activity,
            android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(activity as Activity, arrayOf(READ_EXTERNAL_STORAGE, CAMERA),
            REQUEST_CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle coordinate returned from maps screen
        if (requestCode == REQUEST_MAPS_GPS && resultCode == Activity.RESULT_OK && data != null) {

            // get gps coordinate return
            val coord = data.getDoubleArrayExtra(MapsActivity.coordinateKey)
            if (coord != null && coord.size == 2) {
                // got valid coordinate
                this.task?.latitude = coord[0]
                this.task?.longitude = coord[1]
                updateGPSText()
            }

        }

        // Handle Image from EasyImage lib
        EasyImage.handleActivityResult(requestCode, resultCode, data, activity as Activity,
            object : DefaultCallback() {
                override fun onImagePickerError(
                    e: Exception?,
                    source: EasyImage.ImageSource?, type: Int
                ) {

                    e!!.printStackTrace()
                }

                override fun onImagesPicked(
                    imageFiles: List<File>,
                    source: EasyImage.ImageSource, type: Int
                ) {

                    if (imageFiles.isNotEmpty()) {
                        imageFile = imageFiles[0]
                        // update to task
                        task?.imagePath = imageFile?.absolutePath

                        updateImage()
                    }
                }

                override fun onCanceled(source: EasyImage.ImageSource?, type: Int) {
                    //Cancel handling, you might wanna remove taken photo if it was canceled
                    if (source == EasyImage.ImageSource.CAMERA) {
                        val photoFile = EasyImage
                            .lastlyTakenButCanceledPhoto(activity as Activity)
                        photoFile?.delete()
                    }
                }

            })

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
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_CAMERA_REQUEST_CODE) {

            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                openImagePicker()
            } else {
                Toast.makeText(activity as Activity,"Permission Denied",Toast.LENGTH_SHORT).show()
            }
            return
        }

    }


}
