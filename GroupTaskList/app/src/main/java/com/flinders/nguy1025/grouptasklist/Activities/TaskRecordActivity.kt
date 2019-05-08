package com.flinders.nguy1025.grouptasklist.Activities

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.flinders.nguy1025.grouptasklist.Models.AppDatabase
import com.flinders.nguy1025.grouptasklist.Models.DBTasksHelper
import com.flinders.nguy1025.grouptasklist.Models.Task
import com.flinders.nguy1025.grouptasklist.R
import com.flinders.nguy1025.grouptasklist.Utilities
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.*

/**
 * Activity to show Task Details. This screen is also used for showing as Add new task screen,
 * View Task screen and Edit Task screen by managing different `mode`.
 */
class TaskRecordActivity : AppCompatActivity() {

    enum class RecordMode {
        ADD, EDIT, VIEW
    }

    companion object {

        const val argTask = "task"
        const val argMode = "mode"
        const val argImageFile = "imageFile"
        const val argFolderId = "folderId"
    }

    private val REQUEST_CAMERA_REQUEST_CODE = 10
    private val REQUEST_MAPS_GPS = 11

    private var task: Task? = null
    private var imageFile: File? = null
    private var mode: RecordMode = RecordMode.ADD
    private var folderId: Long? = null

    private var database: AppDatabase? = MainActivity.database

