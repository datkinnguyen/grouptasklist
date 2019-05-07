package com.flinders.nguy1025.grouptasklist.Activities

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.flinders.nguy1025.grouptasklist.Models.AppDatabase
import com.flinders.nguy1025.grouptasklist.Models.DBTasksHelper
import com.flinders.nguy1025.grouptasklist.Models.Task
import com.flinders.nguy1025.grouptasklist.R
import com.flinders.nguy1025.grouptasklist.Utilities
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.*


class NewTaskActivity : AppCompatActivity() {

    companion object {
        const val MODE_NEW = 0
        const val MODE_EDIT = 1
        val REQUEST_MAPS_GPS = 11

        const val argTask = "task"
        const val argFolderId = "folderId"

    }

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 100
    private val REQUEST_CAMERA_REQUEST_CODE = 10

    private var task: Task? = null
    private var imageFile: File? = null
    private var mode: Int = MODE_NEW
    private var folderId: Long? = null

    private var database: AppDatabase? = MainActivity.database

    var tvTask: EditText? = null
    var tvNotes: EditText? = null
    var tvDeadline: TextView? = null
    var tvGPS: TextView? = null
    var btnDeadline: Button? = null
    var btnGPS: Button? = null
    var btnImage: Button? = null
    var imvImage: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        tvTask = findViewById(R.id.tv_task)
        tvNotes = findViewById(R.id.tv_notes)

        btnDeadline = findViewById(R.id.btn_deadline)
        tvDeadline = findViewById(R.id.tv_deadline)

        btnGPS = findViewById(R.id.btn_gps)
        tvGPS = findViewById(R.id.tv_gps)

        btnImage = findViewById(R.id.btn_image)
        imvImage = findViewById(R.id.imv_image)

        val btnCancel = findViewById<Button>(R.id.btn_cancel)
        val btnSave = findViewById<Button>(R.id.btn_save)
        btnCancel?.setOnClickListener { v ->
            run {
                onClickCancel()
            }
        }

        btnSave?.setOnClickListener { v ->
            run {
                onClickSave()
            }
        }

        btnImage?.setOnClickListener { v ->
            run {

                if (!checkCameraPermission()) {
                    requestCameraPermission()
                } else {
                    openImagePicker()
                }
            }
        }

        btnGPS?.setOnClickListener { v ->
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

        btnDeadline?.setOnClickListener { v ->
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
        this.task = intent?.getSerializableExtra(argTask) as Task?

        if (this.task != null) {
            mode = MODE_EDIT
            // fill data
            tvTask?.setText(task?.taskDetails)
            tvNotes?.setText(task?.notes)
            updateDeadlineText()
            updateGPSText()
            updateImage()
        } else {
            mode = MODE_NEW
            this.task = Task("", 0)
        }

        if (mode == MODE_NEW) {
            supportActionBar?.title = resources.getString(R.string.add_new_task_dialog_title)
        } else {
            supportActionBar?.title = resources.getString(R.string.update_task_dialog_title)
        }
    }

    private fun onClickSave() {
        // validate task description
        if (tvTask?.text.toString().isEmpty()) {
            // show error
            Utilities.showToast(this, R.string.text_task_text_required)
            return
        }

        // save input data
        this.task?.taskDetails = tvTask?.text.toString()
        this.task?.notes = tvNotes?.text.toString()

        if (mode == NewTaskActivity.MODE_NEW) {

            task?.folderId = this.folderId
            task?.taskId = DBTasksHelper.AddTaskAsyncTask(database, task!!).execute().get()

        } else if (mode == NewTaskActivity.MODE_EDIT) {
            DBTasksHelper.UpdateTaskAsyncTask(database, task!!).execute()
        }

        // done
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun onClickCancel() {
        //just dismiss
        finish()
    }

    private fun updateGPSText() {
        tvGPS?.text = task?.coordinateString()
    }

    private fun updateDeadlineText() {
        tvDeadline?.text = this.task?.getDeadlineDate()?.toString()
    }

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
        }
    }

    private fun checkCameraPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(READ_EXTERNAL_STORAGE, CAMERA),
            REQUEST_CAMERA_REQUEST_CODE
        )
    }

    fun updateSelectedImage(imgUri: File?) {
        // update to task
        imageFile = imgUri
        task?.imagePath = imageFile?.absolutePath

        updateImage()

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

                    Utilities.showToast(this@NewTaskActivity, e!!.localizedMessage)
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
                            .lastlyTakenButCanceledPhoto(this@NewTaskActivity)
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

}