    private var tvTask: EditText? = null
    private var tvNotes: EditText? = null
    private var tvDeadline: TextView? = null
    private var tvGPS: TextView? = null
    private var imvImage: ImageView? = null
    private var imvComplete: ImageView? = null
    private var tvAddImage: TextView? = null
    private var editItem: MenuItem? = null
    private var deleteItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        configUIEvents()
    }

    private fun configUIEvents() {
        tvTask = findViewById(R.id.tv_task)
        tvNotes = findViewById(R.id.tv_notes)
        imvComplete = findViewById(R.id.imv_completed)
        tvDeadline = findViewById(R.id.tv_deadline)
        tvGPS = findViewById(R.id.tv_gps)
        tvAddImage = findViewById(R.id.tv_add_image)
        imvImage = findViewById(R.id.imv_image)

        val viewAddDueDate = findViewById<View>(R.id.view_add_due_date)
        val viewAddLocation = findViewById<View>(R.id.view_add_location)
        val viewAddImage = findViewById<View>(R.id.view_add_image)

        tvNotes?.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.colorTextNormal)))
        tvGPS?.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.colorTextNormal)))
        tvDeadline?.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.colorTextNormal)))
        tvAddImage?.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.colorTextNormal)))
        tvTask?.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.colorTextNormal)))

        imvComplete?.setOnClickListener { v ->
            run {

                this.task?.completed = this.task?.completed?.not()
                performSave()
                updateCompletedStatus()
            }
        }

        viewAddImage?.setOnClickListener { v ->
            run {

                if (!checkCameraPermission()) {
                    requestCameraPermission()
                } else {
                    openImagePicker()
                }
            }
        }

        viewAddLocation?.setOnClickListener { v ->
            run {

                val intent = Intent(this, MapsActivity::class.java)

                var coord = this.task?.coordinateDoubleArray()
                if (coord != null) {
                    intent.putExtra(MapsActivity.coordinateKey, coord)
                }

                startActivityForResult(
                    intent,
                    REQUEST_MAPS_GPS
                )
            }
        }

        viewAddDueDate?.setOnClickListener { v ->
            run {

                val c = Calendar.getInstance()
                val date: Date = task?.getDeadlineDate() ?: Date()
                c.time = date

                val timePicker =
                    TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->

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
    }

    override fun onResume() {
        super.onResume()

        // load data
        this.folderId = intent?.getLongExtra(argFolderId, 0)
        this.mode = intent?.getSerializableExtra(argMode) as RecordMode
        this.task = intent?.getSerializableExtra(argTask) as Task?

        if (this.task != null) {
            refreshData()
        } else {
            this.task = Task("", 0)
        }

        switchMode(this.mode)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // save edit mode state
        outState.putSerializable(argMode, this.mode)
        outState.putSerializable(argTask, this.task)
        outState.putString(argMode, this.imageFile?.absolutePath)
        outState.putLong(argFolderId, this.folderId!!)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        this.mode = savedInstanceState?.getSerializable(argMode) as RecordMode
        this.task = savedInstanceState.getSerializable(argTask) as Task?
        val imagePath: String? = savedInstanceState.getString(argImageFile)
        if (!imagePath.isNullOrBlank()) {
            this.imageFile = File(imagePath)
        }
        this.folderId = savedInstanceState.getLong(argFolderId)
    }

    /**
     * Fill data from task object to UI components
     */
    private fun refreshData() {
        // fill data
        tvTask?.setText(task?.taskDetails)
        tvNotes?.setText(task?.notes)
        updateCompletedStatus()
        updateDeadlineText()
        updateGPSText()
        updateImage()
    }

    /**
     * Switch mode between VIEW, EDIT and ADD:
     * - VIEW: disable interactions on all views. (cannot edit text, tap on button..)
     * - EDIT: enable interactions, allow users to edit task details
     * - ADD: enable interactions, allow users to input task details
     */
    private fun switchMode(mode: RecordMode) {
        this.mode = mode
        when (mode) {
            RecordMode.VIEW -> {

                tvNotes?.isFocusable = false
                tvNotes?.isClickable = false

                tvTask?.isFocusable = false
                tvTask?.isClickable = false

                tvDeadline?.isClickable = false
                tvGPS?.isClickable = false
                tvAddImage?.isClickable = false

                supportActionBar?.title = resources.getString(R.string.view_task_dialog_title)
            }
            RecordMode.EDIT -> {

                tvNotes?.isFocusable = true
                tvNotes?.isFocusableInTouchMode = true
                tvNotes?.isClickable = true

                tvTask?.isFocusable = true
                tvTask?.isFocusableInTouchMode = true
                tvTask?.isClickable = true

                tvDeadline?.isClickable = true
                tvGPS?.isClickable = true
                tvAddImage?.isClickable = true

                supportActionBar?.title = resources.getString(R.string.update_task_dialog_title)
            }
            RecordMode.ADD -> {

                tvNotes?.isFocusable = true
                tvNotes?.isFocusableInTouchMode = true
                tvNotes?.isClickable = true

                tvTask?.isFocusable = true
                tvTask?.isFocusableInTouchMode = true
                tvTask?.isClickable = true

                tvDeadline?.isClickable = true
                tvGPS?.isClickable = true
                tvAddImage?.isClickable = true

                supportActionBar?.title = resources.getString(R.string.add_new_task_dialog_title)
            }
        }

        // force menu items to refresh
        invalidateOptionsMenu()
    }

    private fun performSave() {
        // validate task description
        if (tvTask?.text.toString().isEmpty()) {
            // show error
            Utilities.showToast(this, R.string.text_task_text_required)
            return
        }

        // save input data
        this.task?.taskDetails = tvTask?.text.toString()
        this.task?.notes = tvNotes?.text.toString()

        if (mode == RecordMode.ADD) {

            task?.folderId = this.folderId
            task?.taskId = DBTasksHelper.AddTaskAsyncTask(database, task!!).execute().get()

        } else if (mode == RecordMode.EDIT || mode == RecordMode.VIEW) {
            DBTasksHelper.UpdateTaskAsyncTask(database, task!!).execute()
        }
    }

    private fun updateCompletedStatus() {
        if (task?.completed == true) {
            imvComplete?.setImageResource(R.drawable.ic_checkmark)
            ImageViewCompat.setImageTintList(
                imvComplete!!,
                ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
            )

            // crossed out title
            tvTask?.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

        } else {
            imvComplete?.setImageResource(R.drawable.ic_checkmark_empty)
            ImageViewCompat.setImageTintList(
                imvComplete!!,
                ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
            )

            tvTask?.paintFlags = Paint.LINEAR_TEXT_FLAG
        }
    }

    private fun updateGPSText() {
        if (this.task?.coordinateDoubleArray() != null) {
            tvGPS?.text = this.task?.coordinateString()
        } else {
            tvGPS?.text = resources.getString(R.string.add_location)
        }
    }

    private fun updateDeadlineText() {
        if (this.task?.getDeadlineDate() != null) {
            tvDeadline?.text = this.task?.getDeadlineDate()?.toString()
            tvDeadline?.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.colorDueText)))
        } else {
            tvDeadline?.text = resources.getString(R.string.add_due_date)
            tvDeadline?.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.colorTextNormal)))
        }
    }

    /**
     * Open image picker to either take a picture by camera or select a picture from gallery
     * Use open-source EasyImage at https://github.com/jkwiecien/EasyImage
     */
    private fun openImagePicker() {
        EasyImage.openChooserWithGallery(
            this,
            resources.getString(R.string.image_select), 0
        )
    }

    private fun updateImage() {
        if (task?.imagePath != null) {
            // create Uri from path
            imvImage!!.setImageBitmap(BitmapFactory.decodeFile(task?.imagePath))
            imvImage!!.visibility = View.VISIBLE
        } else {
            imvImage!!.visibility = View.GONE
        }
    }

    private fun updateSelectedImage(imgUri: File?) {
        // update to task
        imageFile = imgUri
        task?.imagePath = imageFile?.absolutePath

        updateImage()

    }

    /**
     * Check camera permission.
     * @return true if permission is granted. false otherwise
     *
     * Courtesy of https://medium.com/@droidbyme/get-current-location-using-fusedlocationproviderclient-in-android-cb7ebf5ab88e
     */
    private fun checkCameraPermission(): Boolean {
        return (
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(READ_EXTERNAL_STORAGE, CAMERA), REQUEST_CAMERA_REQUEST_CODE
        )
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
        EasyImage.handleActivityResult(
            requestCode, resultCode, data, this,
            object : DefaultCallback() {
                override fun onImagePickerError(
                    e: Exception?,
                    source: EasyImage.ImageSource?, type: Int
                ) {

                    Utilities.showToast(this@TaskRecordActivity, e!!.localizedMessage)
                }

                override fun onImagesPicked(
                    imageFiles: List<File>,
                    source: EasyImage.ImageSource, type: Int
                ) {
                    if (imageFiles.isNotEmpty()) {
                        updateSelectedImage(imageFiles[0])
                    }
                }

                override fun onCanceled(source: EasyImage.ImageSource?, type: Int) {
                    //Cancel handling, you might wanna remove taken photo if it was canceled
                    if (source == EasyImage.ImageSource.CAMERA) {
                        // delete taken but canceled photo
                        val photoFile = EasyImage
                            .lastlyTakenButCanceledPhoto(this@TaskRecordActivity)
                        photoFile?.delete()
                    }
                }

            })
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_REQUEST_CODE) {

            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {

                openImagePicker()

            } else {
                Utilities.showToast(this, R.string.permission_denied_explanation)
            }
            return
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.task_record_menu, menu)

        editItem = menu.findItem(R.id.edit_item)
        deleteItem = menu.findItem(R.id.delete_item)

        when (mode) {
            RecordMode.VIEW -> {
                editItem?.setIcon(R.drawable.ic_edit)
                editItem?.isVisible = true
                deleteItem?.isVisible = true
            }
            RecordMode.EDIT -> {
                editItem?.setIcon(R.drawable.ic_done)
                editItem?.isVisible = true
                deleteItem?.isVisible = true
            }
            RecordMode.ADD -> {
                editItem?.setIcon(R.drawable.ic_done)
                editItem?.isVisible = true
                deleteItem?.isVisible = false
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.edit_item -> {

                when (mode) {
                    RecordMode.VIEW -> switchMode(RecordMode.EDIT)
                    RecordMode.EDIT -> {
                        // In EDIT mode, save and change to VIEW MODE
                        performSave()
                        switchMode(RecordMode.VIEW)

                        Utilities.hideKeyboard(this)

                    }
                    RecordMode.ADD -> {
                        // In ADD mode, save and finish the view
                        performSave()

                        Utilities.hideKeyboard(this)

                        finish()
                    }
                }

                return true
            }
            R.id.delete_item -> {

                // confirm before actually delete data
                Utilities.showSimpleDialog(
                    this,
                    getString(R.string.delete_confirm_title), getString(R.string.delete_confirm_message),
                    getString(R.string.text_yes), DialogInterface.OnClickListener { dialog, which ->
                        DBTasksHelper.DeleteTaskAsyncTask(database, this.task!!).execute()

                        Utilities.showToast(this@TaskRecordActivity, R.string.text_deleted_done)

                        // deleted, dismiss this screen
                        finish()

                    },
                    getString(R.string.text_no), null
                )

                return true
            }
            android.R.id.home -> {
                // dismiss this screen
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
